package com.zhaoyushi.domain.strategy.service.armory;
/*
* 策略抽奖调度
* */
public interface IStrategyDispatch {
    /**
     * 获取抽奖策略装配的随机结果（不带权重，走总表）
     *
     * @param strategyId 策略ID
     * @return 抽奖结果（奖品ID）
     */
    Integer getRandomAwardId(Long strategyId);

    /**
     * 获取抽奖策略装配的随机结果（带权重，走对应权重档位子表）
     *
     * @param strategyId      策略ID
     * @param ruleWeightValue 权重档位值
     * @return 抽奖结果（奖品ID）
     */
    Integer getRandomAwardId(Long strategyId, String ruleWeightValue);
}
