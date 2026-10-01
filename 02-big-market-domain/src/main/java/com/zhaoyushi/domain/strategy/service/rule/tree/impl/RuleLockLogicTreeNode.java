package com.zhaoyushi.domain.strategy.service.rule.tree.impl;

import com.zhaoyushi.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.zhaoyushi.domain.strategy.service.rule.tree.ILogicTreeNode;
import com.zhaoyushi.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author zhaoy
 * @date 2026/9/30
 * @description 次数锁节点
 * <p>决策树中的「次数锁」规则节点，用于限定用户完成 N 次抽奖后才解锁后续流程。
 * 通过 {@code @Component("rule_lock")} 注册，Bean 名与节点 ruleKey 保持一致。
 *
 * <p>当前为骨架占位实现，固定返回放行（ALLOW），后续补充真实的抽奖次数校验逻辑。
 */
@Slf4j
@Component("rule_lock")
public class RuleLockLogicTreeNode implements ILogicTreeNode {

    /**
     * 执行次数锁规则判断
     * <p>当前占位实现：固定返回放行（ALLOW），交由引擎沿 ALLOW 连线走向下一节点。
     *
     * @param userId     用户ID
     * @param strategyId 策略ID
     * @param awardId    候选奖品ID
     * @return 固定放行的节点判定结果
     */
    @Override
    public DefaultTreeFactory.TreeActionEntity logic(String userId, Long strategyId, Integer awardId) {
        return DefaultTreeFactory.TreeActionEntity.builder()
                .ruleLogicCheckTypeVO(RuleLogicCheckTypeVO.ALLOW)
                .build();
    }

}
