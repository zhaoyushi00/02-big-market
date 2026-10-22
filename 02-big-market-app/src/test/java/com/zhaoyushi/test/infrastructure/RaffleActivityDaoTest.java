package com.zhaoyushi.test.infrastructure;

import com.alibaba.fastjson2.JSON;
import com.zhaoyushi.infrastructure.persistent.dao.IRaffleActivityDao;
import com.zhaoyushi.infrastructure.persistent.po.RaffleActivity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author zhaoy
 * @date 2026/10/21
 * @description 抽奖配置类Dao测试
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RaffleActivityDaoTest {

    @Resource
    private IRaffleActivityDao raffleActivityDao;

    @Test
    public void test_queryRaffleActivityByActivityId(){
        RaffleActivity raffleActivity = raffleActivityDao.queryRaffleActivityByActivityId(100301L);
        log.info("测试结果:{}", JSON.toJSON(raffleActivity));
    }

}
