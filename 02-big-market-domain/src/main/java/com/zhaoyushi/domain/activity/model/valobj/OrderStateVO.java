package com.zhaoyushi.domain.activity.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举值对象（用于描述对象属性的值，如枚举，不影响数据库操作的对象，无生命周期）
 *
 * <p>用于描述订单实体 ActivityOrderEntity 的状态属性。
 * 当前下单流程为“领取即完成”，故仅有 completed 一种状态。
 *
 * @author zhaoy
 * @date 2026/10/24
 * @description 订单状态值对象
 */
@Getter
@AllArgsConstructor
public enum OrderStateVO {

    /** 完成：订单已创建，用户已获得参与抽奖的资格 */
    completed("completed", "完成");

    /** 状态编码：存入数据库的状态值 */
    private final String code;
    /** 状态描述：状态的中文说明 */
    private final String desc;

}
