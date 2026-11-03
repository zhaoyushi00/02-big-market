package com.zhaoyushi.domain.activity.service.quota.rule;

import com.zhaoyushi.domain.activity.model.entity.ActivityCountEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivityEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivitySkuEntity;

/**
 * 下单规则过滤接口（责任链节点）
 *
 * <p>继承 {@link IActionChainArmory} 获得链表装配能力，同时定义下单动作的校验入口 {@link #action}。
 * 每个实现类代表一个校验节点（如基础信息校验、SKU 库存校验），由工厂 {@link com.zhaoyushi.domain.activity.service.quota.rule.factory.DefaultActivityChainFactory} 串联执行。
 *
 * @author zhaoy
 * @date 2026/10/25
 * @description 下单规则过滤接口
 */
public interface IActionChain extends IActionChainArmory {

    /**
     * 执行下单动作校验（责任链节点处理逻辑）
     *
     * @param activitySkuEntity   活动商品（SKU）实体
     * @param activityEntity      活动实体
     * @param activityCountEntity 活动次数配置实体
     * @return 校验结果：true 通过 / false 不通过
     */
    boolean action(ActivitySkuEntity activitySkuEntity,
                   ActivityEntity activityEntity,
                   ActivityCountEntity activityCountEntity);

}
