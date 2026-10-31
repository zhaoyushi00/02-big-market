// 声明当前类所属的包路径：类型模块 - 事件
package com.zhaoyushi.types.event;

// 引入 Lombok 全参构造器注解，自动生成包含所有字段的构造方法
import lombok.AllArgsConstructor;
// 引入 Lombok Builder 注解，支持以建造者模式链式创建对象
import lombok.Builder;
// 引入 Lombok Data 注解，自动生成 getter/setter/toString/equals/hashCode
import lombok.Data;
// 引入 Lombok 无参构造器注解，自动生成无参构造方法
import lombok.NoArgsConstructor;

// 引入 java.util.Date，作为消息时间戳类型
import java.util.Date;

/**
 * 基础事件抽象类（事件消息基类）
 *
 * <p>定义领域事件的通用契约与消息体结构：
 * 1. 每个具体事件需实现 {@link #buildEventMessage}（构建消息体）与 {@link #topic}（声明主题）；
 * 2. 内部静态类 EventMessage 为统一的 MQ 消息体，含 id、时间戳、数据三要素。
 *
 * @author zhaoy
 * @date 2026/10/28
 * @description 基础事件（消息基类）
 */
// 使用 Lombok 自动生成 getter/setter/toString/equals/hashCode 等方法
@Data
public abstract class BaseEvent<T> {

    /**
     * 构建事件消息体（由具体事件实现）
     *
     * @param data 事件数据
     * @return 构建完成的事件消息对象
     */
    public abstract EventMessage<T> buildEventMessage(T data);

    /**
     * 返回事件主题（由具体事件实现）
     *
     * @return topic 名称
     */
    public abstract String topic();

    /**
     * 事件消息体（MQ 统一消息结构）
     *
     * @param <T> 事件携带的数据类型
     */
    // 使用 Lombok 自动生成 getter/setter/toString/equals/hashCode 等方法
    @Data
    // 使用 Lombok 生成建造者模式，支持链式构建
    @Builder
    // 使用 Lombok 生成包含全部字段的构造方法
    @AllArgsConstructor
    // 使用 Lombok 生成无参构造方法
    @NoArgsConstructor
    public static class EventMessage<T> {
        /** 消息唯一 ID（用于去重、追踪） */
        private String id;
        /** 消息时间戳 */
        private Date timestamp;
        /** 消息携带的数据（泛型） */
        private T data;
    }

}
