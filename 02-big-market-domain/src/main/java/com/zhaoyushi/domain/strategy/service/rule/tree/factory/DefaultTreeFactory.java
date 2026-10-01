package com.zhaoyushi.domain.strategy.service.rule.tree.factory;

import com.zhaoyushi.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.zhaoyushi.domain.strategy.model.valobj.RuleTreeVO;
import com.zhaoyushi.domain.strategy.service.rule.tree.ILogicTreeNode;
import com.zhaoyushi.domain.strategy.service.rule.tree.factory.engine.IDecisionTreeEngine;
import com.zhaoyushi.domain.strategy.service.rule.tree.factory.engine.impl.DecisionTreeEngine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author zhaoy
 * @date 2026/9/30
 * @description 规则树工厂
 * <p>决策树入口：聚合全部规则节点实现，并按需「打开」一棵决策树返回执行引擎。
 * 作为 Spring 单例 Bean，启动时自动收集容器内所有 ILogicTreeNode 实现注入到节点集合，
 * openLogicTree 时把节点集合与树结构一并交给新建的 DecisionTreeEngine。
 *
 * <p>节点集合由 Spring 按 Bean 名自动注入，key 即各节点的 ruleKey：
 * <pre>
 * {
 *   "rule_lock"       → RuleLockLogicTreeNode,
 *   "rule_stock"      → RuleStockLogicTreeNode,
 *   "rule_luck_award" → RuleLuckAwardLogicTreeNode
 * }
 * </pre>
 */
@Service
public class DefaultTreeFactory {

    /**
     * 规则树节点集合
     * <p>键是节点 Bean 名（rule_lock/rule_stock/rule_luck_award），值是对应的逻辑节点实现。
     * Spring 会把容器中所有 ILogicTreeNode 实现自动注入进这个 Map（key 为 @Component 指定的 Bean 名）。
     */
    private final Map<String, ILogicTreeNode> logicTreeNodeGroup;

    /**
     * 构造规则树工厂
     * <p>Spring 自动注入所有规则节点实现组成的 Map。
     *
     * @param logicTreeNodeGroup 所有规则节点 Bean（key 为 Bean 名，即节点 ruleKey）
     */
    public DefaultTreeFactory(Map<String, ILogicTreeNode> logicTreeNodeGroup) {
        this.logicTreeNodeGroup = logicTreeNodeGroup;
    }

    /**
     * 打开（创建）一棵决策树对应的执行引擎
     * <p>将全部节点实现与树结构数据注入新建的 DecisionTreeEngine，返回引擎接口。
     * 每次调用都会 new 一个新的引擎实例，互不干扰。
     *
     * @param ruleTreeVO 决策树结构数据（根节点 + 节点映射）
     * @return 决策树执行引擎
     */
    public IDecisionTreeEngine openLogicTree(RuleTreeVO ruleTreeVO){
        return new DecisionTreeEngine(logicTreeNodeGroup, ruleTreeVO);
    }

    /**
     * 规则树节点执行结果
     * <p>封装单个节点 logic() 的执行结果：判定结果与（可能存在的）奖品数据。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TreeActionEntity{
        /** 节点判定结果：放行 ALLOW / 接管 TAKE_OVER，引擎据此决定下一跳 */
        private RuleLogicCheckTypeVO ruleLogicCheckTypeVO;
        /** 奖品数据：仅当节点接管（如兜底奖励）时携带最终奖品 */
        private StrategyAwardData strategyAwardData;
    }

    /**
     * 奖品数据
     * <p>决策树最终产出的奖品信息载体，随执行过程逐节点向下传递、被叶子节点覆盖。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StrategyAwardData{
        /** 抽奖奖品ID - 内部流转使用 */
        private Integer awardId;
        /** 抽奖奖品规则值 - 奖品对应的规则配置字符串 */
        private String awardRuleValue;
    }

}
