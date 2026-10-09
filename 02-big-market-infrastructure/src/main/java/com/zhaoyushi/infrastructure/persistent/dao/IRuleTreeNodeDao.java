package com.zhaoyushi.infrastructure.persistent.dao;

import com.zhaoyushi.infrastructure.persistent.po.RuleTreeNode;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 规则树节点表数据访问接口（DAO）
 * <p>对应数据库表 rule_tree_node，提供决策树全部节点的查询能力，
 * 供规则树仓储（StrategyRepository）组装 RuleTreeNodeVO 时读取节点列表。
 *
 * @author zhaoy
 * @date 2026/10/2
 * @description 规则树节点表DAO
 */
@Mapper
public interface IRuleTreeNodeDao {

    /**
     * 根据规则树ID查询规则树节点列表
     *
     * @param treeId 规则树ID，如 "tree_lock"
     * @return 规则树节点列表；若 treeId 不存在则返回空列表
     */
    List<RuleTreeNode> queryRuleTreeNodeListByTreeId(String treeId);

}
