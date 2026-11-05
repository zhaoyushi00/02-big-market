// 声明当前类所属的包路径：领域层 - 任务模块 - 服务接口
package com.zhaoyushi.domain.Task.service;

// 引入任务实体对象，作为本接口方法的入参/返回值类型
import com.zhaoyushi.domain.Task.model.entity.TaskEntity;

// 引入 List 集合，用于返回任务列表
import java.util.List;

/**
 * 消息任务服务接口（领域层 - 任务模块）
 *
 * <p>定义「消息任务补偿」相关的领域服务能力：查询待发送任务、发送消息、
 * 以及更新任务发送状态，是定时任务与领域层的交互入口。
 *
 * @author zhaoy
 * @date 2026/11/4
 * @description 消息任务服务接口
 */
public interface ITaskService {
    /**
     * 查询发送 MQ 失败和超时未发送的 MQ
     *
     * <p>捞取发送失败（fail）以及创建后超过缓冲时间仍未更新（create 超时）的任务，
     * 供定时任务扫描后进行补偿重发。
     *
     * @return 未发送的任务消息列表
     */
    List<TaskEntity> queryNoSendMessageTaskList();

    /**
     * 发送消息
     *
     * <p>将任务中携带的消息内容投递到指定 MQ topic。
     *
     * @param taskEntity 待发送的任务实体
     */
    void sendMessage(TaskEntity taskEntity);

    /**
     * 更新任务发送状态为「完成」
     *
     * @param userId    用户ID（分库分表路由键）
     * @param messageId 消息编号
     */
    void updateTaskSendMessageCompleted(String userId, String messageId);

    /**
     * 更新任务发送状态为「失败」
     *
     * @param userId    用户ID（分库分表路由键）
     * @param messageId 消息编号
     */
    void updateTaskSendMessageFail(String userId, String messageId);
}
