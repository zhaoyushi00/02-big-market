package com.zhaoyushi.domain.strategy.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;
/**
 * 规则限定类型枚举（值对象）
 * <p>描述决策树中节点连线上「限定值」与「节点判定结果」之间的比较运算方式，
 * 用于判断当前节点执行结果能否满足连线条件、从而决定是否沿该连线走向下一节点。
 *
 * <p>使用位置：{@link RuleTreeNodeLineVO} 中 ruleLimitType 字段的取值，
 * 由 {@code DecisionTreeEngine.decisionLogic} 依据该枚举执行具体比较。
 *
 * <p>当前骨架仅实现 EQUAL（相等比较），GT/LT/GE/LE/ENUM 为预留占位。
 */
@Getter
@AllArgsConstructor
public enum RuleLimitTypeVO {

    /** 等于：判定结果与限定值完全相等时走通连线 */
    EQUAL(1, "等于"),
    /** 大于：判定结果大于限定值时走通连线 */
    GT(2, "大于"),
    /** 小于：判定结果小于限定值时走通连线 */
    LT(3, "小于"),
    /** 大于等于：判定结果大于或等于限定值时走通连线 */
    GE(4, "大于&等于"),
    /** 小于等于：判定结果小于或等于限定值时走通连线 */
    LE(5, "小于&等于"),
    /** 枚举：判定结果属于限定值枚举范围时走通连线 */
    ENUM(6, "枚举"),
    ;

    /** 限定类型编码（1:等于 2:大于 3:小于 4:大于等于 5:小于等于 6:枚举） */
    private final Integer code;
    /** 限定类型中文描述 */
    private final String info;

}
