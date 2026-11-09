// 声明当前类所属的包路径：基础设施层 - 持久化 - 仓储实现
package com.zhaoyushi.infrastructure.persistent.repository;

// 引入分库分表路由策略接口，用于手动指定 SQL 落库的分库索引
import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
// 引入 fastjson2 的 JSON 工具，用于将消息对象序列化为 JSON 字符串
import com.alibaba.fastjson2.JSON;
// 引入用户中奖记录聚合对象，作为仓储方法的入参
import com.zhaoyushi.domain.award.model.aggregate.UserAwardRecordAggregate;
// 引入任务实体对象（领域层），从中提取消息、主题、状态等信息
import com.zhaoyushi.domain.award.model.entity.TaskEntity;
// 引入用户中奖记录实体对象（领域层），从中提取中奖明细信息
import com.zhaoyushi.domain.award.model.entity.UserAwardRecordEntity;
// 引入奖品仓储接口，本类实现该接口
import com.zhaoyushi.domain.award.repository.IAwardRepository;
// 引入消息发送器，用于向 MQ 投递发奖消息
import com.zhaoyushi.infrastructure.event.EventPublisher;
// 引入任务表 DAO，用于任务的增删改查
import com.zhaoyushi.infrastructure.persistent.dao.ITaskDao;
// 引入用户中奖记录表 DAO，用于中奖记录的写入
import com.zhaoyushi.infrastructure.persistent.dao.IUserAwardRecordDao;
// 引入用户抽奖订单表 DAO，用于更新抽奖单状态为已使用
import com.zhaoyushi.infrastructure.persistent.dao.IUserRaffleOrderDao;
// 引入任务表持久化对象，作为 DAO 的入参
import com.zhaoyushi.infrastructure.persistent.po.Task;
// 引入用户中奖记录表持久化对象，作为 DAO 的入参
import com.zhaoyushi.infrastructure.persistent.po.UserAwardRecord;
// 引入用户抽奖订单表持久化对象，作为更新抽奖单状态的入参
import com.zhaoyushi.infrastructure.persistent.po.UserRaffleOrder;
// 引入响应码枚举，用于抛出业务异常时携带错误码
import com.zhaoyushi.types.enums.ResponseCode;
// 引入应用异常类，用于抛出业务异常
import com.zhaoyushi.types.exception.AppException;
// 引入 JSR-250 的 @Resource 注解，用于按名称注入依赖
import jakarta.annotation.Resource;
// 引入 Lombok 的 @Slf4j 注解，自动生成日志对象 log
import lombok.extern.slf4j.Slf4j;
// 引入 Spring 的唯一键冲突异常，用于捕获数据库唯一索引冲突
import org.springframework.dao.DuplicateKeyException;
// 引入 @Repository 注解，将本类注册为 Spring 仓储 Bean
import org.springframework.stereotype.Repository;
// 引入事务模板，用于编程式控制事务的提交与回滚
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 奖品仓储服务（基础设施层 - 持久化）
 *
 * <p>「写入中奖记录」的仓储实现，承担真正的落库与发消息动作：
 * 1. 将领域实体转换为持久化对象（PO）；
 * 2. 在同一事务内同时写入「用户中奖记录」与「消息任务」两张表；
 * 3. 事务提交后（事务外）发送发奖 MQ，成功则更新任务为完成，失败则更新为失败等待补偿。
 *
 * @author zhaoy
 * @date 2026/11/4
 * @description 奖品仓储服务
 */
// 使用 Lombok 生成日志对象 log
@Slf4j
// 注册为 Spring 仓储 Bean
@Repository
public class AwardRepository implements IAwardRepository {

    // 注入任务表 DAO，用于写入/更新任务记录
    @Resource
    private ITaskDao taskDao;
    // 注入用户中奖记录表 DAO，用于写入中奖记录
    @Resource
    private IUserAwardRecordDao userAwardRecordDao;
    // 注入分库分表路由策略，用于手动路由到正确的分库
    @Resource
    private IDBRouterStrategy dbRouter;
    // 注入事务模板，用于编程式控制事务边界与回滚
    @Resource
    private TransactionTemplate transactionTemplate;
    // 注入消息发送器，用于向 MQ 投递发奖消息
    @Resource
    private EventPublisher eventPublisher;
    // 注入用户抽奖订单表 DAO，用于将抽奖单状态更新为已使用（防重复抽奖）
    @Resource
    private IUserRaffleOrderDao userRaffleOrderDao;

