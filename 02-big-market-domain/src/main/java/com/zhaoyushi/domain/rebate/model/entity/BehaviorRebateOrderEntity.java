package com.zhaoyushi.domain.rebate.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 行为返利订单实体对象（领域层）
 *
 * <p>记录一次行为动作产生的一笔返利入账流水，对应数据库表 user_behavior_rebate_order：
 * 每命中一条返利配置即生成一笔订单，同一行为可产生多笔订单。
 *
 * @author zhaoy
 * @date 2026/11/14
 * @description 行为返利订单实体对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BehaviorRebateOrderEntity {

    /** 用户ID（返利归属用户） */
    private String userId;
    /** 订单ID（随机 12 位数字，唯一标识一笔返利流水） */
    private String orderId;
    /** 行为类型（sign 签到、openai_pay 支付） */
    private String behaviorType;
    /** 返利描述（如「签到返积分」） */
    private String rebateDesc;
    /** 返利类型（sku 活动库存充值商品、integral 用户活动积分） */
    private String rebateType;
    /** 返利配置【sku值，积分值】 */
    private String rebateConfig;
    /** 业务防重ID - 外部透传，方便查询使用 */
    private String outBusinessNo;
    /** 业务ID - 拼接的唯一值（用户ID_返利类型_外部业务ID，用于幂等防重） */
    private String bizId;

}
