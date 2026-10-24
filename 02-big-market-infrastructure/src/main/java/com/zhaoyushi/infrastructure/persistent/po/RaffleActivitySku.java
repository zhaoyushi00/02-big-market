package com.zhaoyushi.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;

/**
 * 抽奖活动 sku 持久化对象
 *
 * <p>对应数据库表 raffle_activity_sku，把「活动 + 次数配置」的每一个组合当做一个商品：
 * 携带库存限制与次数配置引用。
 *
 * @author zhaoy
 * @date 2026/10/23
 * @description 抽奖活动 sku 持久化对象
 */
@Data
public class RaffleActivitySku {

    /**
     * 商品sku：唯一标识“活动+次数配置”组合商品
     */
    private Long sku;

    /**
     * 活动ID：所属抽奖活动
     */
    private Long activityId;

    /**
     * 活动个人参与次数ID：指向次数配置表
     */
    private Long activityCountId;

    /**
     * 库存总量：可发放总份数，0 表示不限库存
     */
    private Integer stockCount;

    /**
     * 剩余库存：剩余可发放份数，0 表示不限库存
     */
    private Integer stockCountSurplus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}
