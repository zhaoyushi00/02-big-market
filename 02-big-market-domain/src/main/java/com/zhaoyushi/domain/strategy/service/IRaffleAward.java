package com.zhaoyushi.domain.strategy.service;

import com.zhaoyushi.domain.strategy.model.entity.StrategyAwardEntity;

import java.util.List;

/**
 * @author zhaoy
 * @date 2026/10/14
 * @description 策略奖品接口
 */
public interface IRaffleAward {

    /**
     * 根据策略ID查询抽奖奖品列表配置
     * @param strategyId
     * @return 奖品列表
     */
    List<StrategyAwardEntity> queryRaffleStrategyAwardList(Long strategyId);
    /**
     * 根据活动ID查询抽奖奖品列表配置
     * <p>活动与策略为 1:1 绑定，内部先按活动 ID 反查策略 ID，再查询该策略下的奖品列表。
     * 相比按策略 ID 查询，此接口更贴合前端「按活动维度」的调用习惯。
     *
     * @param activityId 活动ID
     * @return 奖品列表
     */
    List<StrategyAwardEntity> queryRaffleStrategyAwardListByActivityId(Long activityId);

}
