package com.zhaoyushi.domain.strategy.service.rule.tree.impl;

import com.zhaoyushi.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.zhaoyushi.domain.strategy.model.valobj.StrategyAwardStockKeyVO;
import com.zhaoyushi.domain.strategy.repository.IStrategyRepository;
import com.zhaoyushi.domain.strategy.service.armory.IStrategyDispatch;
import com.zhaoyushi.domain.strategy.service.rule.tree.ILogicTreeNode;
import com.zhaoyushi.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import jakarta.annotation.Resource;
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

    @Resource
    private IStrategyDispatch strategyDispatch;
    @Resource
    private IStrategyRepository strategyRepository;

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
    public DefaultTreeFactory.TreeActionEntity logic(String userId, Long strategyId, Integer awardId, String ruleValue) {

        log.info("规则过滤-库存扣减 userId:{} strategyId:{} awardId:{}", userId, strategyId, awardId);
        // 扣减库存
        Boolean status = strategyDispatch.subtractionAwardStock(strategyId, awardId);
        if (status){
            // 写入延时队列，延迟消费更新数据库记录【在trigger的job：UpdateAwardStockJob下消费队列，更新数据库记录】
            strategyRepository.awardStockConsumeSendQueue(StrategyAwardStockKeyVO.builder()
                            .awardId(awardId).strategyId(strategyId).build());

            return DefaultTreeFactory.TreeActionEntity.builder()
                    .ruleLogicCheckTypeVO(RuleLogicCheckTypeVO.TAKE_OVER)
                    .strategyAwardVO(DefaultTreeFactory.StrategyAwardVO.builder()
                            .awardId(awardId)
                            .awardRuleValue("")
                            .build())
                    .build();
        }

        return DefaultTreeFactory.TreeActionEntity.builder()
                .ruleLogicCheckTypeVO(RuleLogicCheckTypeVO.ALLOW)
                .build();
    }

}
