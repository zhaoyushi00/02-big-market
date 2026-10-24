package com.zhaoyushi.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 活动账户实体对象
 *
 * <p>对应数据库表 raffle_activity_account（抽奖活动账户表），描述“某个用户在某活动下的次数账户”：
 * 记录该用户被分配的总额度（totalCount/dayCount/monthCount）以及当前剩余额度（*Surplus）。
 *
 * <p>业务位置：下单服务在用户通过 SKU 领取活动时，依据次数配置初始化本账户；
 * 后续每次抽奖扣减 *Surplus，余额不足则拒绝参与。
 *
 * <p>关键点：
 * 1. 账户维度是「用户 × 活动」，唯一键保证同一用户同一活动只有一条账户记录；
 * 2. 同时保留总额度与剩余额度：总额度用于展示/对账，剩余额度用于判断是否还能参与；
 * 3. 本实体是“实例”（会随抽奖动态变化），区别于“模板” ActivityCountEntity。
 *
 * @author zhaoy
 * @date 2026/10/24
 * @description 活动账户实体对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityAccountEntity {

    /**
     * 用户ID：账户归属的用户
     */
    private String userId;

    /**
     * 活动ID：账户对应的活动
     */
    private Long activityId;

    /**
     * 总次数：该用户在本活动被分配的总次数额度
     */
    private Integer totalCount;

    /**
     * 总次数-剩余：总次数维度还剩多少次可用
     */
    private Integer totalCountSurplus;

    /**
     * 日次数：该用户单日可用的次数额度
     */
    private Integer dayCount;

    /**
     * 日次数-剩余：日次数维度还剩多少次可用
     */
    private Integer dayCountSurplus;

    /**
     * 月次数：该用户单月可用的次数额度
     */
    private Integer monthCount;

    /**
     * 月次数-剩余：月次数维度还剩多少次可用
     */
    private Integer monthCountSurplus;

}
