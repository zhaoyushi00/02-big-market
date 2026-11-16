package com.zhaoyushi.domain.rebate.event;

import com.zhaoyushi.types.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 返利发奖消息事件（领域层）
 *
 * <p>继承基础事件抽象类 {@link BaseEvent}，负责构建「行为返利发奖」的 MQ 消息：
 * 1. {@link #buildEventMessage}：把返利消息体封装为统一消息结构（追加随机消息 ID 与时间戳）；
 * 2. {@link #topic}：声明消息主题，主题名从配置项 {@code spring.rabbitmq.topic.send_rebate} 读取。
 *
 * @author zhaoy
 * @date 2026/11/14
 * @description 返利发奖消息事件
 */
@Component
public class SendRebateMessageEvent extends BaseEvent<SendRebateMessageEvent.RebateMessage> {
    // 注入返利消息主题名（配置于 application.yml 的 spring.rabbitmq.topic.send_rebate）
    @Value("${spring.rabbitmq.topic.send_rebate}")
    private String topic;

    /**
     * 构建返利事件消息体
     *
     * <p>为返利消息追加「11 位随机数字消息 ID」与「当前时间戳」，
     * 统一为 {@link BaseEvent.EventMessage} 结构后返回，供 MQ 发送与任务补偿使用。
     *
     * @param data 返利消息体（RebateMessage）
     * @return 构建完成的统一事件消息对象
     */
    @Override
    public BaseEvent.EventMessage<RebateMessage> buildEventMessage(RebateMessage data) {
        return EventMessage.<SendRebateMessageEvent.RebateMessage>builder()
                .id(RandomStringUtils.randomNumeric(11)) // 消息唯一 ID（随机 11 位数字，用于去重/追踪）
                .timestamp(new Date())                    // 消息时间戳（当前时间）
                .data(data)                               // 消息携带的返利数据
                .build();
    }

    /**
     * 返回消息主题名
     *
     * @return 返利发奖 MQ 主题名（如 send_rebate）
     */
    @Override
    public String topic() {
        return topic;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RebateMessage {
        /** 用户ID */
        private String userId;
        /** 返利描述 */
        private String rebateDesc;
        /** 返利类型 */
        private String rebateType;
        /** 返利配置 */
        private String rebateConfig;
        /** 业务ID - 唯一ID，确保幂等 */
        private String bizId;
    }
}
