package com.zhaoyushi.domain.strategy.service.rule.tree;

import com.zhaoyushi.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;

/**
 * @author zhaoy
 * @date 2026/9/30
 * @description 规则树节点逻辑接口
 * <p>每个实现类代表决策树中的一个规则节点，通过 {@code @Component("ruleKey")} 注册为 Spring Bean，
 * Bean 名即节点的 ruleKey。引擎根据当前节点 ruleKey 从节点集合中取出对应实现，调用 logic()
 * 得到判定结果与奖品数据，再据此决定下一步走向。
 *
 * <p>实现类 Bean 名必须与 RuleTreeNodeVO.ruleKey 保持一致，否则引擎无法通过
 * logicTreeNodeGroup.get(ruleKey) 定位到对应节点。
 *
 * <p>典型实现（次数锁节点，固定放行）：
 * <pre>
 * {@code @Component("rule_lock")}
 * public class RuleLockLogicTreeNode implements ILogicTreeNode {
 *     public TreeActionEntity logic(String userId, Long strategyId, Integer awardId) {
 *         return TreeActionEntity.builder()
 *                 .ruleLogicCheckTypeVO(RuleLogicCheckTypeVO.ALLOW)
 *                 .build();
 *     }
 * }
 * </pre>
 */
public interface ILogicTreeNode {

    /**
     * 执行当前节点的规则判断逻辑
     * <p>根据用户、策略、候选奖品等入参，产出本节点的判定结果（放行 ALLOW / 接管 TAKE_OVER），
     * 若为接管节点（如兜底奖励）还需返回最终奖品数据 strategyAwardData。
     * 返回的 TreeActionEntity 封装了判断结果 ruleLogicCheckTypeVO 和奖品数据 strategyAwardData。
     *
     * @param userId     用户ID
     * @param strategyId 策略ID
     * @param awardId    候选奖品ID
     * @return 节点判定结果与奖品数据封装
     */
    DefaultTreeFactory.TreeActionEntity logic(String userId, Long strategyId, Integer awardId);

}
