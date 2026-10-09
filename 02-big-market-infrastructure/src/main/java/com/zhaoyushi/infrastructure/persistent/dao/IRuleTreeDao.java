package com.zhaoyushi.infrastructure.persistent.dao;

import com.zhaoyushi.infrastructure.persistent.po.RuleTree;
import org.apache.ibatis.annotations.Mapper;

/**
 * 规则树表数据访问接口（DAO）
 * <p>对应数据库表 rule_tree，提供规则树主表信息的查询能力，
 * 供规则树仓储（StrategyRepository）组装 RuleTreeVO 时读取树级元信息。
 *
 * @author zhaoy
 * @date 2026/10/2
 * @description 规则树表DAO
 */
@Mapper
public interface IRuleTreeDao {


    /**
     * 根据规则树ID查询规则树主表信息
     *
     * @param treeId 规则树ID，如 "tree_lock"
     * @return 规则树记录；若 treeId 不存在则返回 null
     */
    RuleTree queryRuleTreeByTreeId(String treeId);

}
