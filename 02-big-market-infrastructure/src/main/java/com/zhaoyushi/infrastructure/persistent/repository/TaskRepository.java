// 声明当前类所属的包路径：基础设施层 - 持久化 - 仓储实现
package com.zhaoyushi.infrastructure.persistent.repository;

// 引入任务实体对象（领域层），作为方法入参/返回值类型
import com.zhaoyushi.domain.Task.model.entity.TaskEntity;
// 引入任务仓储接口，本类实现该接口
import com.zhaoyushi.domain.Task.repository.ITaskRepository;
// 引入消息发送器，用于向 MQ 投递消息
import com.zhaoyushi.infrastructure.event.EventPublisher;
// 引入任务表 DAO，用于任务的增删改查
import com.zhaoyushi.infrastructure.persistent.dao.ITaskDao;
// 引入任务表持久化对象，作为 DAO 的入参
import com.zhaoyushi.infrastructure.persistent.po.Task;
// 引入 JSR-250 的 @Resource 注解，用于按名称注入依赖
import jakarta.annotation.Resource;
// 引入 @Repository 注解，将本类注册为 Spring 仓储 Bean
import org.springframework.stereotype.Repository;

// 引入 ArrayList，用于构建任务实体列表
import java.util.ArrayList;
// 引入 List 集合，作为查询结果的返回类型
import java.util.List;

/**
 * 任务服务仓储实现（基础设施层 - 持久化）
 *
 * <p>「消息任务补偿」的仓储实现：
 * 1. 查询未发送（失败或超时）的任务列表，将 PO 转换为领域实体；
 * 2. 发送任务消息到 MQ；
 * 3. 更新任务发送状态（完成/失败）。
 *
 * @author zhaoy
 * @date 2026/11/4
 * @description 任务服务仓储实现
 */
// 注册为 Spring 仓储 Bean
@Repository
public class TaskRepository implements ITaskRepository {

    // 注入任务表 DAO，用于任务的增删改查
    @Resource
    private ITaskDao taskDao;
    // 注入消息发送器，用于向 MQ 投递消息
    @Resource
    private EventPublisher eventPublisher;

    /**
     * 查询未发送的消息任务列表
     *
     * <p>查询发送失败或超时未更新的任务，并将持久化对象（PO）转换为领域实体后返回。
     *
     * @return 未发送的任务消息列表
     */
    @Override
    public List<TaskEntity> queryNoSendMessageTaskList() {
        // 1. 调用 DAO 查询未发送的任务持久化对象列表
        List<Task> tasks = taskDao.queryNoSendMessageTaskList();
        // 2. 预创建领域实体列表，容量与查询结果一致，避免扩容开销
        List<TaskEntity> taskEntities = new ArrayList<>(tasks.size());
        // 3. 遍历每一个持久化对象，逐个转换为领域实体
        for (Task task : tasks) {
            // 3.1 新建一个任务实体对象
            TaskEntity taskEntity = new TaskEntity();
            // 3.2 设置用户 ID
            taskEntity.setUserId(task.getUserId());
            // 3.3 设置消息主题
            taskEntity.setTopic(task.getTopic());
            // 3.4 设置消息编号
            taskEntity.setMessageId(task.getMessageId());
            // 3.5 设置消息主体（数据库中的 JSON 字符串）
            taskEntity.setMessage(task.getMessage());
            // 3.6 将转换后的实体加入结果列表
            taskEntities.add(taskEntity);
        }
        // 4. 返回转换完成的任务实体列表
        return taskEntities;
    }

    /**
     * 发送消息
     *
     * <p>将任务中携带的消息内容（JSON 字符串）投递到指定 MQ topic。
     *
     * @param taskEntity 待发送的任务实体
     */
    @Override
    public void sendMessage(TaskEntity taskEntity) {
        // 调用消息发送器，将消息内容发送到任务指定的 topic
        eventPublisher.publish(taskEntity.getTopic(), taskEntity.getMessage());
    }

    /**
     * 更新任务发送状态为「完成」
     *
     * @param userId    用户ID（分库分表路由键）
     * @param messageId 消息编号
     */
    @Override
    public void updateTaskSendMessageCompleted(String userId, String messageId) {
        // 1. 新建任务持久化对象，作为更新条件
        Task taskReq = new Task();
        // 2. 设置用户 ID（定位分库分表）
        taskReq.setUserId(userId);
        // 3. 设置消息编号（与用户 ID 共同定位任务记录）
        taskReq.setMessageId(messageId);
        // 4. 调用 DAO 将任务状态更新为「完成」
        taskDao.updateTaskSendMessageCompleted(taskReq);
    }

    /**
     * 更新任务发送状态为「失败」
     *
     * @param userId    用户ID（分库分表路由键）
     * @param messageId 消息编号
     */
    @Override
    public void updateTaskSendMessageFail(String userId, String messageId) {
        // 1. 新建任务持久化对象，作为更新条件
        Task taskReq = new Task();
        // 2. 设置用户 ID（定位分库分表）
        taskReq.setUserId(userId);
        // 3. 设置消息编号（与用户 ID 共同定位任务记录）
        taskReq.setMessageId(messageId);
        // 4. 调用 DAO 将任务状态更新为「失败」
        taskDao.updateTaskSendMessageFail(taskReq);
    }
}
