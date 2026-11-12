package com.zhaoyushi.domain.strategy.service;

import java.util.Map;

/**
 * 抽奖规则接口
 *
 * <p>领域服务端口：对外提供与抽奖规则相关的查询能力，供触发器（Controller）层调用。
 * 实现类：{@code DefaultRaffleStrategy}（与奖品、库存等服务共用一个实现类）。
 *
 * @author zhaoy
 * @date 2026/11/10
 * @description 抽奖规则接口
 */
public interface IRaffleRule {

    /**
     * 查询奖品锁定规则（rule_lock）的解锁次数
     * <p>批量查询多个规则树中 rule_lock 节点的 rule_value（抽奖 N 次后解锁的 N 值），
     * 组装成 treeId → N 的映射，用于奖品列表接口判断各奖品的解锁状态。
     *
     * @param treeIds 规则树 ID 数组
     * @return Map：key=规则树ID(treeId)，value=解锁所需抽奖次数(N)
     */
    Map<String, Integer> queryAwardRuleLockCount(String[] treeIds);

}
