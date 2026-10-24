package com.zhaoyushi.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 活动购物车实体对象
 *
 * <p>领域层「购物车」模型，是“用户领取活动”这个下单入口的入参载体：
 * 用户选定某个商品 SKU 后，本实体携带「谁 + 领了哪个 SKU」两个要素，
 * 传给下单服务 {@link com.zhaoyushi.domain.activity.service.IRaffleOrder#createRaffleActivityOrder} 驱动“创建活动订单”用例。
 *
 * <p>业务位置：trigger 层组装本实体 → 作为领域服务入参 → 触发下单链路。
 *
 * <p>关键点：
 * 1. sku 是「活动 + 次数配置」的组合标识（语义为 activity + activity count），不是普通商品编号；
 * 2. 本实体只承载下单入口所需的最小要素，后续链路所需的更多字段由领域内部自行补齐。
 *
 * @author zhaoy
 * @date 2026/10/24
 * @description 活动购物车实体对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityShopCartEntity {

    /** 用户ID：标识“谁”在领取活动（下单主体） */
    private String userId;

    /** 商品SKU：标识“领了哪个商品”，语义为 activity + activity count 的组合（如 9011 代表活动100301+次数配置11101） */
    private Long sku;

}
