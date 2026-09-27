package com.zhaoyushi.domain.strategy.service.rule;

// 抽奖规则过滤接口

import com.zhaoyushi.domain.strategy.model.entity.RuleActionEntity;
import com.zhaoyushi.domain.strategy.model.entity.RuleMatterEntity;

public interface ILogicFilter<T extends RuleActionEntity.RaffleEntity> {

    /**
     * 执行规则过滤
     * <p>根据规则物料（用户/策略/奖品/规则类型）执行一条规则，返回放行或接管的结果。
     *
     * @param ruleMatterEntity 规则物料（过滤所需参数）
     * @return 规则过滤结果（放行 ALLOW / 接管 TAKE_OVER）
     */
    RuleActionEntity<T> filter(RuleMatterEntity ruleMatterEntity);

}
