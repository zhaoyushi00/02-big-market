package com.zhaoyushi.domain.activity.service.quota;

import com.zhaoyushi.domain.activity.model.entity.ActivityCountEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivityEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivitySkuEntity;
import com.zhaoyushi.domain.activity.repository.IActivityRepository;
import com.zhaoyushi.domain.activity.service.quota.rule.factory.DefaultActivityChainFactory;

/**
 * 抽奖活动的支撑类
 *
 * <p>作为 {@link AbstractRaffleActivityAccountQuota} 的父类，收敛下单流程所需的公共依赖与查询方法：
 * 1. 持有活动仓储 {@link IActivityRepository} 与责任链工厂 {@link DefaultActivityChainFactory}；
 * 2. 提供 SKU/活动/次数配置三个查询方法，供模板方法 {@link AbstractRaffleActivityAccountQuota#createSkuRechargeOrder} 复用。
 *
 * @author zhaoy
 * @date 2026/10/25
 * @description 抽奖活动的支撑类
 */
public class RaffleActivityAccountQuotaSupport {

    /** 活动动作责任链工厂：用于获取下单前的规则校验责任链（基础信息校验 + SKU 库存校验） */
    protected DefaultActivityChainFactory defaultActivityChainFactory;

    /** 活动仓储：领域层定义的查询/持久化端口，由基础设施层实现 */
    protected IActivityRepository activityRepository;

    /**
     * 构造器：注入活动仓储与责任链工厂
     *
     * @param activityRepository          活动仓储实现
     * @param defaultActivityChainFactory 活动动作责任链工厂
     */
    public RaffleActivityAccountQuotaSupport(IActivityRepository activityRepository, DefaultActivityChainFactory defaultActivityChainFactory) {
        this.activityRepository = activityRepository;
        this.defaultActivityChainFactory = defaultActivityChainFactory;
    }

    /**
     * 根据 SKU 查询活动商品信息（活动ID + 次数配置ID + 库存）
     *
     * @param sku 商品 SKU 编号
     * @return 活动 SKU 实体
     */
    public ActivitySkuEntity queryActivitySku(Long sku) {
        return activityRepository.queryActivitySku(sku);
    }

    /**
     * 根据活动ID查询活动信息（名称、时间、策略、状态）
     *
     * @param activityId 活动ID
     * @return 活动实体
     */
    public ActivityEntity queryRaffleActivityByActivityId(Long activityId) {
        return activityRepository.queryRaffleActivityByActivityId(activityId);
    }

    /**
     * 根据次数配置编号查询次数配置（总/日/月次数上限）
     *
     * @param activityCountId 活动次数配置编号
     * @return 次数配置实体
     */
    public ActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId) {
        return activityRepository.queryRaffleActivityCountByActivityCountId(activityCountId);
    }


}
