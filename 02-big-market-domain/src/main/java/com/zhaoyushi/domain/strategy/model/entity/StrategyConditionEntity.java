package com.zhaoyushi.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 策略条件实体 - 查询策略的过滤条件
 *
 * <p>用途：承载「按用户(userId)+策略(strategyId)」查询策略/结果的筛选条件。
 * <p>业务位置：策略查询的入参对象（早期版本使用）。
 *
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @create 2023-12-23 09:10
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyConditionEntity {

    /** 用户ID */
    private String userId;
    /** 策略ID */
    private Integer strategyId;

}
