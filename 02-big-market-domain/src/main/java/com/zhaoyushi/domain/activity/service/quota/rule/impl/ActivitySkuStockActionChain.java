package com.zhaoyushi.domain.activity.service.quota.rule.impl;

import com.zhaoyushi.domain.activity.model.entity.ActivityCountEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivityEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivitySkuEntity;
import com.zhaoyushi.domain.activity.model.valobj.ActivitySkuStockKeyVO;
import com.zhaoyushi.domain.activity.repository.IActivityRepository;
import com.zhaoyushi.domain.activity.service.armory.IActivityDispatch;
import com.zhaoyushi.domain.activity.service.quota.rule.AbstractActionChain;
import com.zhaoyushi.types.enums.ResponseCode;
import com.zhaoyushi.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 活动 SKU 库存校验责任链节点
 *
 * <p>作为活动下单动作责任链的尾节点，负责校验并扣减活动 SKU 的库存。
 * 尾节点不再向后传递（无下一节点）。
 *
 * @author zhaoy
 * @date 2026/10/25
 * @description 活动 SKU 库存校验责任链节点
 */
@Slf4j
@Component("activity_sku_stock_action")
public class ActivitySkuStockActionChain extends AbstractActionChain {

    // 注入活动调度器，用于扣减活动 SKU 缓存库存
    @Resource
    private IActivityDispatch activityDispatch;
    // 注入活动仓储，用于将库存扣减记录写入延迟队列
    @Resource
    private IActivityRepository activityRepository;

    /**
     * 执行活动 SKU 库存校验与扣减
     *
     * <p>当前为流程占位：仅打印日志后直接返回 true，具体库存校验、扣减逻辑待补充。
     *
     * @param activitySkuEntity   活动商品（SKU）实体
     * @param activityEntity      活动实体
     * @param activityCountEntity 活动次数配置实体
     * @return 校验结果：当前恒返回 true（占位）
     */
    @Override
    public boolean action(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity) {
        // 记录库存处理开始日志（携带 sku 与 activityId）
        log.info("活动责任链-基础信息【有效期、状态、库存sku】校验开始 sku:{} activityId:{}", activitySkuEntity.getSku(), activityEntity.getActivityId());

        // 扣减库存：调用调度器扣减 Redis 缓存库存（入参为 sku 与活动结束时间）
        boolean status = activityDispatch.subtractionActivitySkuStock(activitySkuEntity.getSku(), activityEntity.getEndDateTime());

        // true；库存扣减成功
        if(status){
            // 记录库存扣减成功日志
            log.info("活动责任链-商品库存处理【有效期、状态、库存(sku)】成功。sku:{} activityId:{}", activitySkuEntity.getSku(), activityEntity.getActivityId());

            // 写入延迟队列，延迟消费更新库存记录（异步趋势落库，避免高并发写库）
            activityRepository.activitySkuStockConsumeSendQueue(ActivitySkuStockKeyVO.builder()
                    // 设置队列元素：商品 SKU 编号
                    .sku(activitySkuEntity.getSku())
                    // 设置队列元素：活动ID
                    .activityId(activityEntity.getActivityId())
                    // 完成构建
                    .build());

            // 扣减成功，返回 true
            return true;
        }

        // 扣减失败（库存耗尽/不足），抛出业务异常（活动库存不足）
        throw new AppException(ResponseCode.ACTIVITY_SKU_STOCK_ERROR.getCode(), ResponseCode.ACTIVITY_SKU_STOCK_ERROR.getInfo());
    }
}
