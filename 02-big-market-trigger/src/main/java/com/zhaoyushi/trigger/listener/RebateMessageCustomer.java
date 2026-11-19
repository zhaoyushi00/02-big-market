package com.zhaoyushi.trigger.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.zhaoyushi.domain.activity.model.entity.SkuRechargeEntity;
import com.zhaoyushi.domain.activity.service.IRaffleActivityAccountQuotaService;
import com.zhaoyushi.domain.rebate.event.SendRebateMessageEvent;
import com.zhaoyushi.domain.rebate.model.valobj.RebateTypeVO;
import com.zhaoyushi.types.event.BaseEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author zhaoy
 * @date 2026/11/16
 * @description 监听行为返利消息
 */
@Slf4j
@Component
public class RebateMessageCustomer {

    // 注入返利消息主题名（配置于 application.yml 的 spring.rabbitmq.topic.send_rebate）
    @Value("${spring.rabbitmq.topic.send_rebate}")
    private String topic;

    // 注入抽奖活动账户额度服务：负责按 SKU 给用户账户充值抽奖次数（入账奖励）
    @Resource
    private IRaffleActivityAccountQuotaService raffleActivityAccountQuotaService;

    // 监听「行为返利发奖」队列：收到消息后解析返利内容，完成 SKU 奖励入账
    @RabbitListener(queuesToDeclare = @Queue(value = "${spring.rabbitmq.topic.send_rebate}"))
    public void listener(String message){
        try {
            // 1、记录消息接收日志：便于排查消息是否送达、内容是否完整
            log.error("监听用户行为返利消息 topic:{} message:{}", topic, message);
            // 2、转换对象（收到的是 JSON）：将 JSON 字符串反序列化为统一事件消息结构
            BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage> eventMessage = JSON.parseObject(message, new TypeReference<BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage>>(){
            }.getType());

            // 3、取出消息体中的返利数据
            SendRebateMessageEvent.RebateMessage rebateMessage = eventMessage.getData();
            // 4、按返利类型分流：目前仅处理 SKU（活动库存充值商品），积分等类型 tv 暂不处理
            if(!RebateTypeVO.SKU.getCode().equals(rebateMessage.getRebateType())){
                log.error("监听用户行为返利消息，非SKU奖励暂时不处理 topic:{} message:{}", topic, message);
                return;
            }

            // 5、入账奖励：构建 SKU 充值实体，调用下单服务给用户账户充值抽奖次数
            SkuRechargeEntity skuRechargeEntity = new SkuRechargeEntity();
            skuRechargeEntity.setUserId(rebateMessage.getUserId()); // 充值主体：用户ID
            skuRechargeEntity.setSku(Long.valueOf(rebateMessage.getRebateConfig())); // 商品 SKU：返利配置里存的就是 sku 值（如 9011）
            skuRechargeEntity.setOutBusinessNo(rebateMessage.getBizId()); // 幂等业务单号：业务唯一 ID，防重复入账
            raffleActivityAccountQuotaService.createSkuRechargeOrder(skuRechargeEntity); // 创建充值订单，返回订单号
        }catch (Exception e){
            // 消费失败：记录错误日志后向上抛出，交由 RabbitMQ 重试机制处理
            log.error("监听用户行为返利消息，消费失败 topic:{} message:{}", topic, message);
            throw e;
        }
    }

}
