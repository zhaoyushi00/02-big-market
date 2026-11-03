package com.zhaoyushi.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 响应码枚举
 * <p>定义系统统一的响应码：code 为程序用，info 为给人看的描述。
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ResponseCode {

    SUCCESS("0000", "成功"),
    UN_ERROR("0001", "未知失败"),
    ILLEGAL_PARAMETER("0002", "非法参数"),
    /** 唯一索引冲突：下单时 out_business_no 重复触发（幂等防重） */
    INDEX_DUP("0003", "唯一索引冲突"),
    STRATEGY_RULE_WEIGHT_IS_NULL("ERR_BIZ_001", "业务异常，策略规则中 rule_weight 权重规则已适用但未配置"),
    UN_ASSEMBLED_STRATEGY_ARMORY("ERR_BIZ_002", "抽奖策略配置未装配，请通过IStrategyArmory完成装配"),
    /** 活动状态异常：下单时活动未处于 open 开启状态 */
    ACTIVITY_STATE_ERROR("ERR_BIZ_003", "活动未开启（非open状态）"),
    /** 活动日期异常：下单时间不在活动开始与结束时间范围内 */
    ACTIVITY_DATE_ERROR("ERR_BIZ_004", "非活动日期范围"),
    /** 活动库存不足：活动 SKU 库存耗尽 */
    ACTIVITY_SKU_STOCK_ERROR("ERR_BIZ_005", "活动库存不足"),
    /** 账户总额度不足：参与活动时用户总账户剩余次数不足 */
    ACCOUNT_QUOTA_ERROR("ERR_BIZ_006","账户总额度不足"),
    /** 账户月额度不足：参与活动时用户当月剩余次数不足 */
    ACCOUNT_MONTH_QUOTA_ERROR("ERR_BIZ_007","账户月额度不足"),
    /** 账户日额度不足：参与活动时用户当日剩余次数不足 */
    ACCOUNT_DAY_QUOTA_ERROR("ERR_BIZ_008","账户日额度不足"),
    ;

    private String code;
    private String info;

}
