package com.zhaoyushi.test.trigger;

import com.alibaba.fastjson.JSON;
import com.zhaoyushi.trigger.api.IRaffleActivityService;
import com.zhaoyushi.trigger.api.dto.ActivityDrawRequestDTO;
import com.zhaoyushi.trigger.api.dto.ActivityDrawResponseDTO;
import com.zhaoyushi.types.model.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 活动抽奖控制器测试
 * <p>通过直接注入触发器服务接口（IRaffleActivityService）调用活动装配与抽奖接口，
 * 验证活动维度抽奖链路的完整性与返回结果。
 *
 * @author zhaoy
 * @date 2026/11/12
 * @description 活动抽奖控制器测试
 */
// 启用 Lombok 日志
@Slf4j
// 使用 SpringRunner 运行测试，使 @Resource 注入等 Spring 能力生效
@RunWith(SpringRunner.class)
// 启动完整 Spring 上下文，进行集成测试
@SpringBootTest
public class RaffleActivityControllerTest {

    // 注入活动抽奖服务（触发器层服务接口），用于调用活动装配与抽奖接口
    @Resource
    private IRaffleActivityService raffleActivityService;

    /**
     * 测试活动装配：将活动 100301 的策略/SKU 数据预热到缓存
     */
    @Test
    public void test_armory() {
        // 调用活动装配接口，返回装配结果
        Response<Boolean> response = raffleActivityService.armory(100301L);
        // 打印装配结果，便于人工查看
        log.info("测试结果：{}", JSON.toJSONString(response));
    }

    /**
     * 测试活动抽奖：用户 xiaofuge 参与活动 100301 的抽奖
     */
    @Test
    public void test_draw() {
        // 构建抽奖请求 DTO
        ActivityDrawRequestDTO request = new ActivityDrawRequestDTO();
        // 设置活动 ID
        request.setActivityId(100301L);
        // 设置参与抽奖的用户 ID
        request.setUserId("xiaofuge");
        // 调用抽奖接口，返回抽奖结果
        Response<ActivityDrawResponseDTO> response = raffleActivityService.draw(request);

        // 打印请求参数，便于对照结果
        log.info("请求参数：{}", JSON.toJSONString(request));
        // 打印抽奖结果
        log.info("测试结果：{}", JSON.toJSONString(response));
    }

}
