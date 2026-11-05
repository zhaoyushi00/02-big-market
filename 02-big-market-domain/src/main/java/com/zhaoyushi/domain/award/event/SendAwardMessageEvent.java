// 声明当前类所属的包路径：领域层 - 奖品模块 - 事件
package com.zhaoyushi.domain.award.event;

// 引入基础事件抽象类，本事件继承它并实现消息体构建与主题声明
import com.zhaoyushi.types.event.BaseEvent;
// 引入 Lombok 全参构造器注解，自动生成包含所有字段的构造方法
import lombok.AllArgsConstructor;
// 引入 Lombok Builder 注解，支持以建造者模式链式创建对象
import lombok.Builder;
// 引入 Lombok Data 注解，自动生成 getter/setter/toString/equals/hashCode
import lombok.Data;
// 引入 Lombok 无参构造器注解，自动生成无参构造方法
import lombok.NoArgsConstructor;
// 引入 RandomStringUtils 工具，用于生成随机的消息 ID
import org.apache.commons.lang3.RandomStringUtils;
// 引入 @Value 注解，用于从配置文件中读取 topic 名称
import org.springframework.beans.factory.annotation.Value;
// 引入 @Service 注解，将本类注册为 Spring Bean
import org.springframework.stereotype.Service;

// 引入 java.util.Date，作为消息时间戳类型
import java.util.Date;

/**
 * 发奖消息事件（领域层 - 奖品模块）
 *
 * <p>继承基础事件 {@link BaseEvent}，负责构建「用户发奖」的 MQ 消息体并声明消息主题：
 * 1. {@link #buildEventMessage} 生成统一结构的事件消息（含唯一 id、时间戳、发奖数据）；
 * 2. {@link #topic} 返回配置的发奖主题名。
 * 该事件消息最终会写入任务表，由定时任务补偿发送到 MQ，驱动发奖流程。
 *
 * @author zhaoy
 * @date 2026/11/4
 * @description 发奖消息事件
 */
// 注册为 Spring Bean
@Service
public class SendAwardMessageEvent extends BaseEvent<SendAwardMessageEvent.SendAwardMessage> {
    // 从配置文件中读取发奖消息的 topic 名称（spring.rabbitmq.topic.send_award）
    @Value("${spring.rabbitmq.topic.send_award}")
    private String topic;

    /**
     * 构建事件消息体
     *
     * <p>以发奖数据为内容，生成统一结构的事件消息：随机 11 位数字 ID + 当前时间戳 + 发奖数据。
     *
     * @param data 发奖消息数据
     * @return 构建完成的事件消息对象
     */
    @Override
    public EventMessage<SendAwardMessage> buildEventMessage(SendAwardMessage data) {
        // 使用建造者模式构建事件消息体并返回
        return EventMessage.<SendAwardMessage>builder()
                // 生成 11 位随机数字作为消息唯一 ID（用于去重与追踪）
                .id(RandomStringUtils.randomNumeric(11))
                // 设置当前时间作为消息时间戳
                .timestamp(new Date())
                // 设置消息携带的发奖数据
                .data(data)
                // 完成构建
                .build();
    }

    /**
     * 返回发奖消息主题
     *
     * @return 发奖消息的 topic 名称
     */
    @Override
    public String topic() {
        // 返回配置的发奖消息主题
        return topic;
    }

    /**
     * 发奖消息数据体（事件内部静态类）
     *
     * <p>描述一条发奖消息需要携带的业务数据：发给谁、发什么奖品。
     */
    // 使用 Lombok 自动生成 getter/setter/toString/equals/hashCode 等方法
    @Data
    // 使用 Lombok 生成建造者模式，支持链式构建
    @Builder
    // 使用 Lombok 生成包含全部字段的构造方法
    @AllArgsConstructor
    // 使用 Lombok 生成无参构造方法
    @NoArgsConstructor
    public static class SendAwardMessage {
        // 用户ID：标识发奖消息的目标用户
        private String userId;
        // 奖品ID：标识要发放的具体奖品
        private Integer awardId;
        // 奖品标题（名称）：奖品的中文名称，用于日志与展示
        private String awardTitle;
    }
}
