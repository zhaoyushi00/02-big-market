package com.zhaoyushi.domain.strategy.service.rule.chain.impl;

import com.zhaoyushi.domain.strategy.service.armory.IStrategyDispatch;
import com.zhaoyushi.domain.strategy.service.rule.chain.AbstractLogicChain;
import com.zhaoyushi.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("default")
public class DefaultLogicChain extends AbstractLogicChain {

    @Resource
    protected IStrategyDispatch strategyDispatch;

    /**
     * 默认抽奖处理
     * <p>责任链兜底节点：直接按策略总表随机抽取奖品，作为默认逻辑模型结果返回。
     *
     * @param userId     用户ID
     * @param strategyId 策略ID
     * @return 默认逻辑模型命中的奖品结果
     */
    @Override
    public DefaultChainFactory.StrategyAwardVO logic(String userId, Long strategyId) {
        Integer awardId = strategyDispatch.getRandomAwardId(strategyId);
        log.info("抽奖责任链-默认处理 userId: {} strategyId: {} ruleModel: {} awardId: {}", userId, strategyId, ruleModel(), awardId);
        return DefaultChainFactory.StrategyAwardVO.builder()
                .awardId(awardId)
                .logicModel(ruleModel())
                .build();
    }

    @Override
    protected String ruleModel() {
        return DefaultChainFactory.LogicModel.RULE_DEFAULT.getCode();
    }
}
