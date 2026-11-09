package com.zhaoyushi.domain.activity.service;

import com.zhaoyushi.domain.activity.model.entity.PartakeRaffleActivityEntity;
import com.zhaoyushi.domain.activity.model.entity.UserRaffleOrderEntity;

/**
 * @author zhaoy
 * @date 2026/11/1
 * @description 抽奖活动参与服务
 */
public interface IRaffleActivityPartakeService {

    /**
     * 创建抽奖单；
     * 用户参与抽奖活动，扣减活动账户库存，产生抽奖单。
     * 若存在未被使用的抽奖单，则直接返回已存在的抽奖单。
     *
     *  @param partakeRaffleActivityEntity 参与抽奖活动实体对象
     *  @return 用户抽奖订单实体对象
     */
    UserRaffleOrderEntity createOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity);

    /**
     * 创建抽奖单（便捷重载）
     * <p>用户参与抽奖活动，扣减活动账户库存，产生抽奖单。如存在未被使用的抽奖单则直接返回已存在的抽奖单。
     * 与 {@link #createOrder(PartakeRaffleActivityEntity)} 的区别在于：本方法直接接收用户 ID 与活动 ID，
     * 由实现类内部组装参与活动实体，简化调用方的组装成本。
     *
     * @param userId     用户ID
     * @param activityId 活动ID
     * @return 用户抽奖订单实体对象
     */
    UserRaffleOrderEntity createOrder(String userId, Long activityId);

}
