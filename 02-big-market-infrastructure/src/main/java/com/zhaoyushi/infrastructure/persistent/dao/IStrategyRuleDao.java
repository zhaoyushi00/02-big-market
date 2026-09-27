package com.zhaoyushi.infrastructure.persistent.dao;

import com.zhaoyushi.infrastructure.persistent.po.StrategyAward;
import com.zhaoyushi.infrastructure.persistent.po.StrategyRule;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/*策略规则Dao*/
@Mapper
public interface IStrategyRuleDao {

    /**
     * 查询策略规则列表（测试用）
     *
     * @return 策略规则列表
     */
    List<StrategyRule> queryStrategyRuleList();

    /**
     * 按策略ID和规则模型查询规则
     *
     * @param strategyRuleReq 查询条件（strategyId、ruleModel）
     * @return 规则信息
     */
    StrategyRule queryStrategyRule(StrategyRule strategyRuleReq);

    /**
     * 按策略ID、规则模型（可选奖品ID）查询规则值
     *
     * @param strategyRule 查询条件（strategyId、ruleModel、awardId 可选）
     * @return 规则值字符串
     */
    String queryStrategyRuleValue(StrategyRule strategyRule);
}
