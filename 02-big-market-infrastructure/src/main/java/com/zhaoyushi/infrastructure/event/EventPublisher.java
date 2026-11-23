// 声明当前类所属的包路径：基础设施层 - 事件
package com.zhaoyushi.infrastructure.event;

import com.alibaba.fastjson.JSON;
import com.zhaoyushi.types.event.BaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息发送器（基础设施层）
 *
 * <p>封装 RabbitMQ 的消息发布能力：将事件消息体序列化为 JSON 后，
 * 通过 RabbitTemplate 发送到指定 topic，并记录发送日志。
 *
 * @author zhaoy
 * @date 2026/10/28
 * @description 消息发送
 */
// 使用 Lombok 生成日志对象 log
@Slf4j
// 注册为 Spring 组件
@Component
public class EventPublisher {

    // 自动注入 RabbitTemplate，用于向 RabbitMQ 发送消息
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发布事件消息到指定 topic
     *
     * @param topic        目标主题（交换机/队列名）
     * @param eventMessage 事件消息体（泛型，携带 id、时间戳、数据）
     */
    public void publish(String topic, BaseEvent.EventMessage<?> eventMessage){
        // 使用 try-catch 包裹发送逻辑，便于在异常时记录失败日志并向上抛出
        try {
            // 将事件消息体序列化为 JSON 字符串
            String messageJson = JSON.toJSONString(eventMessage);
            // 通过 RabbitTemplate 将 JSON 消息发送到指定 topic
            rabbitTemplate.convertAndSend(topic, messageJson);
            // 记录发送成功日志（topic 与消息内容）
            log.info("发送MQ消息 topic:{} message:{}", topic, messageJson);
        } catch (Exception e) {
            // 记录发送失败日志（topic、消息内容、异常堆栈）
            log.error("发送MQ消息失败 topic:{} message:{}", topic, JSON.toJSONString(eventMessage), e);
            // 重新抛出异常，交由上层处理
            throw e;
        }
    }

    /**
     * 发布 JSON 字符串形式的消息到指定 topic（重载方法）
     *
     * <p>与上面的 {@link #publish(String, BaseEvent.EventMessage)} 不同，本方法直接接收
     * 已序列化好的 JSON 字符串消息，用于「消息任务补偿」场景——从 task 表取出消息体（JSON）后直接发送，
     * 无需再经过对象序列化。
     *
     * @param topic            目标主题（交换机/队列名）
     * @param eventMessageJSON 已序列化为 JSON 字符串的事件消息
     */
    public void publish(String topic, String eventMessageJSON){
        // 使用 try-catch 包裹发送逻辑，便于在异常时记录失败日志并向上抛出
        try {
            // 通过 RabbitTemplate 将 JSON 字符串消息直接发送到指定 topic
            rabbitTemplate.convertAndSend(topic, eventMessageJSON);
            // 记录发送成功日志（topic 与消息内容）
            log.info("发送MQ消息 topic:{} message:{}", topic, eventMessageJSON);
        } catch (Exception e) {
            // 记录发送失败日志（topic、消息内容、异常堆栈）
            log.error("发送MQ消息失败 topic:{} message:{}", topic, eventMessageJSON, e);
            // 重新抛出异常，交由上层（任务补偿机制）处理
            throw e;
        }
    }

}
