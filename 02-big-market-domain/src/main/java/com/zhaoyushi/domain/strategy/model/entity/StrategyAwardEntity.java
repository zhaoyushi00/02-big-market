package com.zhaoyushi.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 策略奖品实体 - 策略配置下的奖品明细
 *
 * <p>用途：描述「某个策略(strategyId)下有哪些奖品(awardId)、各奖品库存与中奖概率」。
 * <p>业务位置：策略装配（armory）阶段查询奖品列表，用于构建抽奖概率区间与库存扣减。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyAwardEntity {

    /*抽奖策略ID*/
    private Long strategyId;
    /*抽奖奖品Id*/
    private Integer awardId;
    /*奖品库存总量*/
    private Integer awardCount;
    /*奖品库存剩余*/
    private Integer awardCountSurplus;
    /*奖品中奖概率*/
    private BigDecimal awardRate;

}
