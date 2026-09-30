package com.zhaoyushi.domain.strategy.service.rule.chain.factory;

import com.zhaoyushi.domain.strategy.model.entity.StrategyEntity;
import com.zhaoyushi.domain.strategy.repository.IStrategyRepository;
import com.zhaoyushi.domain.strategy.service.rule.chain.ILogicChain;
import org.springframework.stereotype.Service;

import java.util.Map;
/**
 * 责任链组装工厂
 * <p>根据策略配置的规则模型列表，动态地将各链节点按顺序串成一条责任链。
 * 链尾始终追加兜底节点 default，保证任何策略最终都能产出抽奖结果。
 *
 * <p>组装示例：策略 ruleModels = "rule_blacklist,rule_weight" 时，
 * 拼装出的链为 BackListLogicChain → RuleWeightLogicChain → DefaultLogicChain。
 */
@Service
public class DefaultChainFactory {

    /**
     * 责任链节点集合
     * <p>由 Spring 注入：当构造器参数为 Map<String, ILogicChain> 时，Spring 自动收集所有 ILogicChain 实现 Bean，
     * key 为 @Component 指定的 Bean 名。
     * 例如：{ "rule_backlist" → BackListLogicChain, "rule_weight" → RuleWeightLogicChain, "default" → DefaultLogicChain }
     */
    private final Map<String, ILogicChain> logicChainGroup;

    /** 策略仓储，用于按策略ID查询规则模型配置 */
    private final IStrategyRepository repository;

    /**
     * 构造责任链工厂
     * <p>Spring 自动注入所有链节点组成的 Map 与策略仓储。
     *
     * @param logicChainGroup 所有链节点 Bean（key 为 Bean 名）
     * @param repository      策略仓储
     */
    public DefaultChainFactory(Map<String, ILogicChain> logicChainGroup, IStrategyRepository repository) {
        this.logicChainGroup = logicChainGroup;
        this.repository = repository;
    }

    /**
     * 打开（组装）指定策略对应的责任链
     * <p>按策略配置的规则模型列表顺序取出对应节点，用 appendNext 串成链表，并在链尾追加 default 兜底节点。
     * 未配置任何规则时直接返回 default 节点。
     *
     * <p>示例：ruleModels = ["rule_blacklist", "rule_weight"] 时，
     * 返回链头 BackListLogicChain，链结构为 黑名单 → 权重 → 兜底。
     *
     * @param strategyId 策略ID
     * @return 责任链头节点，调用其 logic() 即可沿链执行
     */
    public ILogicChain openLogicChain(Long strategyId) {
        // 1. 查策略配置，得到规则模型数组，例如 "rule_blacklist,rule_weight" → ["rule_blacklist", "rule_weight"]
        StrategyEntity strategy = repository.queryStrategyEntityByStrategyId(strategyId);
        String[] ruleModels = strategy.ruleModels();

        // 2. 未配置任何规则 → 直接返回兜底节点，走总表抽奖
        if (null == ruleModels || 0 == ruleModels.length) {
            return logicChainGroup.get("default");
        }

        // 3. 取第一个规则节点作为链头，按数组顺序逐个用 appendNext 拼接
        ILogicChain logicChain = logicChainGroup.get(ruleModels[0]);
        ILogicChain current = logicChain;
        for (int i = 1; i < ruleModels.length; i++) {
            ILogicChain nextChain = logicChainGroup.get(ruleModels[i]);
            current = current.appendNext(nextChain);
        }

        // 4. 链尾追加 default 兜底节点，保证链末一定有节点处理
        current.appendNext(logicChainGroup.get("default"));

        // 5. 返回链头
        return logicChain;
    }

}
