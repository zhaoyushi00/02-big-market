package com.zhaoyushi.infrastructure.persistent.repository;

import com.zhaoyushi.domain.strategy.model.entity.StrategyAwardEntity;
import com.zhaoyushi.domain.strategy.model.entity.StrategyEntity;
import com.zhaoyushi.domain.strategy.model.entity.StrategyRuleEntity;
import com.zhaoyushi.domain.strategy.model.valobj.*;
import com.zhaoyushi.domain.strategy.repository.IStrategyRepository;
import com.zhaoyushi.infrastructure.persistent.dao.*;
import com.zhaoyushi.infrastructure.persistent.po.*;
import com.zhaoyushi.infrastructure.persistent.redis.IRedisService;
import com.zhaoyushi.types.common.Constants;
import com.zhaoyushi.types.enums.ResponseCode;
import com.zhaoyushi.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RMap;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.zhaoyushi.types.enums.ResponseCode.UN_ASSEMBLED_STRATEGY_ARMORY;

/* 策略仓储实现 */
// 通过 Lombok 自动生成 log 日志对象，供下方「扣减奖品库存」方法打印日志使用
@Slf4j
@Repository
public class StrategyRepository implements IStrategyRepository {

    @Resource
    private IStrategyDao strategyDao;
    @Resource
    private IStrategyAwardDao strategyAwardDao;
    @Resource
    private IRedisService redisService;
    @Resource
    private IStrategyRuleDao strategyRuleDao;
    @Resource
    private IRuleTreeDao ruleTreeDao;
    @Resource
    private IRuleTreeNodeDao ruleTreeNodeDao;
    @Resource
    private IRuleTreeNodeLineDao ruleTreeNodeLineDao;

