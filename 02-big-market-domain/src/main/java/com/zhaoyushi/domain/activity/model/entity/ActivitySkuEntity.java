package com.zhaoyushi.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 活动 SKU 实体对象
 *
 * <p>对应数据库表 raffle_activity_sku（抽奖活动 sku 表），是「把每一个组合当做一个商品」的领域模型：
 * 一个 SKU = 某个活动下的一种“次数商品”，同时携带库存限制与次数配置引用。
 *
 * <p>业务位置：领域仓储 {@link com.zhaoyushi.domain.activity.repository.IActivityRepository#queryActivitySku}
 * 从基础设施层查询并组装本实体，供下单服务读取活动与次数信息。
 *
 * <p>关键点：
 * 1. activityCountId 是“参与次数配置”的引用，指向 raffle_activity_count 表；
 * 2. stockCount / stockCountSurplus 是“该商品可发放总量”的限制，值为 0 表示不限库存；
 * 3. sku 是「活动 + 次数配置」组合的唯一标识。
 *
 * @author zhaoy
 * @date 2026/10/24
 * @description 活动 SKU 实体对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivitySkuEntity {

    /** 商品sku：唯一标识一个“活动+次数配置”组合商品 */
    private Long sku;

    /** 活动ID：该 SKU 所属的抽奖活动 */
    private Long activityId;

    /** 活动个人参与次数ID：指向次数配置表，表示“在这个活动上一个人可参与多少次（总、日、月）” */
    private Long activityCountId;

    /** 库存总量：该商品可发放的总份数上限，0 表示不限库存 */
    private Integer stockCount;

    /** 剩余库存：还能发放多少份，0 表示不限库存 */
    private Integer stockCountSurplus;

}
