package com.zhaoyushi.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户行为返利流水订单表 持久化对象（基础设施层）
 *
 * <p>对应数据库表 user_behavior_rebate_order，承载返利入账流水的字段映射，
 * 该表按用户 ID 分库分表。
 *
 * @author zhaoy
 * @date 2026/11/14
 * @description 用户行为返利流水订单表 持久化对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBehaviorRebateOrder {
    /** 自增ID（主键） */
    private Long id;
    /** 用户ID（返利归属用户，也是分库分表路由键） */
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
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;
}
