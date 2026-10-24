package com.zhaoyushi.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;

/**
 * 抽奖活动表 持久化对象
 *
 * <p>对应数据库表 raffle_activity，描述一个抽奖活动的基础信息：
 * 活动名称、描述、起止时间、抽奖策略与状态。
 *
 * <p>用户参与抽奖的整体链路（结合其它表）：
 * 1. 用户通过 SKU 下单：查 raffle_activity_sku 得到活动ID与次数配置ID；
 * 2. 查活动 raffle_activity：确认 state 可用、当前时间在 begin/end_date_time 内；
 * 3. 查账户 raffle_activity_account（user_id + activity_id）：看 *Surplus 余额够不够；
 * 4. 生成订单 raffle_activity_order：order_id 唯一，快照本次消耗次数；
 * 5. 走策略抽奖：按 strategy_id 关联 strategy/strategy_award/规则树等。
 *
 * @author zhaoy
 * @date 2026/10/24
 * @description 抽奖活动表 持久化对象
 */
@Data
public class RaffleActivity {

    /**
     * 自增ID
     */
    private Long id;

    /**
     * 活动ID：唯一标识一个抽奖活动
     */
    private Long activityId;

    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 活动描述
     */
    private String activityDesc;

    /**
     * 开始时间：活动可参与的时间下界
     */
    private Date beginDateTime;

    /**
     * 结束时间：活动可参与的时间上界
     */
    private Date endDateTime;

    /**
     * 抽奖策略ID：关联抽奖策略，决定奖品概率与规则
     */
    private Long strategyId;

    /**
     * 活动状态：create(创建)/open(开启)/close(关闭)
     */
    private String state;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}
