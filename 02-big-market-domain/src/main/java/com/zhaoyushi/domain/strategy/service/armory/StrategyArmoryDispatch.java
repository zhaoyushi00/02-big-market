package com.zhaoyushi.domain.strategy.service.armory;

import com.zhaoyushi.domain.strategy.model.entity.StrategyAwardEntity;
import com.zhaoyushi.domain.strategy.model.entity.StrategyEntity;
import com.zhaoyushi.domain.strategy.model.entity.StrategyRuleEntity;
import com.zhaoyushi.domain.strategy.repository.IStrategyRepository;
import com.zhaoyushi.types.enums.ResponseCode;
import com.zhaoyushi.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.*;

/*策略装配库(兵工厂)，负责初始化策略计算*/

@Slf4j
@Service
public class StrategyArmoryDispatch implements IStrategyArmory, IStrategyDispatch {

    @Resource
    private IStrategyRepository repository;

    //装配抽奖策略
    @Override
    public boolean assembleLotteryStrategy(Long strategyId) {
        //1、查该策略下的所有奖品，先装配一张"总表"
        List<StrategyAwardEntity> strategyAwardEntities = repository.queryStrategyAwardList(strategyId);
        assembleLotteryStrategy(String.valueOf(strategyId), strategyAwardEntities);

        //2、权重策略配置 - 适用于 rule_weight— 权重规则配置
        StrategyEntity strategyEntity = repository.queryStrategyEntityByStrategyId(strategyId);
        String ruleWeight = strategyEntity.getRuleWeight();
        if(null == ruleWeight) return true;// 没配权重 → 装配完就结束

        // 3、把规则值取出来解析，例如：
        //    "4000:102,103,104,105 5000:102,103,104,105,106 6000:..."
        StrategyRuleEntity strategyRuleEntity = repository.queryStrategyRule(strategyId, ruleWeight);
        if (strategyRuleEntity == null) {
            throw new AppException(ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getCode(), ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getInfo());
        }
        Map<String, List<Integer>> ruleWeightValuesMap = strategyRuleEntity.getRuleWeightValues();

        // 4、为每个权重档位单独生成一张子表
        Set<String> keys = ruleWeightValuesMap.keySet();
        for (String key : keys) {
            List<Integer> ruleWeightValues = ruleWeightValuesMap.get(key);
            //总表 strategyAwardEntities 是所有奖品的集合，下面要按权重档位做过滤删减，不能直接改原列表，否则下一个档位循环时总表已经被改坏了
            ArrayList<StrategyAwardEntity> strategyAwardEntitiesClone = new ArrayList<>(strategyAwardEntities);
            //removeIf 的意思是：删除满足条件的元素。这里的条件是 !ruleWeightValues.contains(entity.getAwardId())，即"这个奖品的 ID 不在该权重档位允许的列表里"。
            //反过来理解就是：只保留该权重档位允许出现的奖品，把其他奖品统统删掉。
            //举例（档位 4000，允许 [102,103,104,105]）：
            //总表里有奖品 102,103,104,105,106,107
            //106、107 不在允许列表里 → 被删掉
            //剩下 102,103,104,105 → 这是 4000 档位专属的奖品池
            strategyAwardEntitiesClone.removeIf(entity -> !ruleWeightValues.contains(entity.getAwardId()));
            //把过滤后的奖品列表，用 策略ID_权重档位 作为 key，重新走一遍装配流程（算概率、生成查找表、洗牌、存 Redis）
            assembleLotteryStrategy(String.valueOf(strategyId).concat("_").concat(key), strategyAwardEntitiesClone);
        }

        return true;
    }

