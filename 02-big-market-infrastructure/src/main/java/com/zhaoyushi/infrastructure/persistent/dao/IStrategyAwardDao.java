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

    /**
     * 按策略ID和奖品ID查询该奖品配置的规则模型字符串
     *
     * @param strategyAward 查询条件（含 strategyId、awardId）
     * @return 规则模型字符串，如 "tree_lock"；未配置时可能为 null
     */
    String queryStrategyAwardRuleModel(StrategyAward strategyAward);

    /**
     * 按策略ID和奖品ID查询该奖品配置的规则模型字符串（同 queryStrategyAwardRuleModel，供判空版本仓储方法使用）
     *
     * @param strategyAward 查询条件（含 strategyId、awardId）
     * @return 规则模型字符串，如 "tree_lock"；未配置时可能为 null
     */
    String queryStrategyAwardRuleModels(StrategyAward strategyAward);

    void updateStrategyAwardStock(StrategyAward strategyAward);

    StrategyAward queryStrategyAward(StrategyAward strategyAwardReq);
}
