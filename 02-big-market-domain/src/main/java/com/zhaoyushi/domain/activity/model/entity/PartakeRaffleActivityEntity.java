package com.zhaoyushi.domain.activity.model.entity;

import lombok.Data;

/**
 * 参与抽奖活动的实体对象（入参）
 *
 * <p>作为 {@code IRaffleActivityPartakeService#createOrder} 的入参，
 * 只携带「谁参与 + 参与哪个活动」两个信息，其余数据（账户、订单）由服务内部组装。
 *
 * @author zhaoy
 * @date 2026/11/1
 * @description  参与抽奖活动的实体对象
 */
@Data
public class PartakeRaffleActivityEntity {

    /**
     * 用户Id：参与抽奖的主体
     */
    private String userId;

    /**
     * 活动Id：参与的活动编号
     */
    private Long activityId;

}
