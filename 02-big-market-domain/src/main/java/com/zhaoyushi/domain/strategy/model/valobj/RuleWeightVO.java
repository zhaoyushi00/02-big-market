package com.zhaoyushi.domain.strategy.model.valobj;

import lombok.*;

import java.util.List;

/**
 * @author zhaoy
 * @date 2026/11/17
 * @description 权重规则值对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleWeightVO {

    // 原始规则值配置
    private String ruleValue;
    // 权重值
    private Integer weight;
    // 奖品配置
    private List<Integer> awardIds;
    // 奖品列表
    private List<Award> awardList;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Award {
        private Integer awardId;
        private String awardTitle;
    }

}