    /**
     * 查询策略奖品列表（带缓存）
     * <p>优先从 Redis 缓存读取，未命中则查库并回填缓存。
     *
     * @param strategyId 策略ID
     * @return 策略奖品实体列表
     */
    @Override
    public List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId) {

//        Redis 是一个 key-value 存储，而且它的 value 只能存字节（二进制数据），它根本不知道什么是 List、什么是 StrategyAwardEntity。
//        List<StrategyAwardEntity> 对象，塞进 Redis的办法是 序列化（serialize） 和 反序列化（deserialize）：
//        写（setValue）：Java对象  ──序列化──▶  字节/JSON  ──▶  Redis
//        读（getValue）：Redis  ──▶  字节/JSON  ──反序列化──▶  Java对象

        /*
        * 拼接缓存键。STRATEGY_AWARD_KEY 的值是 "big_market_strategy_award_key_"。
        * 例如 strategyId = 10001 时，最终 key 为 big_market_strategy_award_key_10001，保证不同策略的缓存互不干扰。
        * */
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_LIST_KEY + strategyId;
        //缓存优先：能命中缓存就不查数据库，减少 DB 压力、提升响应速度。
        List<StrategyAwardEntity> strategyAwardEntities = redisService.getValue(cacheKey);

        if(null != strategyAwardEntities && !strategyAwardEntities.isEmpty()) return strategyAwardEntities;

        // 从库中读取数据
        List<StrategyAward> strategyAwards = strategyAwardDao.queryStrategyAwardListByStrategyId(strategyId);
        // 将 strategyAwards（大的）转化成 StrategyAwardEntity（小的）「PO → Entity」
        //重新初始化领域实体列表，并预设容量为查到的 PO 数量，避免 ArrayList 动态扩容的性能开销。
        strategyAwardEntities = new ArrayList<>(strategyAwards.size());
        for (StrategyAward strategyAward : strategyAwards) {
            StrategyAwardEntity strategyAwardEntity = StrategyAwardEntity.builder()
                        .strategyId(strategyAward.getStrategyId())
                        .awardId(strategyAward.getAwardId())
                        .awardCount(strategyAward.getAwardCount())
                        .awardCountSurplus(strategyAward.getAwardCountSurplus())
                        .awardRate(strategyAward.getAwardRate())
                        .awardSubtitle(strategyAward.getAwardSubtitle())
                        .awardTitle(strategyAward.getAwardTitle())
                        .sort(strategyAward.getSort())
                        .build();
            strategyAwardEntities.add(strategyAwardEntity);
        }
        redisService.setValue(cacheKey, strategyAwardEntities);
        return strategyAwardEntities;
    }

    /**
     * 存储抽奖概率查找表
     * <p>分别将概率范围值和概率查找表存储到 Redis。
     *
     * @param key                                  策略标识（可能含权重档位）
     * @param rateRange                            概率范围（总格数）
     * @param shuffleStrategyAwardSearchRateTables 打乱后的概率查找表（下标→奖品ID）
     */
    @Override
    public void storeStrategyAwardSearchRateTables(String key, BigDecimal rateRange, HashMap<Integer, Integer> shuffleStrategyAwardSearchRateTables) {
        //1、存储抽奖策略范围值，如1000以内的随机数
        redisService.setValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key, rateRange.intValue());
        //2、存储概率查找表
        Map<Integer, Integer> cacheRateTable = redisService.getMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + key);
        cacheRateTable.putAll(shuffleStrategyAwardSearchRateTables);
    }

    /**
     * 获取概率范围（按策略ID）
     *
     * @param strategyId 策略ID
     * @return 概率范围值（查找表总格数）
     */
    @Override
    public int getRateRange(Long strategyId) {
        String cacheKey = Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + strategyId;
        if (!redisService.isExists(cacheKey)) {
            throw new AppException(UN_ASSEMBLED_STRATEGY_ARMORY.getCode(), cacheKey + Constants.COLON + UN_ASSEMBLED_STRATEGY_ARMORY.getInfo());
        }
        return redisService.getValue(cacheKey);
    }

    /**
     * 获取概率范围（按复合 key）
     *
     * @param key 复合 key（如 策略ID 或 策略ID_权重档位）
     * @return 概率范围值（查找表总格数）
     */
    @Override
    public int getRateRange(String key) {
        Integer rateRange = redisService.getValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key);
        if (rateRange == null) {
            throw new AppException(ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getCode(),
                    "策略未装配，Redis 中不存在概率范围 key：" + key);
        }
        return rateRange;
        //return redisService.getValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key);
    }

    /**
     * 查询策略规则值
     *
     * @param strategyId 策略ID
     * @param awardId    奖品ID（可为 null）
     * @param ruleModel  规则模型
     * @return 规则值字符串
     */
    @Override
    public String queryStrategyRuleValue(Long strategyId, Integer awardId, String ruleModel) {
        StrategyRule strategyRule = new StrategyRule();
        strategyRule.setRuleModel(ruleModel);
        strategyRule.setStrategyId(strategyId);
        strategyRule.setAwardId(awardId);
        return strategyRuleDao.queryStrategyRuleValue(strategyRule);
    }

    /**
     * 查询策略级规则值（奖品ID为空）
     * <p>委托三参重载方法，awardId 传 null，用于查询策略级别的规则配置（如权重、黑名单规则）。
     *
     * @param strategyId 策略ID
     * @param ruleModel  规则模型，如 rule_weight、rule_blacklist
     * @return 规则值字符串
     */
    @Override
    public String queryStrategyRuleValue(Long strategyId, String ruleModel) {
        return queryStrategyRuleValue(strategyId, null, ruleModel);
    }

    /**
     * 获取概率查找表中指定下标对应的奖品ID
     *
     * @param key     策略标识（可能含权重档位）
     * @param rateKey 随机下标
     * @return 奖品ID
     */
    @Override
    public Integer getStrategyAwardAssemble(String key, int rateKey) {
        return redisService.getFromMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + key, rateKey);
    }

    /**
     * 按策略ID查询策略实体（带缓存）
     * <p>优先从 Redis 缓存读取，未命中则查库并回填缓存。
     *
     * @param strategyId 策略ID
     * @return 策略实体
     */
    @Override
    public StrategyEntity queryStrategyEntityByStrategyId(Long strategyId) {
        //优先缓存
        String cacheKey = Constants.RedisKey.STRATEGY_KEY + strategyId;
        StrategyEntity strategyEntity = redisService.getValue(cacheKey);

        if(null!=strategyEntity) return strategyEntity;
        Strategy strategy = strategyDao.queryStrategyByStrategyId(strategyId);
        strategyEntity = StrategyEntity.builder()
                .strategyId(strategy.getStrategyId())
                .strategyDesc(strategy.getStrategyDesc())
                .ruleModels(strategy.getRuleModel())
                .build();
        redisService.setValue(cacheKey, strategyEntity);
        return strategyEntity;
    }

    /**
     * 按策略ID和规则模型查询规则实体
     *
     * @param strategyId 策略ID
     * @param ruleModel  规则模型
     * @return 策略规则实体
     */
    @Override
    public StrategyRuleEntity queryStrategyRule(Long strategyId, String ruleModel) {
        StrategyRule strategyRuleReq = new StrategyRule();
        strategyRuleReq.setStrategyId(strategyId);
        strategyRuleReq.setRuleModel(ruleModel);
        StrategyRule strategyRulesRes = strategyRuleDao.queryStrategyRule(strategyRuleReq);
        return StrategyRuleEntity.builder()
                .strategyId(strategyRulesRes.getStrategyId())
                .awardId(strategyRulesRes.getAwardId())
                .ruleType(strategyRulesRes.getRuleType())
                .ruleModel(strategyRulesRes.getRuleModel())
                .ruleValue(strategyRulesRes.getRuleValue())
                .ruleDesc(strategyRulesRes.getRuleDesc())
                .build();
    }

    /**
     * 按策略ID和奖品ID查询奖品关联的规则模型列表
     *
     * @param strategyId 策略ID
     * @param awardId    奖品ID
     * @return 奖品规则模型值对象（含规则模型字符串，如 "tree_lock"）
     */
    @Override
    public StrategyAwardRuleModelVO queryStrategyAwardRuleModel(Long strategyId, Integer awardId) {
        StrategyAward strategyAward = new StrategyAward();
        strategyAward.setStrategyId(strategyId);
        strategyAward.setAwardId(awardId);
        String ruleModels = strategyAwardDao.queryStrategyAwardRuleModel(strategyAward);
        return StrategyAwardRuleModelVO.builder().ruleModels(ruleModels).build();
    }

    /**
     * 按规则树ID查询并组装完整规则树（带缓存）
     * <p>优先从 Redis 缓存读取；未命中则分别查询规则树主表、节点表、连线表，
     * 将三张平铺表的数据组装成一棵内存树结构 RuleTreeVO：连线按起点分组挂载到对应节点上，
     * 节点以 ruleKey 为键存入 treeNodeMap，供决策树引擎按 key 快速定位节点并遍历执行。
     * 组装完成后回填缓存并返回。
     *
     * @param treeId 规则树ID，如 "tree_lock"
     * @return 规则树值对象；若 treeId 不存在则返回 null
     */
    @Override
    public RuleTreeVO queryRuleTreeVOByTreeId(String treeId) {

        // 1. 优先从缓存获取 "rule_tree_vo_key_treeId"，命中则直接返回，避免重复查库与组装
        String cacheKey = Constants.RedisKey.RULE_TREE_VO_KEY + treeId;
        RuleTreeVO ruleTreeVOCache = redisService.getValue(cacheKey);
        if (null != ruleTreeVOCache) return ruleTreeVOCache;

        // 2. 查询规则树主表、节点表、连线表（三张平铺表）
        RuleTree ruleTree = ruleTreeDao.queryRuleTreeByTreeId(treeId);
        // 树主表不存在时直接返回 null，避免后续组装时调用 getTreeId() 抛 NPE
        if (null == ruleTree) return null;
        List<RuleTreeNode> ruleTreeNodes = ruleTreeNodeDao.queryRuleTreeNodeListByTreeId(treeId);
        List<RuleTreeNodeLine> ruleTreeNodeLines = ruleTreeNodeLineDao.queryRuleTreeNodeLineListByTreeId(treeId);

        // 3. 连线 PO 转 VO，并按起点分组：{起点ruleKey -> [该起点的所有出边]}
        Map<String, List<RuleTreeNodeLineVO>> ruleTreeNodeLineMap = new HashMap<>();
        for (RuleTreeNodeLine ruleTreeNodeLine : ruleTreeNodeLines) {
            RuleTreeNodeLineVO ruleTreeNodeLineVO = RuleTreeNodeLineVO.builder()
                    .treeId(ruleTreeNodeLine.getTreeId())
                    .ruleNodeFrom(ruleTreeNodeLine.getRuleNodeFrom())
                    .ruleNodeTo(ruleTreeNodeLine.getRuleNodeTo())
                    .ruleLimitType(RuleLimitTypeVO.valueOf(ruleTreeNodeLine.getRuleLimitType()))
                    .ruleLimitValue(RuleLogicCheckTypeVO.valueOf(ruleTreeNodeLine.getRuleLimitValue()))
                    .build();

            // computeIfAbsent：起点 key 不存在时新建空 List 并存入 Map，存在则复用旧 List，最终得到 {起点ruleKey -> [该起点的所有出边]}
            List<RuleTreeNodeLineVO> ruleTreeNodeLineVOList = ruleTreeNodeLineMap.computeIfAbsent(ruleTreeNodeLine.getRuleNodeFrom(), k -> new ArrayList<>());
            ruleTreeNodeLineVOList.add(ruleTreeNodeLineVO);
        }

        // 4. 节点 PO 转 VO 并挂载出边列表，构建 {ruleKey -> RuleTreeNodeVO}
        Map<String, RuleTreeNodeVO> treeNodeMap = new HashMap<>();
        for (RuleTreeNode ruleTreeNode : ruleTreeNodes) {
            RuleTreeNodeVO ruleTreeNodeVO = RuleTreeNodeVO.builder()
                    .treeId(ruleTreeNode.getTreeId())
                    .ruleKey(ruleTreeNode.getRuleKey())
                    .ruleDesc(ruleTreeNode.getRuleDesc())
                    .ruleValue(ruleTreeNode.getRuleValue())
                    .treeNodeLineVOList(ruleTreeNodeLineMap.get(ruleTreeNode.getRuleKey()))
                    .build();
            treeNodeMap.put(ruleTreeNode.getRuleKey(), ruleTreeNodeVO);
        }

        // 5. 构建 RuleTreeVO 并回填缓存
        RuleTreeVO ruleTreeVODB = RuleTreeVO.builder()
                .treeId(ruleTree.getTreeId())
                .treeName(ruleTree.getTreeName())
                .treeDesc(ruleTree.getTreeDesc())
                .treeRootRuleNode(ruleTree.getTreeRootRuleKey())
                .treeNodeMap(treeNodeMap)
                .build();

        redisService.setValue(cacheKey, ruleTreeVODB);
        return ruleTreeVODB;

    }

    /**
     * 按策略ID和奖品ID查询奖品关联的规则模型（判空版本）
     * <p>与 queryStrategyAwardRuleModel 查询同一字段，区别在于未配置规则模型（ruleModels 为 null）时返回 null，
     * 供调用方（如 DefaultRaffleStrategy）据此判断是否进入规则树过滤流程。
     *
     * @param strategyId 策略ID
     * @param awardId    奖品ID
     * @return 奖品规则模型值对象；未配置规则模型时返回 null
     */
    @Override
    public StrategyAwardRuleModelVO queryStrategyAwardRuleModelVO(Long strategyId, Integer awardId) {
        StrategyAward strategyAward = new StrategyAward();
        strategyAward.setStrategyId(strategyId);
        strategyAward.setAwardId(awardId);
        String ruleModels = strategyAwardDao.queryStrategyAwardRuleModels(strategyAward);
        if (null == ruleModels) return null;
        return StrategyAwardRuleModelVO.builder().ruleModels(ruleModels).build();
    }

    /**
     * 缓存奖品初始库存
     * <p>先判断库存缓存是否已存在，不存在时再写入，避免重复装配时覆盖已被扣减的库存。
     *
     * @param cacheKay   库存缓存 Key
     * @param awardCount 奖品初始库存数量
     */
    @Override
    public void cacheStrategyAwardCount(String cacheKay, Integer awardCount) {
        // 读取当前库存缓存值，判断是否已初始化
//        Long cacheAwardCount = redisService.getAtomicLong(cacheKay);
//        // 已初始化则直接返回，避免覆盖已扣减的库存
//        if (null != cacheAwardCount) return;
        if (redisService.isExists(cacheKay)) return;
        // 未初始化则写入奖品初始库存
        redisService.setAtomicLong(cacheKay, awardCount);
    }

    /**
     * 扣减奖品库存
     * <p>对库存缓存 Key 执行 decr 原子扣减；扣减后为负数说明库存已耗尽，回写为 0 并返回 false。
     * 扣减成功后通过 setNx 抢占一个「库存Key_剩余量」的锁 Key，返回锁是否抢占成功。
     *
     * @param cacheKay 库存缓存 Key
     * @return 扣减且加锁成功返回 true；库存不足或加锁失败返回 false
     */
    @Override
    public Boolean subtractionAwardStock(String cacheKay) {
        // decr 原子扣减库存，并返回扣减后的剩余量
        long surplus = redisService.decr(cacheKay);
        // 剩余量为负数说明库存已耗尽
        if(surplus < 0){
            // 回写为 0，避免库存变成负数
            redisService.setValue(cacheKay, 0);
            // 返回 false 表示扣减失败（无库存可扣）
            return false;
        }
        // 拼接锁 Key：库存Key_剩余量，用于后续业务唯一性校验
        String lockKey = cacheKay + Constants.UNDERLINE + surplus;
        // 通过 setNx 抢占锁 Key，抢占成功表示本次扣减有效
        // 将 key 的值设为 value，当且仅当 key 不存在。
        // 若给定的 key 已经存在，则 SETNX 不做任何动作。
        // setNx 返回 false 只会在同一个 lockKey 被第二次 setNx 时发生，也就是"同一个剩余量被处理了两次"
        // 就是代替了数据库判空的功能，不用多次查询数据库了
        Boolean lock = redisService.setNx(lockKey);
        // 抢占失败时记录日志，便于排查
        if(!lock){
            log.info("策略奖品库存加锁失败{}", lockKey);
        }
        // 返回锁抢占结果
        return lock;
    }

    /**
     * 奖品库存消费发送延时队列
     * <p>库存扣减成功后，将「策略ID + 奖品ID」库存标识写入 Redis 延时队列，
     * 延迟 3 秒后被 trigger 模块的 UpdateAwardStockJob 消费，异步回写数据库库存记录，
     * 实现 Redis 与数据库的最终一致，避免每次扣减都同步写库造成性能压力。
     *
     * @param strategyAwardStockKeyVO 策略奖品库存Key标识（strategyId + awardId）
     */
    @Override
    public void awardStockConsumeSendQueue(StrategyAwardStockKeyVO strategyAwardStockKeyVO) {
        // 取库存消费队列的缓存 Key
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_QUERY_KEY;
        // 获取阻塞队列，作为延时队列的底层目标队列
        RBlockingQueue<StrategyAwardStockKeyVO> blockingQueue = redisService.getBlockingQueue(cacheKey);
        // 基于阻塞队列获取延时队列
        RDelayedQueue<StrategyAwardStockKeyVO> delayedQueue = redisService.getDelayedQueue(blockingQueue);
        // 投递库存标识到延时队列，延迟 3 秒后进入阻塞队列等待消费
        delayedQueue.offer(strategyAwardStockKeyVO, 3, TimeUnit.SECONDS);
    }

    @Override
    public StrategyAwardStockKeyVO takeQueueValue() {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_QUERY_KEY;
        RBlockingQueue<StrategyAwardStockKeyVO> destinationQueue = redisService.getBlockingQueue(cacheKey);
        return destinationQueue.poll();
    }

    @Override
    public void updateStrategyAwardStock(Long strategyId, Integer awardId) {
        StrategyAward strategyAward = new StrategyAward();
        strategyAward.setAwardId(awardId);
        strategyAward.setStrategyId(strategyId);
        strategyAwardDao.updateStrategyAwardStock(strategyAward);
    }

    @Override
    public StrategyAwardEntity queryStrategyAwardEntity(Long strategyId, Integer awardId) {
        // 优先从缓存获取
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_KEY + strategyId + Constants.UNDERLINE + awardId;
        StrategyAwardEntity strategyAwardEntity = redisService.getValue(cacheKey);
        if (null != strategyAwardEntity) return strategyAwardEntity;
        // 查询数据
        StrategyAward strategyAwardReq = new StrategyAward();
        strategyAwardReq.setStrategyId(strategyId);
        strategyAwardReq.setAwardId(awardId);
        StrategyAward strategyAwardRes = strategyAwardDao.queryStrategyAward(strategyAwardReq);
        // 转换数据
        strategyAwardEntity = StrategyAwardEntity.builder()
                .strategyId(strategyAwardRes.getStrategyId())
                .awardId(strategyAwardRes.getAwardId())
                .awardTitle(strategyAwardRes.getAwardTitle())
                .awardSubtitle(strategyAwardRes.getAwardSubtitle())
                .awardCount(strategyAwardRes.getAwardCount())
                .awardCountSurplus(strategyAwardRes.getAwardCountSurplus())
                .awardRate(strategyAwardRes.getAwardRate())
                .sort(strategyAwardRes.getSort())
                .build();
        // 缓存结果
        redisService.setValue(cacheKey, strategyAwardEntity);
        // 返回数据
        return strategyAwardEntity;
    }

}
