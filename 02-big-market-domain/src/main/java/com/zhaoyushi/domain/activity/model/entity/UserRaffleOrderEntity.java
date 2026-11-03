package com.zhaoyushi.domain.activity.model.entity;

import com.zhaoyushi.domain.activity.model.valobj.UserRaffleOrderStateVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户抽奖订单实体对象
 *
 * <p>领域层的「用户抽奖单」实体，描述用户参与某个活动时产生的抽奖凭证。
 * 与基础设施层的 {@code UserRaffleOrder}（持久化对象）一一对应，落库到分表 user_raffle_order_000~003。
 *
 * <p>关键点：
 * 1. orderId 是本次抽奖单的唯一凭证号（12 位随机数字）；
 * 2. orderState 用枚举 {@link UserRaffleOrderStateVO} 表达（create 创建 / used 已使用 / cancel 已作废）；
 * 3. 一次参与活动 = 一张订单，未消费（create）的订单可被复用，避免重复扣减账户额度。
 *
 * @author zhaoy
 * @date 2026/11/1
 * @description 用户抽奖订单实体对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRaffleOrderEntity {

    /** 用户ID：下单主体（谁在参与抽奖） */
    private String userId;
    /** 活动ID：参与的活动编号 */
    private Long activityId;
    /** 活动名称：冗余活动名，便于订单展示 */
    private String activityName;
    /** 抽奖策略ID：该活动绑定的抽奖策略 */
    private Long strategyId;
    /** 订单ID：本次抽奖单的唯一凭证号 */
    private String orderId;
    /** 下单时间：订单创建时间 */
    private Date orderTime;
    /** 订单状态：create-创建、used-已使用、cancel-已作废 */
    private UserRaffleOrderStateVO orderState;

}
