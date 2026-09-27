package com.zhaoyushi.domain.strategy.model.entity;

import lombok.Data;

/**
 * 规则物料实体对象 - 规则过滤的入参
 *
 * <p>用途：承载执行一条规则过滤所需的必要参数，由调用方组装后传给 {@code ILogicFilter.filter}。
 * <p>注意：ruleModel 为策略级规则（如 rule_weight）时，awardId 无需传入（保持 null）。
 *
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @create 2024-01-06 09:56
 */
@Data
public class RuleMatterEntity {

    /** 用户ID */
    private String userId;
    /** 策略ID */
    private Long strategyId;
    /** 抽奖奖品ID【规则类型为策略，则不需要奖品ID】 */
    private Integer awardId;
    /** 抽奖规则类型【rule_random - 随机值计算、rule_lock - 抽奖几次后解锁、rule_luck_award - 幸运奖(兜底奖品)】 */
    private String ruleModel;

}
