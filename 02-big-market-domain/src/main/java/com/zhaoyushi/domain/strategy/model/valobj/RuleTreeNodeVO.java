package com.zhaoyushi.domain.strategy.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 规则树节点值对象
 * <p>决策树中的一个节点，通过 ruleKey 唯一标识，并持有从该节点伸出的所有连线。
 * 引擎通过 ruleKey 从 Spring 容器中匹配同名逻辑节点 Bean（如 {@code @Component("rule_lock")}），
 * 调用其 logic() 得到判定结果后，结合 treeNodeLineVOList 中的连线决定下一跳。
 *
 * <p>叶子节点（如兜底奖励节点 rule_luck_award）的 treeNodeLineVOList 为 null，
 * 表示没有下一跳，执行到该节点即结束整棵树的遍历。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleTreeNodeVO {

    /** 规则树ID：该节点所属的决策树标识 */
    private Integer treeId;
    /** 规则Key：节点唯一标识，需与对应逻辑节点 Bean 名保持一致（如 rule_lock） */
    private String ruleKey;
    /** 规则描述：该节点规则的业务含义说明 */
    private String ruleDesc;
    /** 规则比值：节点规则的配置参数（字符串），如 "1" 表示抽奖 1 次后解锁 */
    private String ruleValue;

    /** 规则连线：从该节点伸出的所有连线，叶子节点为 null */
    private List<RuleTreeNodeLineVO> treeNodeLineVOList;

}
