package com.zhaoyushi.domain.activity.service.rule.impl;

import com.zhaoyushi.domain.activity.model.entity.ActivityCountEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivityEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivitySkuEntity;
import com.zhaoyushi.domain.activity.service.rule.AbstractActionChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
        log.info("活动责任链-基础信息【有效期、状态】校验开始");
        return next().action(activitySkuEntity, activityEntity, activityCountEntity);
    }
}