    /**
     * 保存用户中奖记录（聚合）
     *
     * <p>在同一个事务内写入中奖记录与任务记录，事务提交后发送发奖 MQ，
     * 并根据发送结果更新任务状态。
     *
     * @param userAwardRecordAggregate 用户中奖记录聚合对象
     */
    @Override
    public void saveUserAwardRecord(UserAwardRecordAggregate userAwardRecordAggregate) {
        // 1. 从聚合对象中取出任务实体
        TaskEntity taskEntity = userAwardRecordAggregate.getTaskEntity();
        // 2. 从聚合对象中取出用户中奖记录实体
        UserAwardRecordEntity userAwardRecordEntity = userAwardRecordAggregate.getUserAwardRecordEntity();
        // 3. 提取活动 ID（用于日志记录）
        Long activityId = userAwardRecordEntity.getActivityId();
        // 4. 提取奖品 ID（用于日志记录）
        Integer awardId = userAwardRecordEntity.getAwardId();
        // 5. 提取用户 ID（用于分库路由与日志记录）
        String userId = userAwardRecordEntity.getUserId();

        // 6. 构建用户中奖记录持久化对象（PO），准备落库
        UserAwardRecord userAwardRecord = new UserAwardRecord();
        // 设置用户 ID
        userAwardRecord.setUserId(userAwardRecordEntity.getUserId());
        // 设置活动 ID
        userAwardRecord.setActivityId(userAwardRecordEntity.getActivityId());
        // 设置抽奖策略 ID
        userAwardRecord.setStrategyId(userAwardRecordEntity.getStrategyId());
        // 设置抽奖订单 ID（幂等键）
        userAwardRecord.setOrderId(userAwardRecordEntity.getOrderId());
        // 设置奖品 ID
        userAwardRecord.setAwardId(userAwardRecordEntity.getAwardId());
        // 设置奖品标题
        userAwardRecord.setAwardTitle(userAwardRecordEntity.getAwardTitle());
        // 设置中奖时间
        userAwardRecord.setAwardTime(userAwardRecordEntity.getAwardTime());
        // 设置奖品状态（取枚举的 code 值落库）
        userAwardRecord.setAwardState(userAwardRecordEntity.getAwardState().getCode());

        // 7. 构建任务持久化对象（PO），准备落库
        Task task = new Task();
        // 设置用户 ID（分库分表路由键）
        task.setUserId(taskEntity.getUserId());
        // 设置消息主题
        task.setTopic(taskEntity.getTopic());
        // 设置消息编号
        task.setMessageId(taskEntity.getMessageId());
        // 设置消息主体：将事件消息对象序列化为 JSON 字符串后落库
        task.setMessage(JSON.toJSONString(taskEntity.getMessage()));
        // 设置任务状态（取枚举的 code 值落库，初始为 create）
        task.setState(taskEntity.getState().getCode());

        // 构建抽奖单更新条件对象（PO），用于把对应抽奖单状态更新为已使用
        UserRaffleOrder userRaffleOrderReq = new UserRaffleOrder();
        // 设置用户 ID（更新条件之一）
        userRaffleOrderReq.setUserId(userAwardRecordEntity.getUserId());
        // 设置订单 ID（更新条件之二）
        userRaffleOrderReq.setOrderId(userAwardRecordEntity.getOrderId());

        // 8. 开启分库路由 + 事务，保证两张表写入的一致性
        try {
            // 8.1 根据用户 ID 计算并指定本次 SQL 落库的分库
            dbRouter.doRouter(userId);
            // 8.2 在事务内执行写入逻辑（status 是事务状态对象，用来手动控制回滚）
            transactionTemplate.execute(status -> {
                try {
                    // 8.3 写入用户中奖记录
                    userAwardRecordDao.insert(userAwardRecord);
                    // 8.4 写入任务记录
                    taskDao.insert(task);
                    // 更新抽奖单状态为已使用：将 order_state 从 create 更新为 used
                    int count = userRaffleOrderDao.updateUserRaffleOrderStateUsed(userRaffleOrderReq);
                    // 更新行数不为 1 说明抽奖单已非 create 状态（已被使用过）
                    if (1 != count) {
                        // 标记事务回滚，撤销已写入的中奖记录与任务记录
                        status.setRollbackOnly();
                        // 记录重复抽奖的异常日志
                        log.error("写入中奖记录，用户抽奖单已使用过，不可重复抽奖 userId: {} activityId: {} awardId: {}", userId, activityId, awardId);
                        // 抛出业务异常，提示抽奖单已使用
                        throw new AppException(ResponseCode.ACTIVITY_ORDER_ERROR.getCode(), ResponseCode.ACTIVITY_ORDER_ERROR.getInfo());
                    }
                    // 8.5 返回任意值，表示事务内逻辑执行成功（execute 需要返回值）
                    return 1;
                    // 8.6 捕获唯一索引冲突（如 order_id 幂等键重复）
                } catch (DuplicateKeyException e) {
                    /**
                     * 标记整个事务回滚
                     * 这一步非常重要：因为 userAwardRecordDao.insert 可能已经成功执行了
                     * 如果插入 task 时才撞上唯一索引冲突，此时第一条 SQL 其实已经"写进去"了（只是还没提交）
                     * 调用 setRollbackOnly() 就是告诉 Spring：这个事务不能提交，必须整体回滚，把第一条也一起撤销掉
                     */
                    status.setRollbackOnly();
                    // 记录唯一索引冲突日志
                    log.error("写入中奖记录，唯一索引冲突 userId: {} activityId: {} awardId: {}", userId, activityId, awardId, e);
                    // 抛出应用异常，交由上层统一处理
                    throw new AppException(ResponseCode.INDEX_DUP.getCode(), e);
                }
            });
        } finally {
            // 8.7 无论成功失败，清理分库路由上下文，避免污染后续请求
            dbRouter.clear();
        }

        // 9. 事务提交后，在事务外发送发奖 MQ（失败时靠任务表补偿重发）
        try {
            // 9.1 发送消息【在事务外执行，如果失败还有任务补偿】
            eventPublisher.publish(task.getTopic(), task.getMessage());
            // 9.2 发送成功，更新任务状态为「完成」
            taskDao.updateTaskSendMessageCompleted(task);
        } catch (Exception e) {
            // 9.3 发送失败，记录日志
            log.error("写入中将记录，发送MQ消息失败 userId:{} topic:{}", userId, task.getTopic());
            // 9.4 更新任务状态为「失败」，等待定时任务补偿重发
            taskDao.updateTaskSendMessageFail(task);
        }

    }
}
