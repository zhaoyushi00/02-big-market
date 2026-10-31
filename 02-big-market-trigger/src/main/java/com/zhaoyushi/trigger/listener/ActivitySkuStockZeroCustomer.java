// 声明当前类所属的包路径：触发器层 - 监听器
package com.zhaoyushi.trigger.listener;

// 引入 fastjson2 的 JSON 工具，用于将消息字符串反序列化为对象
import com.alibaba.fastjson2.JSON;
// 引入 fastjson2 的 TypeReference，用于保留泛型信息做反序列化
import com.alibaba.fastjson2.TypeReference;
// 引入活动 SKU 库存处理接口，提供清空库存与清空队列能力
import com.zhaoyushi.domain.activity.service.ISkuStock;
// 引入基础事件抽象类，用于解析消息体类型
import com.zhaoyushi.types.event.BaseEvent;
// 引入 JSR-250 的 @Resource 注解，用于按名称注入依赖
import jakarta.annotation.Resource;
// 引入 Lombok 的 @Slf4j 注解，自动生成日志对象 log
import lombok.extern.slf4j.Slf4j;
// 引入 @Queue 注解，用于声明/绑定 MQ 队列
import org.springframework.amqp.rabbit.annotation.Queue;
// 引入 @RabbitListener 注解，用于标注 MQ 消息监听方法
import org.springframework.amqp.rabbit.annotation.RabbitListener;
// 引入 @Value 注解，用于从配置文件中注入 topic 值
import org.springframework.beans.factory.annotation.Value;
// 引入 @Component 注解，将本类注册为 Spring Bean
import org.springframework.stereotype.Component;

/**
 * 活动 SKU 库存耗尽消息监听器（消费者）
 *
 * <p>监听 MQ 队列「activity_sku_stock_zero」，当收到库存耗尽的 sku 消息后：
 * 1. 将数据库剩余库存直接置 0（因为 Redis 缓存库存已耗尽）；
 * 2. 清空 Redis 延迟队列（库存已耗尽，无需再延迟更新数据库）。
 *
 * @author zhaoy
 * @date 2026/10/28
 * @description 活动sku库存耗尽
 */
// 使用 Lombok 生成日志对象 log
@Slf4j
// 注册为 Spring 组件
@Component
public class ActivitySkuStockZeroCustomer {

    // 从配置文件中注入 MQ topic 名称（用于日志输出）
    @Value("${spring.rabbitmq.topic.activity_sku_stock_zero}")
    private String topic;

    // 注入活动 SKU 库存处理服务，提供清空库存与清空队列的能力
    @Resource
    private ISkuStock skuStock;

    /**
     * MQ 消息监听方法
     * <p>监听并消费库存耗尽消息，完成数据库库存清零与队列清空。
     *
     * @param message 消息体（JSON 字符串）
     */
    // 声明监听队列为 activity_sku_stock_zero（不存在时自动创建）
    @RabbitListener(queuesToDeclare = @Queue(value = "activity_sku_stock_zero"))
    public void listener(String message) {
        // 使用 try-catch 包裹，异常时记录失败日志并抛出（触发 MQ 重投）
        try {
            // 记录收到消息日志（topic 与消息内容）
            log.info("监听活动sku库存消耗为0消息 topic: {} message: {}", topic, message);
            // 转换对象：将 JSON 字符串反序列化为 EventMessage<Long>（保留泛型信息）
            BaseEvent.EventMessage<Long> eventMessage =
                    JSON.parseObject(message, new TypeReference<BaseEvent.EventMessage<Long>>() {}.getType());
            // 从消息体中取出携带的数据（商品 SKU 编号）
            Long sku = eventMessage.getData();
            // 更新库存：将数据库中的剩余库存直接置 0
            skuStock.clearActivitySkuStock(sku);
            // 清空队列 「此时就不需要延迟更新数据库记录了」
            skuStock.clearQueueValue();
        } catch (Exception e) {
            // 记录消费失败日志（topic、消息内容）
            log.error("监听活动sku库存消耗为0消息，消费失败 topic: {} message: {}", topic, message);
            // 抛出异常，让 MQ 依据重试策略重新投递
            throw e;
        }
    }

}