    private void assembleLotteryStrategy(String key, List<StrategyAwardEntity> strategyAwardEntities){
        //1、获取最小概率值
        //拿到最小概率是为了后面确定「查找表的精度/总格数」
        BigDecimal minAwardRate = strategyAwardEntities.stream()
                //对流（Stream）中的每个 StrategyAwardEntity 对象调用它的 getAwardRate() 方法，把结果取出来作为新的流元素
                //相当于.map(entity -> entity.getAwardRate())
                .map(StrategyAwardEntity::getAwardRate)
                //相当于.min((a, b) -> a.compareTo(b))
                .min(BigDecimal::compareTo)
                //若列表为空，兜底返回 0（避免 Optional 空异常）
                .orElse(BigDecimal.ZERO);

        //2、获取概率值总和
        BigDecimal totalAwardRate = strategyAwardEntities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                //从 0 开始累加，BigDecimal::add 是加法
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //RoundingMode：除法/取整时的舍入模式。
        //SecureRandom：密码学安全的随机数生成器（比 Random 更随机，适合抽奖）。
        //3、用 1 % 0.0001 获取概率范围 百分位、千分位、万分位
        // 计算查找表的总格数（概率范围）：总概率 ÷ 最小概率，并向上取整
        // 例如：最小概率是 0.01，总概率是 0.35，则 rateRange = 0.35 / 0.01 = 35 格。
        // divide(..., 0, RoundingMode.CEILING)：结果保留 0 位小数，向上取整（CEILING），同时避免除不尽时抛 ArithmeticException
        BigDecimal rateRange = totalAwardRate.divide(minAwardRate, 0, RoundingMode.CEILING);

        //4、创建查找表 strategyAwardSearchRateTables，初始容量设为 rateRange 的整数值（预估总格数，减少扩容）
        ArrayList<Integer> strategyAwardSearchRateTables = new ArrayList<>(rateRange.intValue());
        for (StrategyAwardEntity strategyAward : strategyAwardEntities){
            Integer awardId = strategyAward.getAwardId();
            BigDecimal awardRate = strategyAward.getAwardRate();

            //计算每个概率值需要存放查找表的数量，循环填充
            /*
            *   计算这个奖品要占多少个格子，然后把它的 awardId 重复填进查找表。
                rateRange.multiply(awardRate)：总格数 × 该奖品概率 = 该奖品应占的格数。
                .setScale(0, RoundingMode.CEILING)：向上取整（至少占 1 格）。
                .intValue()：转成 int 作为循环次数。
                内层循环：重复 add(awardId) 对应次数。
                举例：rateRange=35，某奖品概率 0.2，则占 ceil(35×0.2)=7 格，查找表里会连续塞 7 个这个 awardId。
                概率越大的奖品，格子越多，被随机命中的机会越大——这就是「按概率分布」的本质。
            * */
            for (int i = 0; i < rateRange.multiply(awardRate).setScale(0, RoundingMode.CEILING).intValue(); i++) {
                strategyAwardSearchRateTables.add(awardId);
            }
        }

        //5、乱序
        Collections.shuffle(strategyAwardSearchRateTables);

        //6、存集合
        //把 List 转成 Map：key 是格子下标 i，value 是那个格子里的 awardId
        //这样抽奖时可以用随机下标 i 直接 map.get(i) 拿到奖品 ID，时间复杂度 O(1)
        HashMap<Integer, Integer> shuffleStrategyAwardSearchRateTables = new HashMap<>();
        for (int i = 0; i < strategyAwardSearchRateTables.size(); i++) {
            shuffleStrategyAwardSearchRateTables.put(i, strategyAwardSearchRateTables.get(i));
        }

        //7、存到Redis  参数分别为 策略ID、概率分布表的大小、被打乱后的概率分布表
        repository.storeStrategyAwardSearchRateTables(key, BigDecimal.valueOf(shuffleStrategyAwardSearchRateTables.size()), shuffleStrategyAwardSearchRateTables);
        //log.info("---------{}"+shuffleStrategyAwardSearchRateTables);

    }

    //抽奖入口：根据策略 ID 随机抽一个奖品 ID 不带权重：走总表
    @Override
    public Integer getRandomAwardId(Long strategyId) {
        // 分布式部署下，不一定为当前应用做的策略装配。也就是值不一定会保存到本应用，而是分布式应用，所以需要从 Redis 中获取。
        int rateRange = repository.getRateRange(strategyId);
//        new SecureRandom().nextInt(rateRange)：生成 [0, rateRange) 内的一个随机整数，作为格子下标
//        repository.getStrategyAwardAssemble(strategyId, 下标)：去查找表里取该下标对应的 awardId
        // 通过生成的随机值，获取概率值奖品查找表的结果
        return repository.getStrategyAwardAssemble(String.valueOf(strategyId), new SecureRandom().nextInt(rateRange));
    }

    // 带权重：拼出 "100001_4000:102,103,104,105" 走对应子表
    @Override
    public Integer getRandomAwardId(Long strategyId, String ruleWeightValue) {
        String key = String.valueOf(strategyId).concat("_").concat(ruleWeightValue);
        // 分布式部署下，不一定为当前应用做的策略装配。也就是值不一定会保存到本应用，而是分布式应用，所以需要从 Redis 中获取。
        int rateRange = repository.getRateRange(key);
        // 通过生成的随机值，获取概率值奖品查找表的结果
        return repository.getStrategyAwardAssemble(key, new SecureRandom().nextInt(rateRange));
    }
}
