package com.zhaoyushi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.alibaba.fastjson.JSON;
import com.zhaoyushi.domain.rebate.model.aggregate.BehaviorRebateAggregate;
import com.zhaoyushi.domain.rebate.model.entity.BehaviorRebateOrderEntity;
import com.zhaoyushi.domain.rebate.model.entity.TaskEntity;
import com.zhaoyushi.domain.rebate.model.valobj.BehaviorTypeVO;
import com.zhaoyushi.domain.rebate.model.valobj.DailyBehaviorRebateVO;
import com.zhaoyushi.domain.rebate.repository.IBehaviorRebateRepository;
import com.zhaoyushi.infrastructure.event.EventPublisher;
import com.zhaoyushi.infrastructure.persistent.dao.IDailyBehaviorRebateDao;
import com.zhaoyushi.infrastructure.persistent.dao.ITaskDao;
import com.zhaoyushi.infrastructure.persistent.dao.IUserBehaviorRebateOrderDao;
import com.zhaoyushi.infrastructure.persistent.po.DailyBehaviorRebate;
import com.zhaoyushi.infrastructure.persistent.po.Task;
import com.zhaoyushi.infrastructure.persistent.po.UserBehaviorRebateOrder;
import com.zhaoyushi.types.enums.ResponseCode;
import com.zhaoyushi.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * 行为返利服务仓储实现（基础设施层）
 *
 * <p>实现领域层仓储接口 {@link IBehaviorRebateRepository}，完成返利配置查询与返利记录落库：
 * 1. 查询返利配置：按行为类型从 daily_behavior_rebate 表取「开启中」配置，PO 转 VO 返回；
 * 2. 保存返利记录：按用户 ID 分库分表路由，在事务内写入「返利订单 + 消息任务」，
 *    事务外同步投递发奖 MQ，成功/失败分别更新任务状态，失败由定时任务补偿。
 *
 * @author zhaoy
 * @date 2026/11/15
 * @description 行为返利服务仓储实现
 */
@Repository
@Slf4j
public class BehaviorRebateRepository implements IBehaviorRebateRepository {

    // 注入日常行为返利配置 DAO：查询返利配置
    @Resource
    private IDailyBehaviorRebateDao dailyBehaviorRebateDao;
    // 注入用户行为返利订单 DAO：写入返利流水
    @Resource
    private IUserBehaviorRebateOrderDao userBehaviorRebateOrderDao;
    // 注入消息任务 DAO：写入/更新发奖消息任务状态
    @Resource
    private ITaskDao taskDao;
    // 注入分库分表路由策略：按用户 ID 路由到目标库表
    @Resource
    private IDBRouterStrategy dbRouter;
    // 注入编程式事务模板：保证订单与任务同库同事务
    @Resource
    private TransactionTemplate transactionTemplate;
    // 注入事件发布器：事务外投递发奖 MQ 消息
    @Resource
    private EventPublisher eventPublisher;

    /**
     * 查询日常行为返利配置
     *
     * @param behaviorTypeVO 行为类型值对象（sign 签到、openai_pay 支付）
     * @return 返利配置值对象列表（持久化对象 PO 转领域值对象 VO）
     */
    @Override
    public List<DailyBehaviorRebateVO> queryDailyBehaviorRebateConfig(BehaviorTypeVO behaviorTypeVO) {
        // 按行为类型编码查询配置表，得到持久化对象列表
        List<DailyBehaviorRebate> dailyBehaviorRebates = dailyBehaviorRebateDao.queryDailyBehaviorRebateByBehaviorType(behaviorTypeVO.getCode());
        // 预先分配与结果集等长的列表容量，避免扩容
        List<DailyBehaviorRebateVO> dailyBehaviorRebateVOS = new ArrayList<>(dailyBehaviorRebates.size());
        // 逐条把持久化对象（PO）转换为领域值对象（VO）
        for (DailyBehaviorRebate dailyBehaviorRebate : dailyBehaviorRebates) {
            dailyBehaviorRebateVOS.add(DailyBehaviorRebateVO.builder()
                    .behaviorType(dailyBehaviorRebate.getBehaviorType())
                    .rebateDesc(dailyBehaviorRebate.getRebateDesc())
                    .rebateType(dailyBehaviorRebate.getRebateType())
                    .rebateConfig(dailyBehaviorRebate.getRebateConfig())
                    .build());
        }
        return dailyBehaviorRebateVOS;
    }

