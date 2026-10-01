package com.zhaoyushi.domain.strategy.service.rule.tree.impl;

import com.zhaoyushi.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.zhaoyushi.domain.strategy.service.rule.tree.ILogicTreeNode;
import com.zhaoyushi.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author zhaoy
 * @date 2026/9/30
 * @description 兜底奖励节点
 * <p>决策树的叶子节点，作为整棵树的兜底：无论上游如何走向，最终在此产出保底奖品，
 * 保证抽奖流程一定能返回一个奖品结果。通过 {@code @Component("rule_luck_award")} 注册，
 * Bean 名与节点 ruleKey 保持一致。
 *
 * <p>当前为骨架占位实现：固定返回接管（TAKE_OVER）并硬编码兜底奖品 awardId=101。
 */
@Slf4j
@Component("rule_luck_award")
public class RuleLuckAwardLogicTreeNode implements ILogicTreeNode {

    /**
     * 执行兜底奖励逻辑
     * <p>当前占位实现：固定返回接管（TAKE_OVER），并携带兜底奖品数据（awardId=101，规则值 "1,100"）。
     *
     * @param userId     用户ID
     * @param strategyId 策略ID
     * @param awardId    候选奖品ID
     * @return 携带兜底奖品的节点判定结果
     */
    @Override
    public DefaultTreeFactory.TreeActionEntity logic(String userId, Long strategyId, Integer awardId) {
        return DefaultTreeFactory.TreeActionEntity.builder()
                .ruleLogicCheckTypeVO(RuleLogicCheckTypeVO.TAKE_OVER)
                .strategyAwardData(DefaultTreeFactory.StrategyAwardData.builder()
                        .awardId(101)
                        .awardRuleValue("1,100")
                        .build())
                .build();
    }

}
