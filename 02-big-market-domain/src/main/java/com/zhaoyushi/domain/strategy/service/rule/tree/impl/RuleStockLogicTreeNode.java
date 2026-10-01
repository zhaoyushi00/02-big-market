package com.zhaoyushi.domain.strategy.service.rule.tree.impl;

import com.zhaoyushi.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.zhaoyushi.domain.strategy.service.rule.tree.ILogicTreeNode;
import com.zhaoyushi.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author zhaoy
 * @date 2026/9/30
 * @description 库存节点
 * <p>决策树中的「库存」规则节点，用于处理奖品库存扣减等逻辑。
 * 通过 {@code @Component("rule_stock")} 注册，Bean 名与节点 ruleKey 保持一致。
 *
 * <p>当前为骨架占位实现，固定返回接管（TAKE_OVER），后续补充真实的库存扣减逻辑。
 */
@Slf4j
@Component("rule_stock")
public class RuleStockLogicTreeNode implements ILogicTreeNode {

    /**
     * 执行库存规则判断
     * <p>当前占位实现：固定返回接管（TAKE_OVER），交由引擎沿 TAKE_OVER 连线走向下一节点。
     *
     * @param userId     用户ID
     * @param strategyId 策略ID
     * @param awardId    候选奖品ID
     * @return 固定接管的节点判定结果
     */
    @Override
    public DefaultTreeFactory.TreeActionEntity logic(String userId, Long strategyId, Integer awardId) {
        return DefaultTreeFactory.TreeActionEntity.builder()
                .ruleLogicCheckTypeVO(RuleLogicCheckTypeVO.TAKE_OVER)
                .build();
    }

}
