package com.zhaoyushi.infrastructure.persistent.repository;

import com.zhaoyushi.domain.strategy.model.entity.StrategyAwardEntity;
import com.zhaoyushi.domain.strategy.model.entity.StrategyEntity;
import com.zhaoyushi.domain.strategy.model.entity.StrategyRuleEntity;
import com.zhaoyushi.domain.strategy.repository.IStrategyRepository;
import com.zhaoyushi.infrastructure.persistent.dao.IStrategyAwardDao;
import com.zhaoyushi.infrastructure.persistent.dao.IStrategyDao;
import com.zhaoyushi.infrastructure.persistent.dao.IStrategyRuleDao;
import com.zhaoyushi.infrastructure.persistent.po.Strategy;
import com.zhaoyushi.infrastructure.persistent.po.StrategyAward;
import com.zhaoyushi.infrastructure.persistent.po.StrategyRule;
import com.zhaoyushi.infrastructure.persistent.redis.IRedisService;
import com.zhaoyushi.types.common.Constants;
import com.zhaoyushi.types.enums.ResponseCode;
import com.zhaoyushi.types.exception.AppException;
import jakarta.annotation.Resource;
import org.redisson.api.RMap;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* 策略仓储实现 */
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
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_KEY + strategyId;
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
        Integer rateRange = redisService.getValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + strategyId);
        if (rateRange == null) {
            throw new AppException(ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getCode(),
                    "策略未装配，Redis 中不存在概率范围 key：" + strategyId);
        }
        return rateRange;
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

}
