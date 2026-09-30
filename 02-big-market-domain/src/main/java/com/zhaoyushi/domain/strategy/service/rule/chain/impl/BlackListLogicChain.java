package com.zhaoyushi.domain.strategy.service.rule.chain.impl;

import com.zhaoyushi.domain.strategy.repository.IStrategyRepository;
import com.zhaoyushi.domain.strategy.service.rule.chain.AbstractLogicChain;
import com.zhaoyushi.types.common.Constants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("rule_blacklist")
public class BlackListLogicChain extends AbstractLogicChain {

    @Resource
    protected IStrategyRepository repository;

    @Override
    public Integer logic(String userId, Long strategyId) {
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
                return awardId;
            }
        }

        // 过滤其他责任链
        log.info("抽奖责任链 - 黑名单放行 userId:{}, strategyId:{}, ruleModel:{}", userId, strategyId, ruleModel());
        return next().logic(userId, strategyId);
    }

    @Override
    protected String ruleModel() {
        return "rule_blacklist";
    }
}
