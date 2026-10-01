package com.zhaoyushi.domain.strategy.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 规则树节点连线值对象
 * <p>描述决策树中「父节点 → 子节点」的一条有向连线，用于衔接 from → to 的节点链路关系。
 * 引擎执行到某节点后，依据该节点判定结果（{@link RuleLogicCheckTypeVO} 的 code）与连线的
 * 限定类型（{@link RuleLimitTypeVO}）、限定值（{@link RuleLogicCheckTypeVO}）做匹配，
 * 命中某条连线后即跳转到该连线 ruleNodeTo 指向的下一个节点。
 *
 * <p>连线示例：rule_lock 判定结果为 ALLOW(0000) 时，走 rule_lock → rule_stock 这条线：
 * <pre>
 * RuleTreeNodeLineVO.builder()
 *         .treeId(100000001)
 *         .ruleNodeFrom("rule_lock")
 *         .ruleNodeTo("rule_stock")
 *         .ruleLimitType(RuleLimitTypeVO.EQUAL)
 *         .ruleLimitValue(RuleLogicCheckTypeVO.ALLOW)
 *         .build();
 * </pre>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleTreeNodeLineVO {

    /** 规则树ID：该连线所属的决策树标识 */
    private Integer treeId;
    /** 规则Key节点 From：连线起点节点标识 */
    private String ruleNodeFrom;
    /** 规则Key节点 To：连线终点节点标识，命中后跳转到该节点 */
    private String ruleNodeTo;
    /** 限定类型；1:=;2:>;3:<;4:>=;5<=;6:enum[枚举范围]，决定这条线走通的比较方式 */
    private RuleLimitTypeVO ruleLimitType;
    /** 限定值（到下个节点）：当节点判定结果通过 ruleLimitType 运算匹配上该值时走通连线 */
    private RuleLogicCheckTypeVO ruleLimitValue;

}
