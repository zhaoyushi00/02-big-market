// 声明当前类所属的包路径：测试模块 - 领域层 - 奖品
package com.zhaoyushi.test.domain.award;

import com.zhaoyushi.domain.award.model.entity.DistributeAwardEntity;
import com.zhaoyushi.domain.award.model.entity.UserAwardRecordEntity;
import com.zhaoyushi.domain.award.model.valobj.AwardStateVO;
import com.zhaoyushi.domain.award.service.IAwardService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * 奖品服务测试类
 *
 * <p>集成测试：验证「写入用户中奖记录」的完整链路（落库 + 发奖 MQ 任务写入）。
 * 循环构造 100 条中奖记录并调用服务写入，覆盖正常写入与分库分表路由逻辑。
 *
 * @author zhaoy
 * @date 2026/11/5
 * @description 奖品服务测试
 */
// 使用 Lombok 生成日志对象 log
@Slf4j
// 使用 SpringRunner 运行 JUnit 测试
@RunWith(SpringRunner.class)
// 启动 Spring Boot 上下文，加载完整配置（含数据源、MyBatis、RabbitMQ 等）
@SpringBootTest
public class AwardServiceTest {

    // 注入奖品服务接口实现，作为被测对象
    @Resource
    private IAwardService awardService;

    /**
     * 测试写入用户中奖记录
     *
     * <p>循环 100 次，每次构造一条中奖记录并调用服务保存，覆盖重复写入与路由场景。
     *
     * @throws InterruptedException 线程休眠被中断时抛出
     */
    @Test
    public void test_saveUserAwardRecord() throws InterruptedException{
        // 1. 循环 100 次，批量写入中奖记录
        for (int i = 0; i < 100; i++) {
            // 2. 新建一条用户中奖记录实体对象
            UserAwardRecordEntity userAwardRecordEntity = new UserAwardRecordEntity();
            // 3. 设置用户 ID（固定测试用户，作为分库分表路由键）
            userAwardRecordEntity.setUserId("zhaoyu");
            // 4. 设置活动 ID
            userAwardRecordEntity.setActivityId(100301L);
            // 5. 设置抽奖策略 ID
            userAwardRecordEntity.setStrategyId(100006L);
            // 6. 设置订单 ID（随机 12 位数字，作为幂等键）
            userAwardRecordEntity.setOrderId(RandomStringUtils.randomNumeric(12));
            // 7. 设置奖品 ID
            userAwardRecordEntity.setAwardId(101);
            // 8. 设置奖品标题
            userAwardRecordEntity.setAwardTitle("OpenAI 增加使用次数");
            // 9. 设置中奖时间为当前时间
            userAwardRecordEntity.setAwardTime(new Date());
            // 10. 设置奖品初始状态为「创建」
            userAwardRecordEntity.setAwardState(AwardStateVO.create);
            // 11. 调用服务写入中奖记录（同事务落库 + 发奖 MQ 任务）
            awardService.saveUserAwardRecord(userAwardRecordEntity);
            // 12. 每次写入后休眠 500 毫秒，避免过快打满线程池/连接池
            Thread.sleep(500);
        }
        // 13. 阻塞主线程，等待异步发送 MQ 的线程执行完毕（演示用）
        new CountDownLatch(1).await();
    }

    /**
     * 测试分发积分奖品
     *
     * <p>构造一个分发奖品实体（积分范围 0.01,1），直接调用发奖服务，
     * 验证积分账户写入与发奖记录状态更新的完整链路。
     *
     * @throws InterruptedException 线程休眠被中断时抛出
     */
    @Test
    public void test_distributeAward() throws InterruptedException {
        // 创建分发奖品实体对象，用于承载发奖入参
        DistributeAwardEntity distributeAwardEntity = new DistributeAwardEntity();
        // 设置发奖目标用户ID
        distributeAwardEntity.setUserId("xiaofuge");
        // 设置发奖关联的订单ID
        distributeAwardEntity.setOrderId("690124733440");
        // 设置要发放的奖品ID
        distributeAwardEntity.setAwardId(101);
        // 设置奖品配置信息（0.01,1 表示随机积分范围，黑名单指定积分值）
        distributeAwardEntity.setAwardConfig("0.01,1");

        // 调用发奖服务执行积分发奖
        awardService.distributeAward(distributeAwardEntity);
    }


}
