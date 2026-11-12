package com.zhaoyushi.test.trigger;

import com.alibaba.fastjson.JSON;
import com.zhaoyushi.trigger.api.IRaffleStrategyService;
import com.zhaoyushi.trigger.api.dto.RaffleAwardListRequestDTO;
import com.zhaoyushi.trigger.api.dto.RaffleAwardListResponseDTO;
import com.zhaoyushi.trigger.api.response.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * 营销抽奖服务测试
 * <p>通过直接注入触发器服务接口（IRaffleStrategyService）调用查询奖品列表接口，
 * 验证「按活动维度查询奖品 + 计算各奖品解锁状态」的新链路。
 *
 * @author zhaoy
 * @date 2026/11/12
 * @description 营销抽奖服务测试
 */
// 启用 Lombok 日志
@Slf4j
// 使用 SpringRunner 运行测试，使 @Resource 注入等 Spring 能力生效
@RunWith(SpringRunner.class)
// 启动完整 Spring 上下文，进行集成测试
@SpringBootTest
public class RaffleStrategyControllerTest {
    // 注入抽奖策略服务（触发器层服务接口），用于调用查询奖品列表接口
    @Resource
    private IRaffleStrategyService raffleStrategyService;

    /**
     * 测试查询奖品列表：按活动 ID + 用户 ID 查询，并校验各奖品的解锁状态字段
     */
    @Test
    public void test_queryRaffleAwardList() {
        // 构建查询奖品列表请求 DTO
        RaffleAwardListRequestDTO request = new RaffleAwardListRequestDTO();
        // 设置用户 ID（用于计算该用户已抽奖次数，判断奖品解锁状态）
        request.setUserId("xiaofuge");
        // 设置活动 ID（活动与策略 1:1 绑定，内部反查策略）
        request.setActivityId(100301L);
        // 调用查询奖品列表接口，返回含解锁状态信息的奖品列表
        Response<List<RaffleAwardListResponseDTO>> response = raffleStrategyService.queryRaffleAwardList(request);

        // 打印请求参数，便于对照结果
        log.info("请求参数：{}", JSON.toJSONString(request));
        // 打印查询结果（含 awardRuleLockCount / isAwardUnlock / waitUnlockCount）
        log.info("测试结果：{}", JSON.toJSONString(response));
    }
}
