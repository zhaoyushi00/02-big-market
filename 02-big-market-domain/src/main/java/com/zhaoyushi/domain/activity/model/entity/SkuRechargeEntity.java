package com.zhaoyushi.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 活动商品充值实体对象
 *
 * <p>用于“给用户活动账户充值抽奖次数”的入参对象，对应下单服务
 * {@link com.zhaoyushi.domain.activity.service.IRaffleOrder#createSkuRechargeOrder} 的入参。
 * 携带三个信息：谁充值（userId）、充哪个商品（sku）、本次充值的外部业务单号（outBusinessNo，用于幂等防重）。
 *
 * <p>关键点：outBusinessNo 由外部调用方透传，作为唯一索引字段，保证同一笔业务单号重复调用也不会重复充值。
 *
 * @author zhaoy
 * @date 2026/10/25
 * @description 活动商品充值实体对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SkuRechargeEntity {

    /** 用户ID：标识“谁”在领取活动（下单主体） */
    private String userId;

    /** 商品SKU：标识“领了哪个商品”，语义为 activity + activity count 的组合（如 9011 代表活动100301+次数配置11101） */
    private Long sku;

    /** 幂等业务单号，外部谁充值谁透传，这样保证幂等（多次调用也能确保结果唯一，不会多次充值） */
    private String outBusinessNo;

}
