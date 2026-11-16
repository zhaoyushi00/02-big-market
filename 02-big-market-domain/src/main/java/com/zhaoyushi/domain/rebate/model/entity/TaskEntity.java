package com.zhaoyushi.domain.rebate.model.entity;

import com.zhaoyushi.domain.rebate.event.SendRebateMessageEvent;
import com.zhaoyushi.domain.rebate.model.valobj.TaskStateVO;
import com.zhaoyushi.types.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息任务实体（领域层）
 *
 * <p>描述一条待发送的发奖 MQ 消息任务，对应数据库 task 表：
 * 消息任务与返利订单在同一事务内落库；事务外投递 MQ 成功后标记「完成」，
 * 失败则标记「失败」，由定时任务扫描重发，实现消息最终一致。
 *
 * @author zhaoy
 * @date 2026/11/14
 * @description 消息任务实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskEntity {

    /** 用户ID（任务归属用户） */
    private String userId;
    /** 消息主题（MQ topic 名称） */
    private String topic;
    /** 消息编号（消息唯一 ID，用于追踪与去重） */
    private String messageId;
    /** 消息主体（统一的 MQ 消息结构，含 id/时间戳/数据） */
    private BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage> message;
    /** 任务状态；create-创建、complete-完成、fail-失败 */
    private TaskStateVO state;

}