    /**
     * 保存用户返利记录
     *
     * <p>分三步完成：
     * 1. 按用户 ID 进行分库分表路由；
     * 2. 事务内写入返利订单与消息任务（任一失败整体回滚，唯一索引冲突抛异常）；
     * 3. 事务外同步投递发奖 MQ，成功标记任务完成、失败标记任务失败（等待定时补偿）。
     *
     * @param userId                   用户 ID（分库分表路由键）
     * @param behaviorRebateAggregates 行为返利聚合对象列表
     */
    @Override
    public void saveUserRebateRecord(String userId, List<BehaviorRebateAggregate> behaviorRebateAggregates) {
        try {
            // 1、设置分库分表路由：根据用户 ID 路由到目标库表
            dbRouter.doRouter(userId);
            // 2、编程式事务内写入订单 + 任务，保证二者同库同事务
            transactionTemplate.execute(status -> {
                try {
                    // 2.1 遍历每个聚合对象，逐条落库
                    for (BehaviorRebateAggregate behaviorRebateAggregate : behaviorRebateAggregates){
                        // 取出返利订单实体，转换为持久化对象写入流水表
                        BehaviorRebateOrderEntity behaviorRebateOrderEntity = behaviorRebateAggregate.getBehaviorRebateOrderEntity();
                        // 用户行为返利订单对象（实体 -> 持久化对象）
                        UserBehaviorRebateOrder userBehaviorRebateOrder = new UserBehaviorRebateOrder();
                        userBehaviorRebateOrder.setUserId(behaviorRebateOrderEntity.getUserId());
                        userBehaviorRebateOrder.setOrderId(behaviorRebateOrderEntity.getOrderId());
                        userBehaviorRebateOrder.setBehaviorType(behaviorRebateOrderEntity.getBehaviorType());
                        userBehaviorRebateOrder.setRebateDesc(behaviorRebateOrderEntity.getRebateDesc());
                        userBehaviorRebateOrder.setRebateType(behaviorRebateOrderEntity.getRebateType());
                        userBehaviorRebateOrder.setRebateConfig(behaviorRebateOrderEntity.getRebateConfig());
                        userBehaviorRebateOrder.setBizId(behaviorRebateOrderEntity.getBizId());
                        userBehaviorRebateOrderDao.insert(userBehaviorRebateOrder);

                        // 取出消息任务实体，转换为持久化对象写入任务表
                        TaskEntity taskEntity = behaviorRebateAggregate.getTaskEntity();
                        Task task = new Task();
                        task.setUserId(taskEntity.getUserId());
                        task.setTopic(taskEntity.getTopic());
                        task.setMessageId(taskEntity.getMessageId());
                        task.setMessage(JSON.toJSONString(taskEntity.getMessage())); // 消息体序列化为 JSON 落库
                        task.setState(taskEntity.getState().getCode());              // 状态取枚举的 code
                        taskDao.insert(task);
                    }
                    return 1;
                }catch (DuplicateKeyException e){
                    // 唯一索引冲突（bizId 幂等防重），标记事务回滚并抛出业务异常
                    status.setRollbackOnly();
                    log.error("写入返利记录，唯一索引冲突 userId: {}", userId, e);
                    throw new AppException(ResponseCode.INDEX_DUP.getCode(), e);
                }
            });
        } finally {
            // 3、无论成败，清理分库分表路由上下文，避免污染其它线程
            dbRouter.clear();
        }

        // 4、同步发送MQ消息（事务外执行，发送失败任务表保留状态等待补偿）
        for (BehaviorRebateAggregate behaviorRebateAggregate : behaviorRebateAggregates) {
            // 取出任务实体，构建更新任务状态所需的持久化对象
            TaskEntity taskEntity = behaviorRebateAggregate.getTaskEntity();
            Task task = new Task();
            task.setUserId(taskEntity.getUserId());
            task.setMessageId(taskEntity.getMessageId());
            try {
                // 发送消息【在事务外执行，如果失败还有任务补偿】
                eventPublisher.publish(taskEntity.getTopic(), taskEntity.getMessage());
                // 发送成功，更新 task 任务表状态为「完成」
                taskDao.updateTaskSendMessageCompleted(task);
            } catch (Exception e) {
                // 发送失败，记录错误日志并将任务状态更新为「失败」
                log.error("写入返利记录，发送MQ消息失败 userId: {} topic: {}", userId, task.getTopic());
                taskDao.updateTaskSendMessageFail(task);
            }
        }

    }
}
