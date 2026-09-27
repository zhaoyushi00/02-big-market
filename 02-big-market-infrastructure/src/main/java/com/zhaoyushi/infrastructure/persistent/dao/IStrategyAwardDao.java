package com.zhaoyushi.infrastructure.persistent.dao;

import com.zhaoyushi.infrastructure.persistent.po.Award;
import com.zhaoyushi.infrastructure.persistent.po.StrategyAward;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/*策略奖品明细配置DAO*/
@Mapper
public interface IStrategyAwardDao {

    /**
     * 查询策略奖品列表（测试用）
     *
     * @return 策略奖品列表
     */
    List<StrategyAward> queryStrategyAwardList();

    /**
     * 按策略ID查询该策略下的奖品明细列表
     *
     * @param strategyId 策略ID
     * @return 策略奖品列表
     */
    List<StrategyAward> queryStrategyAwardListByStrategyId(Long strategyId);
}
