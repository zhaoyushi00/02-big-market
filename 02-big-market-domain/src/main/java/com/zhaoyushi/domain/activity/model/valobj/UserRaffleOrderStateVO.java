package com.zhaoyushi.domain.activity.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户抽奖订单状态枚举（值对象）
 *
 * <p>描述用户抽奖单 {@link com.zhaoyushi.domain.activity.model.entity.UserRaffleOrderEntity} 的生命周期：
 * create（创建）-> used（已使用）/ cancel（已作废）。
 * 领域层用枚举表达，落库时取 code 字符串；读库时用 valueOf 还原。
 *
 * @author zhaoy
 * @date 2026/11/1
 * @description 用户抽奖订单状态枚举
 */
@Getter
@AllArgsConstructor
public enum UserRaffleOrderStateVO {

    /** 创建：订单已生成但尚未被用于抽奖 */
    create("create", "创建"),
    /** 已使用：订单已被消费（兑换了抽奖资格） */
    used("used", "已使用"),
    /** 已作废：订单被取消/作废 */
    cancel("cancel", "已作废"),
    ;

    /** 状态码：落库时写入 order_state 字段 */
    private final String code;
    /** 状态描述：中文说明 */
    private final String desc;

}
