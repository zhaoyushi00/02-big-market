package com.zhaoyushi.domain.strategy.service.rule.tree.impl;

import com.zhaoyushi.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.zhaoyushi.domain.strategy.service.rule.tree.ILogicTreeNode;
import com.zhaoyushi.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import com.zhaoyushi.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

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
     * <p>解析规则值得到兜底奖品（冒号前为兜底奖品ID，冒号后为奖品规则值），
     * 返回接管（TAKE_OVER）并携带兜底奖品数据，保证抽奖流程一定返回奖品。
     *
     * @param userId      用户ID
     * @param strategyId  策略ID
     * @param awardId     候选奖品ID
     * @param ruleValue   规则配置值（兜底奖品配置，如 "101:1,100"）
     * @param endDateTime 活动结束时间（本节点不直接使用，仅为接口统一签名透传）
     * @return 携带兜底奖品的节点判定结果
     */
    @Override
    public DefaultTreeFactory.TreeActionEntity logic(String userId, Long strategyId, Integer awardId, String ruleValue, Date endDateTime) {
        log.info("规则过滤-兜底奖品 userId:{} strategyId:{} awardId:{} ruleValue:{}", userId, strategyId, awardId, ruleValue);
        String[] split = ruleValue.split(Constants.COLON);
        if (split.length == 0) {
            log.error("规则过滤-兜底奖品，兜底奖品未配置告警 userId:{} strategyId:{} awardId:{}", userId, strategyId, awardId);
            throw new RuntimeException("兜底奖品未配置 " + ruleValue);
        }
        // 兜底奖励配置
        Integer luckAwardId = Integer.valueOf(split[0]);
        String awardRuleValue = split.length > 1 ? split[1] : ""; // 解析兜底奖品规则值（冒号后的部分，如 "1,100"），未配置时置空
//        String awardRuleValue;
//        if (split.length > 1) {
//            awardRuleValue = split[1];
//        } else {
//            awardRuleValue = "";
//        }
        // 返回兜底奖品
        log.info("规则过滤-兜底奖品 userId:{} strategyId:{} awardId:{} awardRuleValue:{}", userId, strategyId, luckAwardId, awardRuleValue);

        return DefaultTreeFactory.TreeActionEntity.builder()
                .ruleLogicCheckTypeVO(RuleLogicCheckTypeVO.TAKE_OVER)
                .strategyAwardVO(DefaultTreeFactory.StrategyAwardVO.builder()
                        .awardId(luckAwardId)
                        .awardRuleValue(awardRuleValue)
                        .build())
                .build();
    }

}
