package com.zhaoyushi.test.domain;


import com.alibaba.fastjson.JSON;
import com.zhaoyushi.domain.strategy.model.entity.RaffleAwardEntity;
import com.zhaoyushi.domain.strategy.model.entity.RaffleFactorEntity;
import com.zhaoyushi.domain.strategy.service.IRaffleStrategy;
import com.zhaoyushi.domain.strategy.service.rule.impl.RuleweightLogicFilter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

//抽奖策略测试
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RaffleStrategyTest {

    @Resource
    private IRaffleStrategy raffleStrategy;

    @Resource
    private RuleweightLogicFilter ruleWeightLogicFilter;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(ruleWeightLogicFilter, "userScore", 40500L);
    }

    @Test
    public void test_performRaffle() {
        for (int i = 0; i < 5; i++) {
            RaffleFactorEntity raffleFactorEntity = RaffleFactorEntity.builder()
                    .userId("zhaoyushi")
                    .strategyId(100001L)
                    .build();

            RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(raffleFactorEntity);

            log.info("第{}请求参数：{}", i, JSON.toJSONString(raffleFactorEntity));
            log.info("第{}测试结果：{}", i, JSON.toJSONString(raffleAwardEntity));
        }
//        RaffleFactorEntity raffleFactorEntity = RaffleFactorEntity.builder()
//                .userId("zhaoyushi")
//                .strategyId(100001L)
//                .build();
//
//        RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(raffleFactorEntity);
//
//        log.info("请求参数：{}", JSON.toJSONString(raffleFactorEntity));
//        log.info("测试结果：{}", JSON.toJSONString(raffleAwardEntity));
    }

    @Test
    public void test_performRaffle_blacklist() {
        RaffleFactorEntity raffleFactorEntity = RaffleFactorEntity.builder()
                .userId("user003")  // 黑名单用户 user001,user002,user003
                .strategyId(100001L)
                .build();

        RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(raffleFactorEntity);

        log.info("请求参数：{}", JSON.toJSONString(raffleFactorEntity));
        log.info("测试结果：{}", JSON.toJSONString(raffleAwardEntity));
    }

}
