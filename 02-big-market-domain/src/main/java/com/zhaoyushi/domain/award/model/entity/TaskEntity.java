// 声明当前类所属的包路径：领域层 - 奖品模块 - 实体对象
package com.zhaoyushi.domain.award.model.entity;

import com.zhaoyushi.domain.award.event.SendAwardMessageEvent;
import com.zhaoyushi.domain.award.model.valobj.TaskStateVO;
import com.zhaoyushi.types.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务实体对象（领域层 - 奖品模块）
 *
 * <p>在「写入中奖记录」场景下，任务实体承载一条待发送的发奖 MQ 消息：
 * 消息内容（message）、消息 ID（messageId）、目标主题（topic）、所属用户（userId）
 * 以及任务状态（state）。它与用户中奖记录实体一同构成聚合，同事务落库。
 *
 * @author zhaoy
 * @date 2026/11/4
 * @description 任务实体对象
 */
// 使用 Lombok 自动生成 getter/setter/toString/equals/hashCode 等方法
@Data
// 使用 Lombok 生成建造者模式，支持链式构建
@Builder
// 使用 Lombok 生成包含全部字段的构造方法
@AllArgsConstructor
// 使用 Lombok 生成无参构造方法
@NoArgsConstructor
public class TaskEntity {
    // 用户ID：分库分表路由键，标识这条任务归属于哪个用户
    private String userId;
    // 消息主题：MQ 的交换机/队列名，决定消息发送到哪个队列
    private String topic;
    // 消息编号：消息的唯一标识，与 user_id 共同定位一条任务记录
    private String messageId;
    // 消息主体：统一结构的事件消息（含 id、时间戳、发奖数据）
    private BaseEvent.EventMessage<SendAwardMessageEvent.SendAwardMessage> message;
    // 任务状态：create-创建、complete-完成、fail-失败
    private TaskStateVO state;
}
