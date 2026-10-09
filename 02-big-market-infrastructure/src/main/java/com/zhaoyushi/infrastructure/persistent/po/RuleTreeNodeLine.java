package com.zhaoyushi.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;

/**
 * 规则树节点连线持久化对象（PO）
 * <p>对应数据库表 rule_tree_node_line，存储决策树中「父节点 → 子节点」的一条有向连线。
 * 引擎执行到某节点后，依据该节点判定结果（ruleLimitValue，如 ALLOW/TAKE_OVER）与连线的
 * 限定类型（ruleLimitType，如 EQUAL）做匹配，命中某条连线后即跳转到 ruleNodeTo 指向的下一个节点。
 *
 * <p>注意：ruleLimitType / ruleLimitValue 在库表中存储的是枚举名字符串（如 "EQUAL"、"ALLOW"），
 * 转换到领域层时通过 {@code RuleLimitTypeVO.valueOf(...)}、{@code RuleLogicCheckTypeVO.valueOf(...)} 还原为枚举。
 *
 * @author zhaoy
 * @date 2026/10/2
 * @description 规则树节点连线表
 */
@Data
public class RuleTreeNodeLine {

    /** 自增主键ID */
    private Long id;
    /** 规则树ID：该连线所属的决策树标识 */
    private String treeId;
    /** 规则Key节点 From：连线起点节点标识 */
    private String ruleNodeFrom;
    /** 规则Key节点 To：连线终点节点标识，命中后跳转到该节点 */
    private String ruleNodeTo;
    /** 限定类型：连线走通的比较方式，枚举名（EQUAL/GT/LT/GE/LE/ENUM） */
    private String ruleLimitType;
    /** 限定值：节点判定结果需匹配的值（ALLOW/TAKE_OVER），匹配上才走通该连线 */
    private String ruleLimitValue;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;

}
