// 声明当前类所属的包路径：触发器层 - 监听器
package com.zhaoyushi.trigger.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.zhaoyushi.domain.activity.service.IRaffleActivitySkuStockService;
import com.zhaoyushi.types.event.BaseEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
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
    private IRaffleActivitySkuStockService skuStock;

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
