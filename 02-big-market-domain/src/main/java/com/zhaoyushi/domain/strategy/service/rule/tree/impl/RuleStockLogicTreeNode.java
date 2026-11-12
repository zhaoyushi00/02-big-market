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

import java.util.Date;

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
     * <p>对候选奖品执行库存扣减：扣减成功返回接管（TAKE_OVER），把扣减记录投递到延时队列异步回写数据库，
     * 并携带最终奖品数据；扣减失败（库存不足）返回放行（ALLOW），由后续节点兜底处理。
     *
     * @param userId      用户ID
     * @param strategyId  策略ID
     * @param awardId     候选奖品ID
     * @param ruleValue   节点规则值（本节点未使用）
     * @param endDateTime 活动结束时间，用于给扣减后抢占的锁 Key 设置过期时间
     * @return 扣减成功返回接管（含奖品数据），失败返回放行
     */
    @Override
    public DefaultTreeFactory.TreeActionEntity logic(String userId, Long strategyId, Integer awardId, String ruleValue, Date endDateTime) {

        log.info("规则过滤-库存扣减 userId:{} strategyId:{} awardId:{}", userId, strategyId, awardId);
        // 扣减库存（把活动结束时间透传，用于给锁 Key 设置过期时间）
        Boolean status = strategyDispatch.subtractionAwardStock(strategyId, awardId, endDateTime);
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
