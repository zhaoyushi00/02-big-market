package com.zhaoyushi.domain.activity.service.quota.rule.impl;

import com.zhaoyushi.domain.activity.model.entity.ActivityCountEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivityEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivitySkuEntity;
// 引入活动状态值对象，用于校验活动是否处于 open 状态
import com.zhaoyushi.domain.activity.model.valobj.ActivityStateVO;
import com.zhaoyushi.domain.activity.service.quota.rule.AbstractActionChain;
// 引入响应码枚举，用于抛出业务异常时指定错误码与提示
import com.zhaoyushi.types.enums.ResponseCode;
// 引入业务异常类，校验失败时抛出
import com.zhaoyushi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// 引入 java.util.Date，用于活动日期范围校验
import java.util.Date;

/**
 * 活动基础信息校验责任链节点
 *
 * <p>作为活动下单动作责任链的头节点，负责校验活动的基础信息（有效期、状态等），
 * 校验通过后调用 {@link #next()} 将请求传递给下一节点（SKU 库存校验）。
 *
 * @author zhaoy
 * @date 2026/10/25
 * @description 活动基础信息校验责任链节点
 */
@Slf4j
@Component("activity_base_action")
public class ActivityBaseActionChain extends AbstractActionChain {

    /**
     * 执行活动基础信息校验
     *
     * <p>当前为流程占位：仅打印日志后直接透传到下一节点，具体校验逻辑（有效期、状态）待补充。
     *
     * @param activitySkuEntity   活动商品（SKU）实体
     * @param activityEntity      活动实体
     * @param activityCountEntity 活动次数配置实体
     * @return 下一节点的校验结果
     */
    @Override
    public boolean action(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity) {
        // 记录校验开始日志（携带 sku 与 activityId，便于问题追踪）
        log.info("活动责任链-基础信息【有效期、状态、库存sku】校验开始 sku:{} activityId:{}", activitySkuEntity.getSku(), activityEntity.getActivityId());

        // 校验：活动状态（只有 open 状态的活动才允许下单）
        if(!ActivityStateVO.open.equals(activityEntity.getState())){
            // 状态非法则抛出业务异常（活动未开启）
            throw new AppException(ResponseCode.ACTIVITY_DATE_ERROR.getCode(), ResponseCode.ACTIVITY_STATE_ERROR.getInfo());
        }

        // 校验活动日期：当前时间必须在活动开始与结束时间之间
        Date currentDate = new Date();
        // 判断当前时间是否早于开始时间或晚于结束时间（任一成立即为非活动日期范围）
        if (activityEntity.getBeginDateTime().after(currentDate) || activityEntity.getEndDateTime().before(currentDate)){
            // 日期非法则抛出业务异常（非活动日期范围）
            throw new AppException(ResponseCode.ACTIVITY_DATE_ERROR.getCode(), ResponseCode.ACTIVITY_DATE_ERROR.getInfo());
        }

        // 校验活动 sku 库存（缓存获取）：剩余库存必须大于 0
        if(activitySkuEntity.getStockCountSurplus() <= 0){
            // 库存不足则抛出业务异常（活动库存不足）
            throw new AppException(ResponseCode.ACTIVITY_SKU_STOCK_ERROR.getCode(), ResponseCode.ACTIVITY_SKU_STOCK_ERROR.getInfo());
        }

        // 校验全部通过，调用下一责任链节点（SKU 库存校验/扣减节点）
        return next().action(activitySkuEntity, activityEntity, activityCountEntity);
    }
}
