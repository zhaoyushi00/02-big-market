package com.zhaoyushi.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖活动表 持久化对象
 * @create 2024-03-02 13:06
 */

/**
 * 用户 user001 参与活动 100301 抽奖：
 * 查活动 raffle_activity：确认 state 可用、在 begin/end_date_time 内、stock_count_surplus > 0；
 * 查账户 raffle_activity_account（user_id=user001, activity_id=100301）：看 total_count_surplus 等余额够不够；
 * 生成订单 raffle_activity_order：order_id 唯一，state=not_used；
 * 扣账户次数：更新 raffle_activity_account 的 *_surplus 减 1；
 * 记流水 raffle_activity_account_flow：flow_id 唯一，biz_id=order_id（防重复扣）；
 * 走策略抽奖：按 strategy_id 关联 strategy/strategy_award/规则树等；
 * 订单置为 used，扣活动库存 stock_count_surplus - 1。
 */

@Data
public class RaffleActivity {

    /**
     * 自增ID
     */
    private Long id;

    /**
     * 活动ID
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
     * 开始时间
     */
    private Date beginDateTime;

    /**
     * 结束时间
     */
    private Date endDateTime;

    /**
     * 库存总量
     */
    private Integer stockCount;

    /**
     * 剩余库存
     */
    private Integer stockCountSurplus;

    /**
     * 活动参与次数配置
     */
    private Long activityCountId;

    /**
     * 抽奖策略ID
     */
    private Long strategyId;

    /**
     * 活动状态
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
