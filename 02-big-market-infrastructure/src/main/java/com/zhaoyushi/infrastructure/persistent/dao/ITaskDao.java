// 声明当前类所属的包路径：基础设施层 - 持久化 - DAO
package com.zhaoyushi.infrastructure.persistent.dao;

// 引入分库分表路由注解，标注在更新方法上，自动按 userId 路由到正确的分库分表
import cn.bugstack.middleware.db.router.annotation.DBRouter;
// 引入任务表持久化对象，作为 DAO 方法的入参
import com.zhaoyushi.infrastructure.persistent.po.Task;
// 引入 MyBatis 的 @Mapper 注解，将本接口注册为 MyBatis Mapper
import org.apache.ibatis.annotations.Mapper;

// 引入 List 集合，作为查询结果的返回类型
import java.util.List;

/**
 * 任务表 DAO（数据访问接口）
 *
 * <p>定义 task 表（消息任务表）的增删改查操作，由 MyBatis 映射 XML 提供具体 SQL 实现。
 * 该表用于「消息任务补偿」：记录待发送的 MQ 消息及发送状态。
 *
 * @author zhaoy
 * @description 任务表，发送MQ
 * @create 2024-04-03 15:57
 */
// 注册为 MyBatis Mapper 接口
@Mapper
public interface ITaskDao {
    /**
     * 插入一条任务记录
     *
     * @param task 任务持久化对象
     */
    void insert(Task task);

    /**
     * 更新任务发送状态为「完成」
     *
     * <p>消息发送成功后调用，将 state 置为 completed。
     *
     * @param task 任务持久化对象（携带 userId 与 messageId 作为更新条件）
     */
    // 分库分表路由：自动按 userId 定位到正确的分库分表
    @DBRouter
    void updateTaskSendMessageCompleted(Task task);

    /**
     * 更新任务发送状态为「失败」
     *
     * <p>消息发送失败后调用，将 state 置为 fail，等待补偿重试。
     *
     * @param task 任务持久化对象（携带 userId 与 messageId 作为更新条件）
     */
    // 分库分表路由：自动按 userId 定位到正确的分库分表
    @DBRouter
    void updateTaskSendMessageFail(Task task);

    /**
     * 查询未发送的消息任务列表
     *
     * <p>捞取发送失败（fail）以及超时未更新（create 且超过缓冲时间）的任务。
     *
     * @return 未发送的任务持久化对象列表
     */
    List<Task> queryNoSendMessageTaskList();
}
