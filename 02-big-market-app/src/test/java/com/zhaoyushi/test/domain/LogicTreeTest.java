package com.zhaoyushi.test.domain;

import com.alibaba.fastjson2.JSON;
import com.zhaoyushi.domain.strategy.model.valobj.*;
import com.zhaoyushi.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import com.zhaoyushi.domain.strategy.service.rule.tree.factory.engine.IDecisionTreeEngine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author zhaoy
 * @date 2026/10/1
 * @description 决策树测试
 * <p>集成测试：手工构建一棵决策树（rule_lock → rule_stock / rule_luck_award），
 * 通过 DefaultTreeFactory 打开引擎并执行，验证决策树遍历与最终奖品产出链路。
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class LogicTreeTest {

    /** 规则树工厂，用于打开决策树引擎 */
    @Resource
    private DefaultTreeFactory defaultTreeFactory;

    /**
     * 测试决策树完整执行链路
     * <p>树结构：
     * <pre>
     * rule_lock --左(TAKE_OVER)--> rule_luck_award
     *           --右(ALLOW)------> rule_stock --右(TAKE_OVER)--> rule_luck_award
     * </pre>
     * 执行路径：rule_lock 判定 ALLOW → 走右线到 rule_stock → 判定 TAKE_OVER → 到 rule_luck_award（叶子），
     * 最终产出奖品 awardId=101、awardRuleValue="1,100"。
     */
    @Test
    public void test_tree_rule() {
        // 1. 构建 rule_lock 节点：含两根连线（左走 rule_luck_award，右走 rule_stock）
        RuleTreeNodeVO rule_lock = RuleTreeNodeVO.builder()
                .treeId(100000001)
                .ruleKey("rule_lock")
                .ruleDesc("限定用户已完成N次抽奖后解锁")
                .ruleValue("1")
                .treeNodeLineVOList(new ArrayList<RuleTreeNodeLineVO>() {{
                    add(RuleTreeNodeLineVO.builder()
                            .treeId(100000001)
                            .ruleNodeFrom("rule_lock")
                            .ruleNodeTo("rule_luck_award")
                            .ruleLimitType(RuleLimitTypeVO.EQUAL)
                            .ruleLimitValue(RuleLogicCheckTypeVO.TAKE_OVER)
                            .build());

                    add(RuleTreeNodeLineVO.builder()
                            .treeId(100000001)
                            .ruleNodeFrom("rule_lock")
                            .ruleNodeTo("rule_stock")
                            .ruleLimitType(RuleLimitTypeVO.EQUAL)
                            .ruleLimitValue(RuleLogicCheckTypeVO.ALLOW)
                            .build());
                }})
                .build();

        // 2. 构建 rule_luck_award 叶子节点（treeNodeLineVOList 为 null，无下一跳）
        RuleTreeNodeVO rule_luck_award = RuleTreeNodeVO.builder()
                .treeId(100000001)
                .ruleKey("rule_luck_award")
                .ruleDesc("限定用户已完成N次抽奖后解锁")
                .ruleValue("1")
                .treeNodeLineVOList(null)
                .build();

        // 3. 构建 rule_stock 节点：含一根连线（判定 TAKE_OVER 时走 rule_luck_award）
        RuleTreeNodeVO rule_stock = RuleTreeNodeVO.builder()
                .treeId(100000001)
                .ruleKey("rule_stock")
                .ruleDesc("库存处理规则")
                .ruleValue(null)
                .treeNodeLineVOList(new ArrayList<RuleTreeNodeLineVO>() {{
                    add(RuleTreeNodeLineVO.builder()
                            .treeId(100000001)
                            .ruleNodeFrom("rule_lock")
                            .ruleNodeTo("rule_luck_award")
                            .ruleLimitType(RuleLimitTypeVO.EQUAL)
                            .ruleLimitValue(RuleLogicCheckTypeVO.TAKE_OVER)
                            .build());
                }})
                .build();

        // 4. 组装决策树：设置树ID、名称、描述与根节点
        RuleTreeVO ruleTreeVO = new RuleTreeVO();
        ruleTreeVO.setTreeId(100000001);
        ruleTreeVO.setTreeName("决策树规则；增加dall-e-3画图模型");
        ruleTreeVO.setTreeDesc("决策树规则；增加dall-e-3画图模型");
        ruleTreeVO.setTreeRootRuleNode("rule_lock");

        // 5. 用「双花括号初始化」填充节点映射（key = 节点 ruleKey，value = 节点对象）
        // 第①个 {：匿名内部类——new HashMap<String, RuleTreeNodeVO>() { ... } 创建一个 HashMap 的匿名子类
        // 第②个 {：实例初始化块——在对象构造过程中自动执行内部 put(...)，达到创建时顺便填充数据的效果
        ruleTreeVO.setTreeNodeMap(new HashMap<String, RuleTreeNodeVO>() {{
            put("rule_lock", rule_lock);
            put("rule_stock", rule_stock);
            put("rule_luck_award", rule_luck_award);
        }});

        // 6. 通过工厂打开决策树，得到执行引擎
        IDecisionTreeEngine treeEngine = defaultTreeFactory.openLogicTree(ruleTreeVO);

        // 7. 执行决策树并打印最终奖品结果
        DefaultTreeFactory.StrategyAwardData data = treeEngine.process("zhaoyushi", 100001L, 100);
        log.info("测试结果：{}", JSON.toJSONString(data));

    }

}
