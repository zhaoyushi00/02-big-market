// 声明当前类所属的包路径：领域层 - 事件
package com.zhaoyushi.domain.event;

// 引入基础事件抽象类，本类继承它并定义消息体与主题
import com.zhaoyushi.types.event.BaseEvent;
// 引入随机字符串工具，用于生成消息唯一 ID
import org.apache.commons.lang3.RandomStringUtils;
// 引入 @Value 注解，用于从配置文件中注入 topic 值
import org.springframework.beans.factory.annotation.Value;
// 引入 @Component 注解，将本类注册为 Spring Bean
import org.springframework.stereotype.Component;

// 引入 java.util.Date，作为消息时间戳类型
import java.util.Date;

/**
 * 活动 SKU 库存清空消息事件
 *
 * <p>当活动 SKU 的 Redis 缓存库存扣减到 0 时，发布本事件对应的 MQ 消息，
 * 通知消费者把数据库中的剩余库存直接置 0，实现缓存与数据库库存的最终一致性。
 *
 * @author zhaoy
 * @date 2026/10/28
 * @description 活动sku库存清空消息
 */
// 注册为 Spring 组件，由容器管理
@Component
public class ActivitySkuStockZeroMessageEvent extends BaseEvent<Long> {

    // 从配置文件中注入 MQ topic 名称（spring.rabbitmq.topic.activity_sku_stock_zero）
    @Value("${spring.rabbitmq.topic.activity_sku_stock_zero}")
    private String topic;

    /**
     * 构建事件消息体
     * <p>生成唯一消息 ID 与时间戳，将数据（sku）封装进 EventMessage 返回。
     *
     * @param data 事件携带的数据（此处为商品 SKU 编号）
     * @return 构建完成的事件消息对象
     */
    @Override
    public EventMessage<Long> buildEventMessage(Long data) {
        // 使用建造者模式构建事件消息：设置 id、时间戳、数据
        return EventMessage.<Long>builder()
                // 使用随机数字生成 11 位消息唯一 ID
                .id(RandomStringUtils.randomNumeric(11))
                // 设置消息时间戳为当前时间
                .timestamp(new Date())
                // 设置消息携带的数据（商品 SKU 编号）
                .data(data)
                // 完成构建
                .build();
    }

    /**
     * 返回本事件对应的 MQ 主题名称
     *
     * @return topic 名称
     */
    @Override
    public String topic() {
        // 返回注入的 topic 名称
        return topic;
    }
}
