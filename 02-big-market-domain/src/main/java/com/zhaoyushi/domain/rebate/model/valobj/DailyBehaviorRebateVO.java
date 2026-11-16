package com.zhaoyushi.domain.rebate.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 日常行为返利配置值对象（领域层）
 *
 * <p>承载一条「开启中」的返利规则，对应数据库表 daily_behavior_rebate：
 * 由仓储层查询后转换为本值对象，供返利服务据以生成订单与发奖消息。
 *
 * @author zhaoy
 * @date 2026/11/14
 * @description 日常行为返利配置值对象
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyBehaviorRebateVO {

    /** 行为类型（sign 签到、openai_pay 支付） */
    private String behaviorType;
    /** 返利描述（如「签到返积分」） */
    private String rebateDesc;
    /** 返利类型（sku 活动库存充值商品、integral 用户活动积分） */
    private String rebateType;
    /** 返利配置（返利的数值，如积分数量、sku 数量） */
    private String rebateConfig;

}
