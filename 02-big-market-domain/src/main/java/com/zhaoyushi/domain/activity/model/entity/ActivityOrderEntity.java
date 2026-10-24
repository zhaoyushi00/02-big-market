package com.zhaoyushi.domain.activity.model.entity;

import com.zhaoyushi.domain.activity.model.valobj.OrderStateVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 活动参与（订单）实体对象
 *
 * <p>对应数据库表 raffle_activity_order（抽奖活动单），描述“用户某次参与抽奖的下单凭证”：
 * 记录订单号、下单时间、本次消耗的次数快照以及订单状态。
 *
 * <p>业务位置：下单服务 {@link com.zhaoyushi.domain.activity.service.IRaffleOrder#createRaffleActivityOrder}
 * 的返回结果，作为“用户获得参与抽奖资格（可消耗次数）”的凭证返回给调用方。
 *
 * <p>关键点：
 * 1. orderId 唯一，是本次下单的凭证号；
 * 2. totalCount/dayCount/monthCount 是“这一笔订单消耗的次数快照”，不是累计参与次数；
 * 3. state 用 OrderStateVO 表达，当前仅 completed（完成）一种状态。
 *
 * @author zhaoy
 * @date 2026/10/24
 * @description 活动参与（订单）实体对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityOrderEntity {

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
     * 订单状态：completed(完成)
     */
    private OrderStateVO state;

}
