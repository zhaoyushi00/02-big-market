package com.zhaoyushi.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;

/**
 * 规则树持久化对象（PO）
 * <p>对应数据库表 rule_tree，存储决策树的主表元信息：树ID、名称、描述与根节点 key。
 * 一棵规则树由「根节点 + 若干节点(rule_tree_node) + 若干连线(rule_tree_node_line)」组成，
 * 本表只记录树级别信息，节点与连线分别存储在下属两张子表中，供决策树引擎遍历执行时定位。
 *
 * @author zhaoy
 * @date 2026/10/2
 * @description 规则树表
 */
@Data
public class RuleTree {

    /** 自增主键ID */
    private Long id;
    /** 规则树ID：决策树唯一标识，形如 "tree_lock" */
    private String treeId;
    /** 规则树名称：决策树名称，便于日志与可读性 */
    private String treeName;
    /** 规则树描述：决策树业务说明 */
    private String treeDesc;
    /** 规则根节点：决策树的起始节点 key，引擎从这里开始遍历 */
    private String treeRootRuleKey;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;

}
