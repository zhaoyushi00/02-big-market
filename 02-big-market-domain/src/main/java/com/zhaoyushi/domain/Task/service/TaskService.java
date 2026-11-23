// 声明当前类所属的包路径：领域层 - 任务模块 - 服务实现
package com.zhaoyushi.domain.Task.service;

import com.zhaoyushi.domain.Task.model.entity.TaskEntity;
import com.zhaoyushi.domain.Task.repository.ITaskRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 消息任务服务实现（领域层 - 任务模块）
 *
 * <p>消息任务补偿机制的应用服务：对上层（定时任务）暴露「查询待发送任务、
 * 发送消息、更新任务状态」能力，内部全部委托给仓储接口 {@link ITaskRepository} 完成，
 * 自身不包含业务规则，仅做职责转发。
 *
 * @author zhaoy
 * @date 2026/11/4
 * @description 消息任务服务实现
 */
// 注册为 Spring 服务 Bean
@Service
public class TaskService implements ITaskService{
    // 注入任务仓储接口实现，负责底层数据访问与消息发送
    @Resource
    private ITaskRepository taskRepository;

    /**
     * 查询未发送的消息任务列表
     *
     * <p>直接委托仓储层查询发送失败或超时未更新的任务。
     *
     * @return 未发送的任务消息列表
     */
    @Override
    public List<TaskEntity> queryNoSendMessageTaskList() {
        // 调用仓储层查询并返回未发送的任务列表
        return taskRepository.queryNoSendMessageTaskList();
    }

    /**
     * 发送消息
     *
     * <p>直接委托仓储层将任务消息投递到 MQ。
     *
     * @param taskEntity 待发送的任务实体
     */
    @Override
    public void sendMessage(TaskEntity taskEntity) {
        // 调用仓储层发送该任务对应的消息
        taskRepository.sendMessage(taskEntity);
    }

    /**
     * 更新任务发送状态为「完成」
     *
     * @param userId    用户ID（分库分表路由键）
     * @param messageId 消息编号
     */
    @Override
    public void updateTaskSendMessageCompleted(String userId, String messageId) {
        // 调用仓储层将该任务标记为发送完成
        taskRepository.updateTaskSendMessageCompleted(userId, messageId);
    }

    /**
     * 更新任务发送状态为「失败」
     *
     * @param userId    用户ID（分库分表路由键）
     * @param messageId 消息编号
     */
    @Override
    public void updateTaskSendMessageFail(String userId, String messageId) {
        // 调用仓储层将该任务标记为发送失败
        taskRepository.updateTaskSendMessageFail(userId, messageId);
    }
}
