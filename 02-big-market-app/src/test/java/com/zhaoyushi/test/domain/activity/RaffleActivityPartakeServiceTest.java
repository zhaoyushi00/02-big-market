package com.zhaoyushi.test.domain.activity;

import com.alibaba.fastjson.JSON;
import com.zhaoyushi.domain.activity.model.entity.PartakeRaffleActivityEntity;
import com.zhaoyushi.domain.activity.model.entity.UserRaffleOrderEntity;
import com.zhaoyushi.domain.activity.service.IRaffleActivityPartakeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 抽奖活动参与服务测试类
 *
 * <p>验证 {@link IRaffleActivityPartakeService#createOrder} 的参与活动流程：
 * 校验活动状态/日期 -> 查未使用订单（幂等） -> 扣减总/月/日账户额度 -> 生成抽奖单。
 *
 * @author zhaoy
 * @date 2026/11/3
 * @description 抽奖活动参与服务测试
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RaffleActivityPartakeServiceTest {

    /** 注入抽奖活动参与服务 */
    @Resource
    private IRaffleActivityPartakeService raffleActivityPartakeService;

    /**
     * 测试创建抽奖单
     *
     * <p>第一次调用：扣减账户额度并生成一张 create 状态的抽奖单；
     * 重复调用：因已存在未消费订单，直接返回已有订单，不再扣额度。
     */
    @Test
    public void test_createOrder() {
        // 请求参数：组装参与活动入参（用户 zhaoy + 活动 100301）
        PartakeRaffleActivityEntity partakeRaffleActivityEntity = new PartakeRaffleActivityEntity();
        partakeRaffleActivityEntity.setUserId("zhaoy");
        partakeRaffleActivityEntity.setActivityId(100301L);
        // 调用接口：创建抽奖单
        UserRaffleOrderEntity userRaffleOrder = raffleActivityPartakeService.createOrder(partakeRaffleActivityEntity);
        log.info("请求参数：{}", JSON.toJSONString(partakeRaffleActivityEntity));
        log.info("测试结果：{}", JSON.toJSONString(userRaffleOrder));
    }

}
