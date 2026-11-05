// 声明当前类所属的包路径：测试模块 - 领域层 - 奖品
package com.zhaoyushi.test.domain.award;

// 引入用户中奖记录实体对象，用于构造测试入参
import com.zhaoyushi.domain.award.model.entity.UserAwardRecordEntity;
// 引入奖品状态枚举值对象，用于设置中奖记录的初始状态
import com.zhaoyushi.domain.award.model.valobj.AwardStateVO;
// 引入奖品服务接口，作为被测对象
import com.zhaoyushi.domain.award.service.IAwardService;
// 引入 JSR-250 的 @Resource 注解，用于按名称注入依赖
import jakarta.annotation.Resource;
// 引入 Lombok 的 @Slf4j 注解，自动生成日志对象 log
import lombok.extern.slf4j.Slf4j;
// 引入 RandomStringUtils 工具，用于生成随机订单号
import org.apache.commons.lang3.RandomStringUtils;
// 引入 JUnit 4 的 @Test 注解，标记测试方法
import org.junit.Test;
// 引入 JUnit 4 的 @RunWith 注解，指定测试运行器
import org.junit.runner.RunWith;
// 引入 @SpringBootTest 注解，启动 Spring 上下文进行集成测试
import org.springframework.boot.test.context.SpringBootTest;
// 引入 SpringRunner，使 Spring 测试上下文与 JUnit 4 配合工作
import org.springframework.test.context.junit4.SpringRunner;

// 引入 java.util.Date，作为中奖时间字段的类型
import java.util.Date;
// 引入 CountDownLatch，用于阻塞主线程等待异步任务执行
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

}
