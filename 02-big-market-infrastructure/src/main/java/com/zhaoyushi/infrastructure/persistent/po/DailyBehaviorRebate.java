package com.zhaoyushi.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 日常行为返利活动配置 持久化对象（基础设施层）
 *
 * <p>对应数据库表 daily_behavior_rebate，承载返利规则的字段映射。
 *
 * @author zhaoy
 * @date 2026/11/14
 * @description 日常行为返利活动配置 持久化对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyBehaviorRebate {

    /** 自增ID（主键） */
    private Long id;
    /** 行为类型（sign 签到、openai_pay 支付） */
    private String behaviorType;
    /** 返利描述（如「签到返积分」） */
    private String rebateDesc;
    /** 返利类型（sku 活动库存充值商品、integral 用户活动积分） */
    private String rebateType;
    /** 返利配置（返利的数值，如积分数量、sku 数量） */
    private String rebateConfig;
    /** 状态（open 开启、close 关闭） */
    private String state;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;

}
