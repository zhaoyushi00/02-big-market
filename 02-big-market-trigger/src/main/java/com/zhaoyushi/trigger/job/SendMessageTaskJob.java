// 声明当前类所属的包路径：触发器层 - 定时任务
package com.zhaoyushi.trigger.job;

// 引入任务实体对象（领域层），作为待发送消息的载体
import com.zhaoyushi.domain.Task.model.entity.TaskEntity;
// 引入消息任务服务接口，提供查询/发送/更新任务状态能力
import com.zhaoyushi.domain.Task.service.ITaskService;
// 引入 JSR-250 的 @Resource 注解，用于按名称注入依赖
import jakarta.annotation.Resource;
// 引入 Lombok 的 @Slf4j 注解，自动生成日志对象 log
import lombok.extern.slf4j.Slf4j;
// 引入 @Scheduled 注解，用于声明定时任务执行方法
import org.springframework.scheduling.annotation.Scheduled;
// 引入 @Component 注解，将本类注册为 Spring Bean
import org.springframework.stereotype.Component;
// 引入分库分表路由策略接口，用于手动切换分库分表
import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;

// 引入 List 集合，用于接收查询到的任务列表
import java.util.List;
// 引入线程池执行器，用于异步执行任务发送，提高吞吐
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 发送 MQ 消息任务队列（触发器层 - 定时任务）
 *
 * <p>「消息任务补偿机制」的核心调度器：每隔 5 秒扫描所有分库分表的 task 表，
 * 捞取发送失败或超时未发送的任务，并通过线程池异步重发 MQ 消息，
 * 发送成功/失败后分别更新任务状态，最终保证「写入中奖记录」与「发奖 MQ」最终一致。
 *
 * @author zhaoy
 * @date 2026/11/4
 * @description 发送MQ消息任务队列
 */
// 注册为 Spring 组件 Bean
@Component()
// 使用 Lombok 生成日志对象 log
@Slf4j
public class SendMessageTaskJob {

    // 注入消息任务服务，提供查询/发送/更新任务状态能力
    @Resource
    private ITaskService taskService;
    // 注入线程池执行器，用于异步执行扫描与发送，提高并发吞吐
    @Resource
    private ThreadPoolExecutor executor;
    // 注入分库分表路由策略，用于手动遍历每个分库分表
    @Resource
    private IDBRouterStrategy dbRouter;

    /**
     * 定时任务执行方法
     *
     * <p>cron 表达式：每 5 秒执行一次（秒 分 时 日 月 周）。
     * 主流程：遍历所有分库 -> 逐个分库查询未发送任务 -> 逐个任务异步发送并更新状态。
     */
    // cron 表达式：每 5 秒执行一次
    @Scheduled(cron = "0/5 * * * * ?")
    public void exec(){
        // 外层 try-catch 包裹整个流程，保证任何异常都不中断后续调度
        try {
            // 1. 获取分库数量（配置的分库总数，用于遍历）
            int dbCount = dbRouter.dbCount();
            // 2. 逐个扫描分库【每个分库各有一张任务表】
            for (int dbIdx = 1; dbIdx <= dbCount; dbIdx++) {
                // 2.1 定义 final 变量，供内部匿名类（Runnable）安全引用当前分库索引
                int finalDbIdx = dbIdx;
                // 2.2 提交线程池异步扫描当前分库，各分库并行处理，提高效率
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // 2.3 指定本次 SQL 落库的「分库」索引
                            dbRouter.setDBKey(finalDbIdx);
                            // 2.4 指定本次 SQL 落库的「分表」索引（task 表不分表，固定为 0）
                            dbRouter.setTBKey(0);
                            // 2.5 查询当前分库中未发送（失败或超时）的任务列表
                            List<TaskEntity> taskEntities = taskService.queryNoSendMessageTaskList();
                            // 2.6 任务列表为空则直接返回，等待下一次调度
                            if (taskEntities.isEmpty()) return;

                            // 2.7 遍历任务列表，逐个发送 MQ 消息
                            for (TaskEntity taskEntity : taskEntities){
                                // 2.8 再开线程异步发送，提高发送效率（线程池策略为 CallerRunsPolicy）
                                executor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            // 2.9 发送该任务对应的 MQ 消息
                                            taskService.sendMessage(taskEntity);
                                            // 2.10 发送成功，更新任务状态为「完成」
                                            taskService.updateTaskSendMessageCompleted(taskEntity.getUserId(), taskEntity.getMessageId());
                                        } catch (Exception e) {
                                            // 2.11 发送失败，记录错误日志
                                            log.error("定时任务，发送MQ消息失败 userId: {} topic: {}", taskEntity.getUserId(), taskEntity.getTopic());
                                            // 2.12 更新任务状态为「失败」，等待下一次补偿重试
                                            taskService.updateTaskSendMessageFail(taskEntity.getUserId(), taskEntity.getMessageId());
                                        }
                                    }
                                });
                            }

                        }finally {
                            // 2.13 无论成功失败，清理分库分表路由上下文，避免污染其他线程
                            dbRouter.clear();
                        }
                    }
                });
            }

        } catch (Exception e) {
            // 3. 记录整个扫描流程失败日志（含异常堆栈）
            log.error("定时任务，扫描MQ任务表发送消息失败。", e);
        }
    }

}
