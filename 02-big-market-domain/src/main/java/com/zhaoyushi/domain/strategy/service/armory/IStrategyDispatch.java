package com.zhaoyushi.domain.strategy.service.armory;

import java.util.Date;

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

    /**
     * 扣减奖品缓存库存
     * <p>根据策略ID和奖品ID拼接库存缓存 Key，委托仓储层对 Redis 中的奖品库存执行原子扣减。
     * 额外传入活动结束时间，用于给扣减后抢占的锁 Key 设置过期时间。
     *
     * @param strategyId  策略ID
     * @param awardId     奖品ID
     * @param endDateTime 活动结束时间，可为 null（null 时锁 Key 不设过期时间）
     * @return 扣减成功返回 true；库存不足返回 false
     */
    Boolean subtractionAwardStock(Long strategyId, Integer awardId, Date endDateTime);
}
