package com.zhaoyushi.test.domain.redate;

import com.alibaba.fastjson2.JSON;
import com.zhaoyushi.domain.rebate.model.entity.BehaviorEntity;
import com.zhaoyushi.domain.rebate.model.valobj.BehaviorTypeVO;
import com.zhaoyushi.domain.rebate.service.IBehaviorRebateService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * 行为返利单测
 *
 * <p>验证 {@link IBehaviorRebateService#createOrder} 在真实环境（含分库分表、事务、MQ）下
 * 的完整入账流程：查询返利配置 -> 生成订单 -> 落库 -> 投递发奖 MQ。
 *
 * @author zhaoy
 * @date 2026/11/15
 * @description 行为返利单测
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class BehaviorRebateServiceTest {

    // 注入行为返利服务，执行待测的入账逻辑
    @Resource
    private IBehaviorRebateService behaviorRebateService;

    @Test
    public void test_createOrder() {
        // 构建行为实体：用户 zhaoyushi 执行签到行为
        BehaviorEntity behaviorEntity = new BehaviorEntity();
        behaviorEntity.setUserId("zhaoyushi");
        behaviorEntity.setBehaviorTypeVO(BehaviorTypeVO.SIGN);
        // 重复的 OutBusinessNo 会报错唯一索引冲突，这也是保证幂等的手段，确保不会多记账
        behaviorEntity.setOutBusinessNo("261115");

        // 执行返利入账，返回本次行为命中的所有返利订单 ID
        List<String> orderIds = behaviorRebateService.createOrder(behaviorEntity);
        // 打印请求参数与测试结果，便于观察验证
        log.info("请求参数：{}", JSON.toJSONString(behaviorEntity));
        log.info("测试结果：{}", JSON.toJSONString(orderIds));
    }

}
