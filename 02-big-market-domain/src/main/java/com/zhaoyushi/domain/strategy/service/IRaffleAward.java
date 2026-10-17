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

}
