package com.zhaoyushi.infrastructure.persistent.dao;

import com.zhaoyushi.infrastructure.persistent.po.Strategy;
import com.zhaoyushi.infrastructure.persistent.po.StrategyAward;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/*抽奖策略Dao*/
@Mapper
public interface IStrategyDao {

    /**
     * 查询策略列表
     *
     * @return 策略列表
     */
    List<Strategy> queryStrategyList();

    /**
     * 按策略ID查询策略
     *
     * @param strategyId 策略ID
     * @return 策略信息
     */
    Strategy queryStrategyByStrategyId(Long strategyId);
}
