package com.zhaoyushi.domain.rebate.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhaoy
 * @date 2026/11/16
 * @description 返利类型（sku 活动库存充值商品、integral 用户活动积分）
 */
@Getter
@AllArgsConstructor
public enum RebateTypeVO {

    SKU("sku", "活动库存充值商品"),
    INTEGRAL("integral", "用户活动积分"),
    ;

    private final String code;
    private final String info;

}
