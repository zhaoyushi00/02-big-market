package com.zhaoyushi.domain.activity.service;

import com.zhaoyushi.domain.activity.model.entity.SkuRechargeEntity;
import com.zhaoyushi.domain.activity.service.quota.AbstractRaffleActivityAccountQuota;

/**
 * 抽奖订单服务接口（领域服务端口）
 *
 * <p>定义“通过 SKU 下单、获得抽奖资格”的用例入口。
 * 实现类：{@link AbstractRaffleActivityAccountQuota}（抽象模板，固化标准下单流程）。
 *
 * @author zhaoy
 * @date 2026/10/23
 * @description 抽奖订单服务接口
 */
public interface IRaffleActivityAccountQuotaService {

    /**
     * 创建 sku 账户充值订单，给用户增加抽奖次数
     * <p>
     * 1. 在【打卡、签到、分享、对话、积分兑换】等行为动作下，创建出活动订单，给用户的活动账户【日、月】充值可用的抽奖次数。
     * 2. 对于用户可获得的抽奖次数，比如首次进来就有一次，则是依赖于运营配置的动作，在前端页面上。用户点击后，可以获得一次抽奖次数。
     *
     * @param skuRechargeEntity 活动商品充值实体对象（用户ID + SKU + 外部业务单号）
     * @return 活动订单号（orderId），作为本次充值下单的凭证
     */
    String createSkuRechargeOrder(SkuRechargeEntity skuRechargeEntity);

    /**
     * 查询用户今日已参与抽奖次数
     * <p>按「用户 + 活动 + 当天日期」统计该用户今日已使用的抽奖额度，
     * 供奖品列表接口判断奖品解锁状态（是否达到「抽奖 N 次解锁」门槛）。
     *
     * @param activityId 活动ID
     * @param userId     用户ID
     * @return 用户今日已参与抽奖次数
     */
    Integer queryRaffleActivityAvvountDayPartakeCount(Long activityId, String userId);
}
