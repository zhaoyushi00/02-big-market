package com.zhaoyushi.domain.strategy.service.rule.tree.factory.engine.impl;

import com.zhaoyushi.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.zhaoyushi.domain.strategy.model.valobj.RuleTreeNodeLineVO;
import com.zhaoyushi.domain.strategy.model.valobj.RuleTreeNodeVO;
import com.zhaoyushi.domain.strategy.model.valobj.RuleTreeVO;
import com.zhaoyushi.domain.strategy.service.rule.tree.ILogicTreeNode;
import com.zhaoyushi.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import com.zhaoyushi.domain.strategy.service.rule.tree.factory.engine.IDecisionTreeEngine;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * @author zhaoy
 * @date 2026/9/30
 * @description 决策树引擎
 * <p>决策树的执行器，由 {@link DefaultTreeFactory#openLogicTree} 创建，每次打开一棵树 new 一个实例。
 * 职责：从根节点开始逐级遍历，每到一个节点调用其 logic() 得到判定结果，再用判定结果的
 * code 与当前节点的连线做匹配，命中连线即跳到下一节点，直到叶子节点（无连线）结束。
 *
 * <p>遍历示例（树结构：rule_lock 判定 ALLOW 走 rule_stock，rule_stock 判定 TAKE_OVER 走 rule_luck_award）：
 * <pre>
 * rule_lock(ALLOW) → rule_stock(TAKE_OVER) → rule_luck_award(叶子，产出奖品)
 * </pre>
 */
@Slf4j
public class DecisionTreeEngine implements IDecisionTreeEngine {

    /** 规则树节点集合：key 为节点 ruleKey，value 为对应节点逻辑实现 */
    private final Map<String, ILogicTreeNode> logicTreeNodeGroup;

    /** 本次要执行的决策树结构数据 */
    private final RuleTreeVO ruleTreeVO;

    /**
     * 构造决策树引擎
     *
     * @param logicTreeNodeGroup 规则树节点集合（由工厂传入）
     * @param ruleTreeVO         决策树结构数据
     */
    public DecisionTreeEngine(Map<String, ILogicTreeNode> logicTreeNodeGroup, RuleTreeVO ruleTreeVO) {
        this.logicTreeNodeGroup = logicTreeNodeGroup;
        this.ruleTreeVO = ruleTreeVO;
    }

    /**
     * 执行整棵决策树
     * <p>从根节点开始，循环执行「取节点 → 执行 logic() → 记录结果 → 匹配连线找下一节点」，
     * 直到 nextNode 为 null（到达叶子节点）时结束，返回最终奖品数据。
     *
     * @param userId     用户ID
     * @param strategyId 策略ID
     * @param awardId    候选奖品ID
     * @return 最终奖品数据，若整棵树无节点接管则为 null
     */
    @Override
    public DefaultTreeFactory.StrategyAwardData process(String userId, Long strategyId, Integer awardId) {

        // 最终奖品数据，随遍历被叶子节点覆盖
        DefaultTreeFactory.StrategyAwardData strategyAwardData = null;

        // 1. 获取基础信息：根节点 key 与整棵树的节点映射
        String nextNode = ruleTreeVO.getTreeRootRuleNode();
        Map<String, RuleTreeNodeVO> treeNodeMap = ruleTreeVO.getTreeNodeMap();

        // 2. 取出起始节点（根节点记录了第一个要执行的规则）
        RuleTreeNodeVO ruleTreeNode = treeNodeMap.get(nextNode);
        while (null != nextNode){
            // 3. 用当前节点的 ruleKey 从节点组里取出对应的逻辑实现（如 rule_lock → RuleLockLogicTreeNode）
            ILogicTreeNode logicTreeNode = logicTreeNodeGroup.get(ruleTreeNode.getRuleKey());

            // 4. 执行节点判断逻辑，得到判定结果与奖品数据
            DefaultTreeFactory.TreeActionEntity logicEntity = logicTreeNode.logic(userId, strategyId, awardId);
            RuleLogicCheckTypeVO ruleLogicCheckTypeVO = logicEntity.getRuleLogicCheckTypeVO();
            strategyAwardData = logicEntity.getStrategyAwardData();
            log.info("决策树引擎【{}】treeId:{} node:{} code:{}", ruleTreeVO.getTreeName(), ruleTreeVO.getTreeId(), nextNode, ruleLogicCheckTypeVO.getCode());

            // 5. 用判定结果 code 匹配连线，得到下一节点 key，再取出下一节点对象
            nextNode = nextNode(ruleLogicCheckTypeVO.getCode(), ruleTreeNode.getTreeNodeLineVOList());
            ruleTreeNode = treeNodeMap.get(nextNode);

        }
        // 6. 返回最终结果
        return strategyAwardData;

    }

    /**
     * 根据当前节点判定结果匹配连线，得到下一节点 key
     * <p>遍历当前节点的所有连线，用判定结果 code 与连线限定值做比较，
     * 命中第一条满足条件的连线即返回其 ruleNodeTo。
     *
     * @param matterValue            当前节点判定结果的 code（ALLOW=0000 / TAKE_OVER=0001）
     * @param ruleTreeNodeLineVOList 当前节点的连线列表（叶子节点为 null）
     * @return 下一节点 key，叶子节点或未命中时返回 null
     */
    private String nextNode(String matterValue, List<RuleTreeNodeLineVO> ruleTreeNodeLineVOList){
        // 叶子节点（无连线）直接返回 null，结束遍历
        if (null == ruleTreeNodeLineVOList || ruleTreeNodeLineVOList.isEmpty()) return null;
        for(RuleTreeNodeLineVO nodeLine : ruleTreeNodeLineVOList){
            if (decisionLogic(matterValue, nodeLine)){
                return nodeLine.getRuleNodeTo();
            }
        }
        throw new RuntimeException("决策树引擎， nextNode 计算失败，未找到可执行节点");
    }

    /**
     * 判断当前判定结果是否满足某条连线的限定条件
     * <p>依据连线的 ruleLimitType（比较方式）对 matterValue 与连线限定值做比较。
     * 当前仅实现 EQUAL（相等比较），其余比较类型暂未实现，返回 false。
     *
     * @param matterValue 当前节点判定结果的 code
     * @param nodeLine    待判断的连线
     * @return 满足连线条件返回 true，否则 false
     */
    public boolean decisionLogic(String matterValue, RuleTreeNodeLineVO nodeLine) {
        switch (nodeLine.getRuleLimitType()) {
            case EQUAL:
                return matterValue.equals(nodeLine.getRuleLimitValue().getCode());
            // 以下规则暂时不需要实现
            case GT:
            case LT:
            case GE:
            case LE:
            default:
                return false;
        }
    }

}
