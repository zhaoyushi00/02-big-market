// 声明当前类所属的包路径：触发器层 - 消息监听器
package com.zhaoyushi.trigger.listener;

// 引入 Lombok 的 @Slf4j 注解，自动生成日志对象 log
import lombok.extern.slf4j.Slf4j;
// 引入 @Queue 注解，用于声明监听队列（队列不存在时自动创建）
import org.springframework.amqp.rabbit.annotation.Queue;
// 引入 @RabbitListener 注解，将方法注册为 RabbitMQ 消息监听器
import org.springframework.amqp.rabbit.annotation.RabbitListener;
// 引入 @Value 注解，用于从配置文件中读取 topic 名称
import org.springframework.beans.factory.annotation.Value;
// 引入 @Component 注解，将本类注册为 Spring Bean
import org.springframework.stereotype.Component;

/**
 * 发奖消息消费者（触发器层 - 消息监听器）
 *
 * <p>监听「发奖」消息队列（send_award），消费由发奖流程投递的 MQ 消息。
 * 当前为演示实现：仅记录消息日志，实际发奖逻辑（如调用奖品发放、通知用户）可在此扩展。
 *
 * @author zhaoy
 * @date 2026/11/5
 * @description 发奖消息消费者
 */
// 使用 Lombok 生成日志对象 log
@Slf4j
// 注册为 Spring 组件 Bean
@Component
public class SendAwardCustomer {

    // 从配置文件中读取发奖消息的 topic 名称（spring.rabbitmq.topic.send_award）
    @Value("${spring.rabbitmq.topic.send_award}")
    private String topic;

    /**
     * 监听发奖消息队列
     *
     * <p>当队列中有新消息时自动触发本方法。queuesToDeclare 声明队列（不存在则自动创建），
     * 队列名取自配置文件中的 topic。
     *
     * @param message 消费到的消息内容（JSON 字符串）
     */
    // 注册为 RabbitMQ 监听器：监听发奖队列，队列不存在时自动声明创建
    @RabbitListener(queuesToDeclare = @Queue(value = "${spring.rabbitmq.topic.send_award}"))
    public void listener(String message) {
        // 使用 try-catch 包裹消费逻辑，便于记录消费结果
        try {
            // 记录消费成功日志（topic 与消息内容）
            log.info("监听用户奖品发送消息 topic: {} message: {}", topic, message);
        } catch (Exception e) {
            // 记录消费失败日志（topic 与消息内容）
            log.error("监听用户奖品发送消息，消费失败 topic: {} message: {}", topic, message);
            // 重新抛出异常，交由 RabbitMQ 根据重试机制决定是否重投
            throw e;
        }
    }
}
