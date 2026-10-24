package com.zhaoyushi.domain.activity.service;

import com.alibaba.fastjson.JSON;
import com.zhaoyushi.domain.activity.model.entity.*;
import com.zhaoyushi.domain.activity.repository.IActivityRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象活动服务类，定义标准的下单流程（模板方法）
 *
 * <p>实现 {@link IRaffleOrder}，以模板方法固化“创建活动订单”的标准步骤：
 * 按 SKU → 活动 → 次数配置 的顺序依次查询，为后续初始化账户、生成订单做准备。
 *
 * <p>关键点：
 * 1. 通过构造器注入仓储，子类可复用该流程；
 * 2. 当前为流程骨架，订单构建逻辑待完善。
 *
 * @author zhaoy
 * @date 2026/10/23
 * @description 抽象活动抽象类，定义标准的流程
 */
@Slf4j
public abstract class AbstractRaffleActivity implements IRaffleOrder {

    /** 活动仓储：领域层定义的查询端口，由基础设施层实现 */
    protected IActivityRepository activityRepository;

    /**
     * 构造器注入活动仓储
     *
     * @param activityRepository 活动仓储实现
     */
    public AbstractRaffleActivity(IActivityRepository activityRepository){
        this.activityRepository = activityRepository;
    }


    /**
     * 以 sku 创建抽奖活动订单，获得参与抽奖资格（可消耗的次数）
     *
     * @param activityShopCartEntity 活动购物车实体（用户ID + 商品SKU）
     * @return 活动订单实体（当前为流程骨架，返回空订单，待完善）
     */
    @Override
    public ActivityOrderEntity createRaffleActivityOrder(ActivityShopCartEntity activityShopCartEntity) {
        // 1. 通过sku查询活动商品信息，拿到  活动ID 与  次数配置ID
        ActivitySkuEntity activitySkuEntity = activityRepository.queryActivitySku(activityShopCartEntity.getSku());
        // 2. 根据  活动ID  查询活动信息，拿到活动名称、时间、策略、状态
        ActivityEntity activityEntity = activityRepository.queryRaffleActivityByActivityId(activitySkuEntity.getActivityId());
        // 3. 根据  次数配置ID  查询次数信息（用户在活动上可参与的总/日/月次数）
        ActivityCountEntity activityCountEntity = activityRepository.queryRaffleActivityCountByActivityCountId(activitySkuEntity.getActivityCountId());

        // 打印查询结果，便于调试观察三份数据的组装情况
        log.info("查询结果：{} {} {}", JSON.toJSONString(activitySkuEntity), JSON.toJSONString(activityEntity), JSON.toJSONString(activityCountEntity));

        // TODO 待完善：初始化账户次数、生成订单，当前仅返回空订单骨架
        return ActivityOrderEntity.builder().build();
    }
}
