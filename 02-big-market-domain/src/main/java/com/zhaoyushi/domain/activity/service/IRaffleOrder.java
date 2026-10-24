package com.zhaoyushi.domain.activity.service;

import com.zhaoyushi.domain.activity.model.entity.ActivityOrderEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivityShopCartEntity;

/**
 * 抽奖订单服务接口（领域服务端口）
 *
 * <p>定义“通过 SKU 下单、获得抽奖资格”的用例入口。
 * 实现类：{@link AbstractRaffleActivity}（抽象模板，固化标准下单流程）。
 *
 * @author zhaoy
 * @date 2026/10/23
 * @description 抽奖订单服务接口
 */
public interface IRaffleOrder {

    /**
     * 以 sku 创建抽奖活动订单，获得参与抽奖资格（可消耗的次数）
     *
     * <p>用例流程：用户选定 SKU → 查询 SKU/活动/次数配置 → 初始化账户次数 + 生成订单 → 返回订单凭证。
     *
     * @param activityShopCartEntity 活动购物车实体，携带用户ID与商品SKU（通过sku领取活动）
     * @return 活动参与（订单）记录实体，作为用户获得抽奖资格的凭证
     */
    ActivityOrderEntity createRaffleActivityOrder(ActivityShopCartEntity activityShopCartEntity);

}
