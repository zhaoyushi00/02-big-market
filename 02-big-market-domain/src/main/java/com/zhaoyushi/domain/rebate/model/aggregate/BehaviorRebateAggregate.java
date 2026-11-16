package com.zhaoyushi.domain.rebate.model.aggregate;

import com.zhaoyushi.domain.rebate.model.entity.BehaviorRebateOrderEntity;
import com.zhaoyushi.domain.rebate.model.entity.TaskEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 行为返利聚合对象（领域层 - 聚合根）
 *
 * <p>作为一次「行为返利」的最小一致性单元，将两个强关联的实体聚合到一起：
 * 1. {@link BehaviorRebateOrderEntity} 返利订单实体 —— 记录本次返利的入账流水；
 * 2. {@link TaskEntity} 任务实体 —— 记录待发送的发奖 MQ 消息任务；
 * 二者在仓储层同一事务内落库，保证「返利入账」与「发奖消息」原子一致。
 *
 * @author zhaoy
 * @date 2026/11/14
 * @description 行为返利聚合对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BehaviorRebateAggregate {

    /** 用户ID（聚合根归属用户） */
    private String userId;
    /** 行为返利订单实体对象（入账流水） */
    private BehaviorRebateOrderEntity behaviorRebateOrderEntity;
    /** 任务实体对象（发奖 MQ 消息任务） */
    private TaskEntity taskEntity;

}
