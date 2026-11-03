package com.zhaoyushi.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户抽奖订单表 持久化对象
 *
 * <p>对应分库分表后的 user_raffle_order_000~003（按 userId 分表）。
 * 记录用户参与活动的抽奖单凭证，order_state 标记订单是否已被使用。
 *
 * @author zhaoy
 * @description 用户抽奖订单表
 * @create 2024-04-03 15:30
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRaffleOrder {

    /** 自增主键ID */
    private String id;
    /** 用户ID：下单主体 */
    private String userId;
    /** 活动ID：参与的活动 */
    private Long activityId;
    /** 活动名称：冗余活动名 */
    private String activityName;
    /** 抽奖策略ID：活动绑定的策略 */
    private Long strategyId;
    /** 订单ID：抽奖单唯一凭证号 */
    private String orderId;
    /** 下单时间：订单创建时间 */
    private Date orderTime;
    /** 订单状态；create-创建、used-已使用、cancel-已作废 */
    private String orderState;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;

}
