package com.zhaoyushi.test.domain;


import com.alibaba.fastjson.JSON;
import com.zhaoyushi.domain.strategy.model.entity.RaffleAwardEntity;
import com.zhaoyushi.domain.strategy.model.entity.RaffleFactorEntity;
import com.zhaoyushi.domain.strategy.service.IRaffleStrategy;
import com.zhaoyushi.domain.strategy.service.armory.IStrategyArmory;
import com.zhaoyushi.domain.strategy.service.rule.chain.impl.RuleWeightLogicChain;
import com.zhaoyushi.domain.strategy.service.rule.filter.impl.RuleLockLogicFilter;
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
    private IStrategyArmory strategyArmory;
    @Resource
    private RuleWeightLogicChain weightLogicChain;
    @Resource
    private RuleLockLogicFilter ruleLockLogicFilter;

    @Before
    public void setUp() {
        // 策略装配 100001、100002、100003
        log.info("测试结果：{}", strategyArmory.assembleLotteryStrategy(100001L));
        log.info("测试结果：{}", strategyArmory.assembleLotteryStrategy(100002L));
        log.info("测试结果：{}", strategyArmory.assembleLotteryStrategy(100003L));

        // 通过反射 mock 规则中的值
        ReflectionTestUtils.setField(weightLogicChain, "userScore", 40500L);
        ReflectionTestUtils.setField(ruleLockLogicFilter, "userRaffleCount", 10L);
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

    /**
     * 次数错校验，抽奖n次后解锁。100003 策略，你可以通过调整 @Before 的 setUp 方法中个人抽奖次数来验证。比如最开始设置0，之后设置10
     * ReflectionTestUtils.setField(ruleLockLogicFilter, "userRaffleCount", 10L);
     */
    @Test
    public void test_raffle_center_rule_lock(){
        RaffleFactorEntity raffleFactorEntity = RaffleFactorEntity.builder()
                .userId("zhaoyushi")
                .strategyId(100003L)
                .build();

        RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(raffleFactorEntity);

        log.info("请求参数：{}", JSON.toJSONString(raffleFactorEntity));
        log.info("测试结果：{}", JSON.toJSONString(raffleAwardEntity));
    }

}
