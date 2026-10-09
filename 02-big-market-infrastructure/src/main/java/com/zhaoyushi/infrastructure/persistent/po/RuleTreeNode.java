package com.zhaoyushi.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;

/**
 * 规则树节点持久化对象（PO）
 * <p>对应数据库表 rule_tree_node，存储决策树中的每一个节点。
 * 每个节点通过 ruleKey 唯一标识，ruleKey 需与 Spring 容器中对应的逻辑节点 Bean 名保持一致
 * （如 {@code @Component("rule_lock")}），引擎执行时按 ruleKey 匹配并调用该逻辑节点的判断逻辑。
 * ruleValue 为该节点规则的配置参数，如 "1" 表示抽奖 1 次后解锁。
 *
 * @author zhaoy
 * @date 2026/10/2
 * @description 规则树节点表
 */
@Data
public class RuleTreeNode {

    /** 自增主键ID */
    private Long id;
    /** 规则树ID：该节点所属的决策树标识 */
    private String treeId;
    /** 规则Key：节点唯一标识，需与逻辑节点 Bean 名保持一致（如 rule_lock） */
    private String ruleKey;
    /** 规则描述：该节点规则的业务含义说明 */
    private String ruleDesc;
    /** 规则比值：节点规则的配置参数（字符串），如 "1" 表示抽奖 1 次后解锁 */
    private String ruleValue;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;

}
