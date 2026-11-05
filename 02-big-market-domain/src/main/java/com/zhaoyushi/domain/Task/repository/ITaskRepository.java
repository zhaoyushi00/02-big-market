// 声明当前类所属的包路径：领域层 - 任务模块 - 仓储接口
package com.zhaoyushi.domain.Task.repository;

// 引入任务实体对象，作为本接口方法的入参/返回值类型
import com.zhaoyushi.domain.Task.model.entity.TaskEntity;

// 引入 List 集合，用于返回任务列表
import java.util.List;

/**
 * 任务服务仓储接口（领域层 - 任务模块）
 *
 * <p>定义「消息任务」的持久化与发送边界，向上屏蔽底层实现（数据库、MQ），
 * 由基础设施层（infrastructure）提供具体实现。
 *
 * @author zhaoy
 * @date 2026/11/4
 * @description 任务服务仓储接口
 */
public interface ITaskRepository {

    /**
     * 查询未发送的消息任务列表
     *
     * <p>捞取发送失败（fail）以及超时未更新（create 且超过缓冲时间）的任务，
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
     * <p>消息发送成功后调用，将该任务标记为 completed，避免被重复扫描。
     *
     * @param userId    用户ID（分库分表路由键）
     * @param messageId 消息编号
     */
    void updateTaskSendMessageCompleted(String userId, String messageId);

    /**
     * 更新任务发送状态为「失败」
     *
     * <p>消息发送失败后调用，将该任务标记为 fail，交由下一次定时扫描重试。
     *
     * @param userId    用户ID（分库分表路由键）
     * @param messageId 消息编号
     */
    void updateTaskSendMessageFail(String userId, String messageId);

}
