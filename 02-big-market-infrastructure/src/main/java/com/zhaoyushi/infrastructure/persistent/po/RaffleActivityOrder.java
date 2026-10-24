package com.zhaoyushi.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;

/**
 * 抽奖活动单 持久化对象
 *
 * <p>对应数据库表 raffle_activity_order（分库分表），一个活动 → 多个订单（1:N），
 * 记录「用户某次抽奖」的凭证：订单号、下单时间、本次消耗次数快照与订单状态。
 *
 * <p>关键点：
 * 1. orderId 唯一，是本次下单的凭证号；
 * 2. totalCount/dayCount/monthCount 记录本笔订单消耗的次数快照（不是累计参与次数）。
 *
 * @author zhaoy
 * @date 2026/10/24
 * @description 抽奖活动单 持久化对象
 */
@Data
public class RaffleActivityOrder {

    /**
     * 自增ID
     */
    private Long id;

    /**
     * 用户ID：下单用户
     */
    private String userId;

    /**
     * 活动ID：参与的活动
     */
    private Long activityId;

    /**
     * 活动名称：冗余活动名称，便于订单展示
     */
    private String activityName;

    /**
     * 抽奖策略ID：该活动使用的抽奖策略
     */
    private Long strategyId;

    /**
     * 订单ID：本次下单的唯一凭证号
     */
    private String orderId;

    /**
     * 下单时间：订单创建时间
     */
    private Date orderTime;

    /**
     * 总次数：本笔订单消耗的总次数快照
     */
    private Integer totalCount;

    /**
     * 日次数：本笔订单消耗的日次数快照
     */
    private Integer dayCount;

    /**
     * 月次数：本笔订单消耗的月次数快照
     */
    private Integer monthCount;

    /**
     * 订单状态：not_used(未使用)/used(已使用)/complete(完成)
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
