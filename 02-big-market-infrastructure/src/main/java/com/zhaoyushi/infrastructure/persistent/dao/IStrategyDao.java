package com.zhaoyushi.infrastructure.persistent.dao;

import com.zhaoyushi.infrastructure.persistent.po.Strategy;
import com.zhaoyushi.infrastructure.persistent.po.StrategyAward;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/*抽奖策略Dao*/
@Mapper
public interface IStrategyDao {

    List<Strategy> queryStrategyList();

}
