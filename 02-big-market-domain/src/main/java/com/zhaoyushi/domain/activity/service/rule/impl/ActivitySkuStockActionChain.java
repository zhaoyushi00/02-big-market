package com.zhaoyushi.domain.activity.service.rule.impl;

import com.zhaoyushi.domain.activity.model.entity.ActivityCountEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivityEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivitySkuEntity;
import com.zhaoyushi.domain.activity.service.rule.AbstractActionChain;
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
        log.info("活动责任链-商品库存处理【校验&扣减】开始");
        return true;
    }
}
