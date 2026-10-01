package com.zhaoyushi.domain.strategy.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 规则树值对象
 * <p>描述一棵完整的决策树：包含根节点标识、节点映射等，供决策树引擎遍历执行。
 * 注意：该类虽然带有 treeId，但不作为数据库持久化实体，而是在内存中整体流转，
 * 因此被定义为值对象（不具备唯一ID、不需要改变数据库结果）。
 *
 * <p>treeNodeMap 以「节点 key → 节点对象」的形式组织，引擎据此快速定位任意节点。
 *
 * <p>组装示例：
 * <pre>
 * RuleTreeVO ruleTreeVO = new RuleTreeVO();
 * ruleTreeVO.setTreeId(100000001);
 * ruleTreeVO.setTreeRootRuleNode("rule_lock");
 * ruleTreeVO.setTreeNodeMap(Map.of(
 *         "rule_lock", rule_lock,
 *         "rule_stock", rule_stock,
 *         "rule_luck_award", rule_luck_award));
 * </pre>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleTreeVO {

    /** 规则树ID：决策树唯一标识 */
    private Integer treeId;
    /** 规则树名称：决策树名称，便于日志与可读性 */
    private String treeName;
    /** 规则树描述：决策树业务说明 */
    private String treeDesc;
    /** 规则根节点：决策树的起始节点 key，引擎从这里开始执行 */
    private String treeRootRuleNode;

    /** 规则节点：key(节点key) → 节点对象 的映射，引擎据此定位节点 */
    private Map<String, RuleTreeNodeVO> treeNodeMap;

}
