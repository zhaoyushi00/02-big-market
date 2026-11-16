package com.zhaoyushi.domain.rebate.service;

import com.zhaoyushi.domain.rebate.model.entity.BehaviorEntity;

import java.util.List;

/**
 * 行为返利服务接口（领域层）
 *
 * <p>对外提供「行为返利入账」能力：上游（签到、支付等业务）触发行为后，
 * 调用本接口完成返利订单创建与发奖消息任务的编排。
 *
 * @author zhaoy
 * @date 2026/11/14
 * @description 行为返利服务接口
 */
public interface IBehaviorRebateService {

    /**
     * 创建行为动作的入账订单
     *
     * <p>根据行为类型查询返利配置，为每条配置生成一笔返利订单并投递发奖 MQ 消息。
     *
     * @param behaviorEntity 行为实体对象（含用户 ID、行为类型、外部业务单号）
     * @return 生成的返利订单 ID 集合（无可用配置时返回空列表）
     */
    List<String> createOrder(BehaviorEntity behaviorEntity);

}
