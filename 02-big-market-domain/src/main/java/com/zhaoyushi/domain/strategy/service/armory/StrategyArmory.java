package com.zhaoyushi.domain.strategy.service.armory;

import com.zhaoyushi.domain.strategy.model.entity.StrategyAwardEntity;
import com.zhaoyushi.domain.strategy.repository.IStrategyRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/*策略装配库(兵工厂)，负责初始化策略计算*/

@Slf4j
@Service
public class StrategyArmory implements IStrategyArmory {

    @Resource
    private IStrategyRepository repository;

    //装配抽奖策略
    @Override
    public boolean assembleLotteryStrategy(Long strategyId) {
        //1、查询策略配置
        List<StrategyAwardEntity> strategyAwardEntities = repository.queryStrategyAwardList(strategyId);

        //2、获取最小概率值
        //拿到最小概率是为了后面确定「查找表的精度/总格数」
        BigDecimal minAwardRate = strategyAwardEntities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .min(BigDecimal::compareTo)
                //若列表为空，兜底返回 0（避免 Optional 空异常）
                .orElse(BigDecimal.ZERO);

        //3、获取概率值总和
        BigDecimal totalAwardRate = strategyAwardEntities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                //从 0 开始累加，BigDecimal::add 是加法
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //RoundingMode：除法/取整时的舍入模式。
        //SecureRandom：密码学安全的随机数生成器（比 Random 更随机，适合抽奖）。
        //4、用 1 % 0.0001 获取概率范围 百分位、千分位、万分位
        //计算查找表的总格数（概率范围）：总概率 ÷ 最小概率，并向上取整
// 例如：最小概率是 0.01，总概率是 0.35，则 rateRange = 0.35 / 0.01 = 35 格。
// divide(..., 0, RoundingMode.CEILING)：结果保留 0 位小数，向上取整（CEILING），同时避免除不尽时抛 ArithmeticException
        BigDecimal rateRange = totalAwardRate.divide(minAwardRate, 0, RoundingMode.CEILING);

        //5、创建查找表 strategyAwardSearchRateTables，初始容量设为 rateRange 的整数值（预估总格数，减少扩容）
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

        //6、乱序
        Collections.shuffle(strategyAwardSearchRateTables);

        //7、存集合
        //把 List 转成 Map：key 是格子下标 i，value 是那个格子里的 awardId
        //这样抽奖时可以用随机下标 i 直接 map.get(i) 拿到奖品 ID，时间复杂度 O(1)
        HashMap<Integer, Integer> shuffleStrategyAwardSearchRateTables = new HashMap<>();
        for (int i = 0; i < strategyAwardSearchRateTables.size(); i++) {
            shuffleStrategyAwardSearchRateTables.put(i, strategyAwardSearchRateTables.get(i));
        }

        //8、存到Redis  参数分别为 策略ID、概率分布表的大小、被打乱后的概率分布表
        repository.storeStrateAwardSearchRateTables(strategyId, rateRange, shuffleStrategyAwardSearchRateTables);
        log.info("---------{}"+shuffleStrategyAwardSearchRateTables);
        return true;
    }

    //抽奖入口：根据策略 ID 随机抽一个奖品 ID
    @Override
    public Integer getRandomAwardId(Long strategyId) {
        int rateRange = repository.getRateRange(strategyId);
//        new SecureRandom().nextInt(rateRange)：生成 [0, rateRange) 内的一个随机整数，作为格子下标
//        repository.getStrategyAwardAssemble(strategyId, 下标)：去查找表里取该下标对应的 awardId
        return repository.getStrategyAwardAssemble(strategyId, new SecureRandom().nextInt(rateRange));
    }
}
