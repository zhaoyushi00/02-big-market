package com.zhaoyushi.test.infrastructure;

import com.alibaba.fastjson2.JSON;
import com.zhaoyushi.infrastructure.persistent.dao.IRaffleActivityDao;
import com.zhaoyushi.infrastructure.persistent.dao.IRaffleActivityOrderDao;
import com.zhaoyushi.infrastructure.persistent.po.RaffleActivity;
import com.zhaoyushi.infrastructure.persistent.po.RaffleActivityOrder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.jeasy.random.EasyRandom;

import java.util.Date;
import java.util.List;

/**
 * @author zhaoy
 * @date 2026/10/22
 * @description 抽奖活动订单测试
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RaffleActivityOrderDaoTest {

    @Resource
    private IRaffleActivityOrderDao raffleActivityOrderDao;
    private final EasyRandom easyRandom = new EasyRandom();
    @Resource
    private IRaffleActivityDao raffleActivityDao;

    @Test
    public void test_insert_random() {
        for (int i = 0; i < 5; i++) {
            RaffleActivityOrder raffleActivityOrder = new RaffleActivityOrder();
            // EasyRandom 可以通过指定对象类的方式，随机生成对象值。如；easyRandom.nextObject(String.class)、easyRandom.nextObject(RaffleActivityOrder.class)
            raffleActivityOrder.setUserId(easyRandom.nextObject(String.class));
            raffleActivityOrder.setActivityId(100301L);
            raffleActivityOrder.setActivityName("测试活动");
            raffleActivityOrder.setStrategyId(100006L);
            raffleActivityOrder.setOrderId(org.apache.commons.lang.RandomStringUtils.randomNumeric(12));
            raffleActivityOrder.setOrderTime(new Date());
            raffleActivityOrder.setState("not_used");
            // 插入数据
            raffleActivityOrderDao.insert(raffleActivityOrder);
        }
    }

    @Test
    public void test_insert(){
        RaffleActivityOrder raffleActivityOrder = new RaffleActivityOrder();
        raffleActivityOrder.setUserId("zhaoyushi");
        raffleActivityOrder.setActivityId(100301L);
        raffleActivityOrder.setActivityName("测试活动");
        raffleActivityOrder.setStrategyId(100006L);
        raffleActivityOrder.setOrderId(RandomStringUtils.randomNumeric(12));
        raffleActivityOrder.setOrderTime(new Date());
        raffleActivityOrder.setState("not_used");
        // 插入数据
        raffleActivityOrderDao.insert(raffleActivityOrder);

    }

    @Test
    public void test_queryRaffleActivityByActivityId(){
        List<RaffleActivityOrder> zhaoyushi = raffleActivityOrderDao.queryRaffleActivityOrderByUserId("zhaoyushi");
        log.info("测试结果:{}", JSON.toJSON(zhaoyushi));
    }

}
