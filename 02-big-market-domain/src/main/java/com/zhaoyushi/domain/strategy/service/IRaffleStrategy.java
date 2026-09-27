package com.zhaoyushi.domain.strategy.service;

// 抽奖策略接口

import com.zhaoyushi.domain.strategy.model.entity.RaffleAwardEntity;
import com.zhaoyushi.domain.strategy.model.entity.RaffleFactorEntity;

public interface IRaffleStrategy {

    /**
     * 执行抽奖
     * <p>根据抽奖因子（用户+策略）执行完整抽奖流程，返回抽中的奖品。
     *
     * @param raffleFactorEntity 抽奖因子（用户ID、策略ID）
     * @return 抽中的奖品信息
     */
    RaffleAwardEntity performRaffle(RaffleFactorEntity raffleFactorEntity);

}
