package com.zhaoyushi.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 抽奖因子实体 - 抽奖的入参
 *
 * <p>用途：描述「谁(userId)在哪个策略(strategyId)下抽奖」，是发起抽奖的最小必要参数。
 * <p>业务位置：{@code IRaffleStrategy.performRaffle} 的入参类型。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RaffleFactorEntity {

    private String userId;
    private Long strategyId;
    /** 活动结束时间：由下单订单带入，透传到决策树的库存扣减节点，作为锁 Key 的过期时间基准 */
    private Date endDateTime;

}
