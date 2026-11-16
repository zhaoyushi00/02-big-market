package com.zhaoyushi.domain.rebate.service;

import com.zhaoyushi.domain.rebate.event.SendRebateMessageEvent;
import com.zhaoyushi.domain.rebate.model.aggregate.BehaviorRebateAggregate;
import com.zhaoyushi.domain.rebate.model.entity.BehaviorEntity;
import com.zhaoyushi.domain.rebate.model.entity.BehaviorRebateOrderEntity;
import com.zhaoyushi.domain.rebate.model.entity.TaskEntity;
import com.zhaoyushi.domain.rebate.model.valobj.DailyBehaviorRebateVO;
import com.zhaoyushi.domain.rebate.model.valobj.TaskStateVO;
import com.zhaoyushi.domain.rebate.repository.IBehaviorRebateRepository;
import com.zhaoyushi.types.common.Constants;
import com.zhaoyushi.types.event.BaseEvent;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 行为返利服务实现（领域层）
 *
 * <p>负责「用户行为 -> 返利订单 + 发奖消息任务」的核心编排逻辑：
 * 1. 依据行为类型查询「开启中」的返利配置；
 * 2. 为每条返利配置生成一笔返利订单，并拼装对应的发奖 MQ 消息任务；
 * 3. 将订单与任务打包为聚合对象，交由仓储层「同库同事务」落库；
 * 4. 仓储层在事务外同步投递 MQ，若投递失败由任务表定时补偿，保证最终一致。
 *
 * @author zhaoy
 * @date 2026/11/14
 * @description 行为返利服务实现
 */
@Service
public class BehaviorRebateService implements IBehaviorRebateService{

    // 注入行为返利仓储接口：负责查询返利配置、保存返利订单与任务
    @Resource
    private IBehaviorRebateRepository behaviorRebateRepository;
    // 注入返利消息事件：负责构建发奖 MQ 消息体并提供主题名
    @Autowired
    private SendRebateMessageEvent sendRebateMessageEvent;

    /**
     * 创建行为返利入账订单
     *
     * <p>一次行为动作（如签到、支付）可能命中多条返利配置（sku 充值 + 积分），
     * 因此返回「订单 ID 列表」，每笔订单对应一条返利记录与一条发奖消息任务。
     *
     * @param behaviorEntity 行为实体（含用户 ID、行为类型、外部业务单号）
     * @return 本次行为生成的全部返利订单 ID 集合（无可用返利配置时返回空列表）
     */
    @Override
    public List<String> createOrder(BehaviorEntity behaviorEntity) {

        // 1、查询返利配置：按行为类型取出所有「开启中」的返利配置
        List<DailyBehaviorRebateVO> dailyBehaviorRebateVOS = behaviorRebateRepository.queryDailyBehaviorRebateConfig(behaviorEntity.getBehaviorTypeVO());
        // 无可用返利配置时直接返回空列表，后续流程不再处理
        if(null == dailyBehaviorRebateVOS || dailyBehaviorRebateVOS.isEmpty()) return new ArrayList<>();

        // 2、构建聚合对象：遍历每条返利配置，逐条生成订单与任务
        ArrayList<String> orderIds = new ArrayList<>();
        ArrayList<BehaviorRebateAggregate> behaviorRebateAggregates = new ArrayList<>();
        for (DailyBehaviorRebateVO dailyBehaviorRebateVO : dailyBehaviorRebateVOS){
            // 拼装业务唯一标识：用户ID_返利类型_外部业务ID，作为幂等/防重入账的依据
            String bizId = behaviorEntity.getUserId() + Constants.UNDERLINE + dailyBehaviorRebateVO.getRebateType() + Constants.UNDERLINE + behaviorEntity.getOutBusinessNo();
            // 构建返利订单实体：随机 12 位数字作为订单号
            BehaviorRebateOrderEntity behaviorRebateOrderEntity = BehaviorRebateOrderEntity.builder()
                        .userId(behaviorEntity.getUserId())
                        .orderId(RandomStringUtils.randomNumeric(12))
                        .behaviorType(dailyBehaviorRebateVO.getBehaviorType())
                        .rebateDesc(dailyBehaviorRebateVO.getRebateDesc())
                        .rebateType(dailyBehaviorRebateVO.getRebateType())
                        .rebateConfig(dailyBehaviorRebateVO.getRebateConfig())
                        .bizId(bizId)
                        .build();
            // 收集订单 ID，作为方法返回值
            orderIds.add(behaviorRebateOrderEntity.getOrderId());

            // 构建 MQ 消息体：携带返利类型/配置/业务唯一标识，供下游发奖服务解析
            SendRebateMessageEvent.RebateMessage rebateMessage = SendRebateMessageEvent.RebateMessage.builder()
                    .userId(behaviorEntity.getUserId())
                    .rebateType(dailyBehaviorRebateVO.getRebateType())
                    .rebateConfig(dailyBehaviorRebateVO.getRebateConfig())
                    .bizId(bizId)
                    .build();

            // 将消息体封装为统一的事件消息结构（追加消息 ID 与时间戳）
            BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage> rebateMessageEventMessage = sendRebateMessageEvent.buildEventMessage(rebateMessage);

            // 组装消息任务实体：用于落库 task 表，作为 MQ 发送失败时的补偿凭证
            TaskEntity taskEntity = new TaskEntity();
            taskEntity.setMessage(rebateMessageEventMessage);          // 消息主体（含 id/时间戳/数据）
            taskEntity.setMessageId(rebateMessageEventMessage.getId()); // 消息唯一 ID
            taskEntity.setState(TaskStateVO.create);                    // 初始状态：创建（待发送）
            taskEntity.setTopic(sendRebateMessageEvent.topic());        // 目标 MQ 主题
            taskEntity.setUserId(behaviorEntity.getUserId());           // 归属用户

            // 组装聚合根：将订单实体 + 任务实体聚合到一起，交由仓储层一并落库
            BehaviorRebateAggregate behaviorRebateAggregate = BehaviorRebateAggregate.builder()
                    .userId(behaviorEntity.getUserId())
                    .behaviorRebateOrderEntity(behaviorRebateOrderEntity)
                    .taskEntity(taskEntity)
                    .build();

            behaviorRebateAggregates.add(behaviorRebateAggregate);

        }

        // 3、存储聚合对象：仓储层在事务内写订单+任务，事务外投递 MQ
        behaviorRebateRepository.saveUserRebateRecord(behaviorEntity.getUserId(), behaviorRebateAggregates);

        // 4、返回本次行为生成的全部返利订单 ID 集合
        return orderIds;
    }
}
