package com.zhaoyushi.domain.rebate.repository;

import com.zhaoyushi.domain.rebate.model.aggregate.BehaviorRebateAggregate;
import com.zhaoyushi.domain.rebate.model.entity.BehaviorRebateOrderEntity;
import com.zhaoyushi.domain.rebate.model.valobj.BehaviorTypeVO;
import com.zhaoyushi.domain.rebate.model.valobj.DailyBehaviorRebateVO;

import java.util.List;

/**
 * 行为返利服务仓储接口（领域层）
 *
 * <p>定义行为返利领域的持久化契约，由基础设施层实现：
 * 1. 查询日常行为返利配置；
 * 2. 保存用户返利记录（返利订单 + 发奖消息任务，并投递 MQ）。
 *
 * @author zhaoy
 * @date 2026/11/14
 * @description 行为返利服务仓储接口
 */
public interface IBehaviorRebateRepository {

    /**
     * 查询日常行为返利配置
     *
     * <p>按行为类型查询所有「开启中」的返利配置，一次行为可命中多条配置。
     *
     * @param behaviorTypeVO 行为类型值对象（如 sign 签到、openai_pay 支付）
     * @return 返利配置值对象列表（无配置时返回空列表）
     */
    List<DailyBehaviorRebateVO> queryDailyBehaviorRebateConfig(BehaviorTypeVO behaviorTypeVO);

    /**
     * 保存用户返利记录
     *
     * <p>在分库分表路由下，将返利订单与消息任务在同一事务内落库，
     * 并在事务外同步投递发奖 MQ 消息（失败时任务表保留状态等待定时补偿）。
     *
     * @param userId                   用户 ID（用于分库分表路由）
     * @param behaviorRebateAggregates 行为返利聚合对象列表
     */
    void saveUserRebateRecord(String userId, List<BehaviorRebateAggregate> behaviorRebateAggregates);

    List<BehaviorRebateOrderEntity> queryOrderByOutBusinessNo(String userId, String outBusinessNo);
}
