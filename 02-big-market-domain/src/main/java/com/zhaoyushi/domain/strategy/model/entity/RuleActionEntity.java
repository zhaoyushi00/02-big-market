package com.zhaoyushi.domain.strategy.model.entity;

import com.zhaoyushi.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import lombok.*;

/**
 * 规则动作实体 - 规则过滤器的统一返回结果
 *
 * <p>用途：承载一次规则过滤的执行结果，包含两部分：
 * <ul>
 *   <li>code/info：规则是否放行（ALLOW 放行 / TAKE_OVER 接管），决定后续流程走向；</li>
 *   <li>data（泛型 T）：规则命中所产生的具体数据，按抽奖阶段（前/中/后）区分类型。</li>
 * </ul>
 * <p>泛型约束 T extends RaffleEntity：限定 data 只能是「抽奖阶段实体」一族，保证类型安全。
 * <p>业务位置：{@code ILogicFilter.filter} 的返回类型，由抽奖主流程消费。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleActionEntity <T extends RuleActionEntity.RaffleEntity> {

    private String code = RuleLogicCheckTypeVO.ALLOW.getCode();
    private String info = RuleLogicCheckTypeVO.ALLOW.getInfo();

    // 命中的规则标识（rule_weight / rule_blacklist
    private String ruleModel;
    // 规则产生的具体数据，用泛型区分阶段  继承的是 RuleActionEntity.RaffleEntity
    private T data;


    static public class RaffleEntity{

    }

    // 抽奖之前
    @EqualsAndHashCode(callSuper = true)  //调用的父类 equals/hashCode
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    static public class RaffleBeforeEntity extends RaffleEntity{

        // 策略ID
        private Long strategyId;

        // 权重值Key；用于抽奖时可以选择权重抽奖
        private String ruleWeightValueKey;

        // 奖品ID；用于黑名单，不用抽奖，直接返回奖品完事
        private Integer awardId;

    }

    // 抽奖之中
    static public class RaffleCenterEntity extends RaffleEntity{

    }

    // 抽奖之后
    static public class RaffleAfterEntity extends RaffleEntity{

    }
}
