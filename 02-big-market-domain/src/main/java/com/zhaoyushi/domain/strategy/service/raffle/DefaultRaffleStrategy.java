package com.zhaoyushi.domain.strategy.service.raffle;

import com.zhaoyushi.domain.strategy.model.entity.RaffleFactorEntity;
import com.zhaoyushi.domain.strategy.model.entity.RuleActionEntity;
import com.zhaoyushi.domain.strategy.model.entity.RuleMatterEntity;
import com.zhaoyushi.domain.strategy.model.entity.StrategyAwardEntity;
import com.zhaoyushi.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.zhaoyushi.domain.strategy.model.valobj.RuleTreeVO;
import com.zhaoyushi.domain.strategy.model.valobj.StrategyAwardRuleModelVO;
import com.zhaoyushi.domain.strategy.model.valobj.StrategyAwardStockKeyVO;
import com.zhaoyushi.domain.strategy.repository.IStrategyRepository;
import com.zhaoyushi.domain.strategy.service.AbstractRaffleStrategy;
import com.zhaoyushi.domain.strategy.service.IRaffleAward;
import com.zhaoyushi.domain.strategy.service.IRaffleStock;
import com.zhaoyushi.domain.strategy.service.armory.IStrategyDispatch;
import com.zhaoyushi.domain.strategy.service.rule.chain.ILogicChain;
import com.zhaoyushi.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import com.zhaoyushi.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import com.zhaoyushi.domain.strategy.service.rule.tree.factory.engine.IDecisionTreeEngine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * @author zhaoy
 * @date 2026/10/8
 * @description 抽奖策略默认实现
 * <p>承接 {@link AbstractRaffleStrategy} 定义的抽奖主流程骨架，实现其中的两个抽象步骤：
 * 责任链抽奖计算（raffleLogicChain）与规则树抽奖过滤（raffleLogicTree）。
 */
@Slf4j
@Service
public class DefaultRaffleStrategy extends AbstractRaffleStrategy implements IRaffleAward, IRaffleStock {

    //在创建对象时，接收外部传入的依赖，并转交给父类完成初始化
    //DefaultChainFactory defaultChainFactory：责任链工厂，负责根据策略配置组装责任链。
    //IStrategyDispatch strategyDispatch：策略调度接口，负责执行抽奖的"装配/抽取"逻辑。
    //IStrategyRepository repository：策略仓储接口，负责查询策略数据（从数据库/Redis 读取策略、奖品、规则等）。
    //父类中已经有了 就不用先protected再构造函数了
    public DefaultRaffleStrategy(DefaultChainFactory defaultChainFactory, IStrategyDispatch strategyDispatch, IStrategyRepository repository, DefaultTreeFactory defaultTreeFactory) {
        super(defaultChainFactory, strategyDispatch, repository, defaultTreeFactory);
    }

    /**
     * 责任链抽奖计算
     * <p>打开指定策略对应的责任链，从链头开始沿链执行，得到命中的奖品ID与逻辑模型标识。
     * 黑名单、权重等前置规则命中时直接返回；默认规则命中时进入规则树过滤流程。
     *
     * @param userId     用户ID
     * @param strategyId 策略ID
     * @return 责任链抽奖结果（含奖品ID与逻辑模型标识）
     */
    @Override
    public DefaultChainFactory.StrategyAwardVO raffleLogicChain(String userId, Long strategyId) {
        ILogicChain logicChain = defaultChainFactory.openLogicChain(strategyId);
        return logicChain.logic(userId, strategyId);
    }

    /**
     * 规则树抽奖过滤
     * <p>根据责任链命中的奖品ID查询其配置的规则模型，打开对应的决策树并执行过滤，
     * 依次经过抽奖次数判断、库存扣减、兜底奖励等节点，返回最终可获得奖品。
     * 若奖品未配置规则模型则直接返回原奖品；若配置的规则树不存在则抛出异常提示配置缺失。
     *
     * @param userId     用户ID
     * @param strategyId 策略ID
     * @param awardId    责任链命中的候选奖品ID
     * @return 规则树过滤结果（含最终奖品ID与奖品规则值）
     */
    @Override
    public DefaultTreeFactory.StrategyAwardVO raffleLogicTree(String userId, Long strategyId, Integer awardId) {
        // 1. 查询策略下该奖品配置的规则模型，得到规则模型 Key（用于定位规则树）
        StrategyAwardRuleModelVO strategyAwardRuleModelVO = repository.queryStrategyAwardRuleModelVO(strategyId, awardId);
        // 2. 未配置规则模型：无需过滤，直接返回原奖品
        if (null == strategyAwardRuleModelVO) {
            return DefaultTreeFactory.StrategyAwardVO.builder().awardId(awardId).build();
        }
        // 3. 根据规则模型 Key 查询对应的规则树结构
        RuleTreeVO ruleTreeVO = repository.queryRuleTreeVOByTreeId(strategyAwardRuleModelVO.getRuleModels());
        // 4. 规则树缺失：配置错误，抛出异常提示补齐 rule_tree 相关库表数据
        if (null == ruleTreeVO) {
            throw new RuntimeException("存在抽奖策略配置的规则模型 Key，未在库表 rule_tree、rule_tree_node、rule_tree_line 配置对应的规则树信息 "
                    + strategyAwardRuleModelVO.getRuleModels());
        }
        // 5. 打开决策树引擎并执行过滤（次数判断、库存扣减、兜底奖励），返回最终可获得奖品
        IDecisionTreeEngine treeEngine = defaultTreeFactory.openLogicTree(ruleTreeVO);
        return treeEngine.process(userId, strategyId, awardId);
    }

    @Override
    public StrategyAwardStockKeyVO takeQueueValue() throws InterruptedException {
        return repository.takeQueueValue();
    }

    @Override
    public void updateStrategyAwardStock(Long strategyId, Integer awardId) {
        repository.updateStrategyAwardStock(strategyId, awardId);
    }

    @Override
    public List<StrategyAwardEntity> queryRaffleStrategyAwardList(Long strategyId) {
        return repository.queryStrategyAwardList(strategyId);
    }
}
