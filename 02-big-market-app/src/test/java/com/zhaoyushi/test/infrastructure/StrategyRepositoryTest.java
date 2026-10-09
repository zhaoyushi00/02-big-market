package com.zhaoyushi.test.infrastructure;

import com.alibaba.fastjson2.JSON;
import com.zhaoyushi.domain.strategy.model.valobj.RuleTreeVO;
import com.zhaoyushi.domain.strategy.repository.IStrategyRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 策略仓储集成测试
 * <p>通过 SpringBootTest 加载完整上下文，验证规则树仓储方法的真实查库与组装能力，
 * 依赖本地 MySQL（big_market 库）与 Redis 环境。
 *
 * @author zhaoy
 * @date 2026/10/2
 * @description 仓储数据查询
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class StrategyRepositoryTest {

    /** 策略仓储接口（注入基础设施层实现 StrategyRepository） */
    @Resource
    private IStrategyRepository repository;

    /**
     * 验证按规则树ID查询并组装完整规则树
     * <p>treeId 取数据库 rule_tree 表中已配置的 "tree_lock"，
     * 断言可查询到树主表、节点表、连线表数据并成功组装为 RuleTreeVO。
     */
    @Test
    public void queryRuleTreeVOByTreeId(){
        RuleTreeVO ruleTreeVO = repository.queryRuleTreeVOByTreeId("tree_lock");
        log.info("测试结果:{}", JSON.toJSON(ruleTreeVO));
    }

}
