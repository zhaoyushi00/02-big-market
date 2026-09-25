package com.zhaoyushi.infrastructure.persistent.repository;

import com.zhaoyushi.domain.strategy.model.entity.StrategyAwardEntity;
import com.zhaoyushi.domain.strategy.repository.IStrategyRepository;
import com.zhaoyushi.infrastructure.persistent.dao.IStrategyAwardDao;
import com.zhaoyushi.infrastructure.persistent.po.StrategyAward;
import com.zhaoyushi.infrastructure.persistent.redis.IRedisService;
import com.zhaoyushi.types.common.Constants;
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
    private IStrategyAwardDao strategyAwardDao;
    @Resource
    private IRedisService redisService;

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

    @Override
    public void storeStrateAwardSearchRateTables(Long strategyId, BigDecimal rateRange, HashMap<Integer, Integer> shuffleStrategyAwardSearchRateTables) {
        //1、存储抽奖策略范围值，如1000以内的随机数
        redisService.setValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + strategyId, rateRange.intValue());
        //2、存储概率查找表
        Map<Integer, Integer> cacheRateTable = redisService.getMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + strategyId);
        cacheRateTable.putAll(shuffleStrategyAwardSearchRateTables);
    }

    @Override
    public int getRateRange(Long strategyId) {
        return redisService.getValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + strategyId);
    }

    @Override
    public Integer getStrategyAwardAssemble(Long strategyId, int rateKey) {
        return redisService.getFromMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + strategyId, rateKey);
    }

}
