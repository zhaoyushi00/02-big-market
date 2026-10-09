package com.zhaoyushi.domain.strategy.service.rule.chain.impl;

import com.zhaoyushi.domain.strategy.repository.IStrategyRepository;
import com.zhaoyushi.domain.strategy.service.rule.chain.AbstractLogicChain;
import com.zhaoyushi.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import com.zhaoyushi.types.common.Constants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("rule_blacklist")
public class BlackListLogicChain extends AbstractLogicChain {

    @Resource
    protected IStrategyRepository repository;

    /**
     * 黑名单责任链过滤
     * <p>查询黑名单规则值，若当前用户命中黑名单则直接返回黑名单指定奖品（接管），
     * 否则放行并交由下一个链节点继续处理。
     *
     * @param userId     用户ID
     * @param strategyId 策略ID
     * @return 命中的奖品结果；未命中时返回下一节点的处理结果
     */
    @Override
    public DefaultChainFactory.StrategyAwardVO logic(String userId, Long strategyId) {
        log.info("抽奖责任链 - 黑名单开始 userId:{} strategyId:{} ruleModel:{}", userId, strategyId, ruleModel());
        String ruleValue = repository.queryStrategyRuleValue(strategyId, ruleModel());
        String[] splitRuleValue = ruleValue.split(Constants.COLON);
        Integer awardId  = Integer.parseInt(splitRuleValue[0]);

        // 100: user001, user002, user003
        // 过滤其他规则
        String[] userBlackIds = splitRuleValue[1].split(Constants.SPLIT);
        for (String userBlackId : userBlackIds){
            if(userId.equals(userBlackId)){
                log.info("抽奖责任链 - 黑名单接管 userId:{}, strategyId:{}, ruleModel:{}", userId, strategyId, ruleModel());
                return DefaultChainFactory.StrategyAwardVO.builder()
                        .awardId(awardId)
                        .logicModel(ruleModel())
                        .build();
            }
        }

        // 过滤其他责任链
        log.info("抽奖责任链 - 黑名单放行 userId:{}, strategyId:{}, ruleModel:{}", userId, strategyId, ruleModel());
        return next().logic(userId, strategyId);
    }

    @Override
    protected String ruleModel() {
        return DefaultChainFactory.LogicModel.RULE_BLACKLIST.getCode();
    }
}
