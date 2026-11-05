// 声明当前类所属的包路径：领域层 - 任务模块 - 实体对象
package com.zhaoyushi.domain.Task.model.entity;

// 引入 Lombok 的 @Data 注解，自动生成 getter/setter/toString/equals/hashCode 方法
import lombok.Data;

/**
 * 任务实体对象（领域层 - 任务模块）
 *
 * <p>用于描述一条待发送/待补偿的 MQ 消息任务，是「消息任务补偿机制」的核心载体：
 * 抽奖中奖时会在同一事务内落一条任务记录，随后定时任务扫描未发送成功的任务并重发消息，
 * 从而保证「写入中奖记录」与「发送发奖 MQ」之间的最终一致性。
 *
 * @author zhaoy
 * @date 2026/11/4
 * @description 任务实体对象
 */
// 使用 Lombok 自动生成 getter/setter 等方法
@Data
public class TaskEntity {
    // 用户ID：分库分表路由键，同时标识这条任务归属于哪个用户
    private String userId;
    // 消息主题：MQ 的交换机/队列名，决定消息最终被发送到哪个队列
    private String topic;
    // 消息编号：消息的唯一标识，与 user_id 共同定位一条任务记录
    private String messageId;
    // 消息主体：序列化后的消息内容（JSON 字符串），发送时直接投递给 MQ
    private String message;
}
