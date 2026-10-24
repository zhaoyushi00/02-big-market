package com.zhaoyushi.test.domain.activity;

import com.alibaba.fastjson.JSON;
import com.zhaoyushi.domain.activity.model.entity.ActivityOrderEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivityShopCartEntity;
import com.zhaoyushi.domain.activity.service.IRaffleOrder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author zhaoy
 * @date 2026/10/24
 * @description
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RaffleOrderTest {
    @Resource
    private IRaffleOrder raffleOrder;

    @Test
    public void test_createRaffleActivityOrder() {
        ActivityShopCartEntity activityShopCartEntity = new ActivityShopCartEntity();

        activityShopCartEntity.setUserId("zhaoy");
        activityShopCartEntity.setSku(9011L);

        ActivityOrderEntity raffleActivityOrder = raffleOrder.createRaffleActivityOrder(activityShopCartEntity);
        log.info("测试结果：{}", JSON.toJSONString(raffleActivityOrder));
    }
}
