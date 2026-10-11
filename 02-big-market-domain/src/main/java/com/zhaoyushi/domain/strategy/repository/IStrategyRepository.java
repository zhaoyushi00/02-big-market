package com.zhaoyushi.domain.strategy.repository;

import com.zhaoyushi.domain.strategy.model.entity.StrategyAwardEntity;
import com.zhaoyushi.domain.strategy.model.entity.StrategyEntity;
import com.zhaoyushi.domain.strategy.model.entity.StrategyRuleEntity;
import com.zhaoyushi.domain.strategy.model.valobj.RuleTreeVO;
import com.zhaoyushi.domain.strategy.model.valobj.StrategyAwardRuleModelVO;
import com.zhaoyushi.domain.strategy.model.valobj.StrategyAwardStockKeyVO;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

/**
 * 策略仓储接口（领域层仓储端口）
 * <p>定义抽奖策略领域所需的全部数据访问能力，由基础设施层的 StrategyRepository 实现。
 * 负责策略、奖品、规则的查询，以及抽奖概率查找表在 Redis 中的存取。
 * <p>涉及两类数据存储：
 * <ul>
 *   <li>MySQL：策略（strategy）、策略奖品（strategy_award）、策略规则（strategy_rule）</li>
 *   <li>Redis：装配好的概率查找表（总表 key=策略ID；子表 key=策略ID_权重档位）及其概率范围值</li>
 * </ul>
 */
public interface IStrategyRepository {

    /**
     * 查询策略下的奖品列表（带缓存）
     * <p>优先从 Redis 缓存读取，未命中则查库并将 PO 转换为 Entity 后回填缓存。
     *
     * @param strategyId 策略ID
     * @return 该策略下的奖品实体列表
     */
    List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId);

    /**
     * 存储抽奖概率查找表到 Redis
     * <p>分两部分存储：概率范围值（总格数）与打乱后的概率查找表（下标→奖品ID），供抽奖时随机命中。
     *
     * @param key                                  概率表标识（总表为策略ID，子表为 策略ID_权重档位）
     * @param rateRange                            概率范围（查找表总格数）
     * @param shuffleStrategyAwardSearchRateTables 打乱后的概率查找表（下标→奖品ID）
     */
    void storeStrategyAwardSearchRateTables(String key, BigDecimal rateRange, HashMap<Integer, Integer> shuffleStrategyAwardSearchRateTables);

    /**
     * 获取概率范围值（总表）
     * <p>按策略ID读取总表的查找表总格数，用于生成随机下标；未装配时抛出异常。
     *
     * @param strategyId 策略ID
     * @return 概率范围值（查找表总格数）
     */
    int getRateRange(Long strategyId);

    /**
     * 获取概率查找表中指定下标对应的奖品ID
     * <p>抽奖时用随机下标（rateKey）在查找表中定位奖品，时间复杂度 O(1)。
     *
     * @param key     概率表标识（总表为策略ID，子表为 策略ID_权重档位）
     * @param rateKey 随机下标
     * @return 奖品ID
     */
    Integer getStrategyAwardAssemble(String key, int rateKey);

    /**
     * 按策略ID查询策略实体（带缓存）
     * <p>优先从 Redis 缓存读取，未命中则查库并回填缓存。策略实体包含策略描述与规则模型列表。
     *
     * @param strategyId 策略ID
     * @return 策略实体
     */
    StrategyEntity queryStrategyEntityByStrategyId(Long strategyId);

    /**
     * 按策略ID和规则模型查询规则实体
     * <p>用于装配阶段读取规则的完整配置（规则类型、规则值、规则描述等）。
     *
     * @param strategyId 策略ID
     * @param ruleModel  规则模型标识，如 rule_weight、rule_blacklist
     * @return 策略规则实体
     */
    StrategyRuleEntity queryStrategyRule(Long strategyId, String ruleModel);

    /**
     * 获取概率范围值（子表）
     * <p>按复合 key 读取子表的查找表总格数，key 形如「策略ID_权重档位」；未装配时抛出异常。
     *
     * @param key 概率表标识（策略ID 或 策略ID_权重档位）
     * @return 概率范围值（查找表总格数）
     */
    int getRateRange(String key);

    /**
     * 查询规则值字符串
     * <p>按策略ID、奖品ID、规则模型查询规则的原始值，例如权重规则返回 "4000:102,103,104,105 5000:..."。
     *
     * @param strategyId 策略ID
     * @param awardId    奖品ID（可为 null，表示策略级规则）
     * @param ruleModel  规则模型标识，如 rule_weight、rule_blacklist
     * @return 规则值字符串
     */
    String queryStrategyRuleValue(Long strategyId, Integer awardId, String ruleModel);

    /**
     * 查询策略级规则值（奖品ID为空）
     * <p>委托三参重载方法，awardId 传 null，用于查询策略级别的规则配置（如权重、黑名单规则）。
     *
     * @param strategyId 策略ID
     * @param ruleModel  规则模型标识，如 rule_weight、rule_blacklist
     * @return 规则值字符串
     */
    String queryStrategyRuleValue(Long strategyId, String ruleModel);
    /**
     * 查询奖品关联的规则模型
     * <p>按策略ID和奖品ID查询该奖品配置的规则模型列表（如 rule_lock、rule_lock_award），用于抽奖中/后的规则过滤。
     *
     * @param strategyId 策略ID
     * @param awardId    奖品ID
     * @return 奖品规则模型值对象（含规则模型列表）
     */
    StrategyAwardRuleModelVO queryStrategyAwardRuleModel(Long strategyId, Integer awardId);

    /**
     * 按规则树ID查询并组装完整规则树（带缓存）
     * <p>优先从 Redis 缓存读取；未命中则查询规则树主表、节点表、连线表并组装成内存树结构 RuleTreeVO。
     *
     * @param treeId 规则树ID，如 "tree_lock"
     * @return 规则树值对象；若 treeId 不存在则返回 null
     */
    RuleTreeVO queryRuleTreeVOByTreeId(String treeId);

    /**
     * 按策略ID和奖品ID查询奖品关联的规则模型（判空版本）
     * <p>未配置规则模型时返回 null，供调用方据此判断是否进入规则树过滤流程。
     *
     * @param strategyId 策略ID
     * @param awardId    奖品ID
     * @return 奖品规则模型值对象；未配置规则模型时返回 null
     */
    StrategyAwardRuleModelVO queryStrategyAwardRuleModelVO(Long strategyId, Integer awardId);

    /**
     * 缓存奖品初始库存
     * <p>在策略装配阶段，将每个奖品的初始库存写入 Redis 原子计数器（AtomicLong），
     * 供后续抽奖时通过 decr 原子扣减，保证高并发下库存扣减不超卖。
     *
     * @param cacheKay   库存缓存 Key（由 策略ID + 下划线 + 奖品ID 拼接而成）
     * @param awardCount 奖品初始库存数量
     */
    void cacheStrategyAwardCount(String cacheKay, Integer awardCount);

    /**
     * 扣减奖品库存
     * <p>对指定库存缓存 Key 执行 decr 原子扣减，返回扣减是否成功（即是否还有剩余库存可扣）。
     *
     * @param cacheKay 库存缓存 Key
     * @return 扣减成功返回 true；库存不足（扣减后为负数）返回 false
     */
    Boolean subtractionAwardStock(String cacheKay);

    void awardStockConsumeSendQueue(StrategyAwardStockKeyVO strategyAwardStockKeyVO);

    StrategyAwardStockKeyVO takeQueueValue();

    void updateStrategyAwardStock(Long strategyId, Integer awardId);
}
