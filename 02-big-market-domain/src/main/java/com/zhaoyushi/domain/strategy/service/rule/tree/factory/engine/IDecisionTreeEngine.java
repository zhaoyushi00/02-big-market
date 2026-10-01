package com.zhaoyushi.domain.strategy.service.rule.tree.factory.engine;

import com.zhaoyushi.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;

/**
 * @author zhaoy
 * @date 2026/9/30
 * @description 决策树组合引擎接口
 * <p>决策树引擎的对外契约，负责从根节点开始，根据每个节点的判断结果（ALLOW/TAKE_OVER）
 * 和连线上的限定值，逐步走到下一个节点，直到叶子节点，最终产出奖品数据。
 *
 * <p>引擎由 {@link DefaultTreeFactory#openLogicTree} 创建，创建时注入全部节点实现与树结构数据。
 * 每次打开一棵树都会 new 一个引擎实例，互不干扰。
 */
public interface IDecisionTreeEngine {

    /**
     * 执行整棵决策树
     * <p>输入用户、策略、奖品，从根节点开始沿连线逐级向下遍历，最终返回 StrategyAwardData
     * （最终奖品 ID 和奖品规则值）。
     *
     * @param userId     用户ID
     * @param strategyId 策略ID
     * @param awardId    候选奖品ID
     * @return 最终奖品数据（奖品ID与奖品规则值），未接管时可能为 null
     */
    DefaultTreeFactory.StrategyAwardData process(String userId, Long strategyId, Integer awardId);

}
