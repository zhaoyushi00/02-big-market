package com.zhaoyushi.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 活动账户（月）实体对象
 *
 * <p>按「用户 + 活动 + 月（yyyy-mm）」维度记录当月的抽奖次数额度，对应表 raffle_activity_account_month。
 * 与总账户 raffle_activity_account 的关系：总账户存的是「当前月」的月额度镜像（month_count_surplus），
 * 月账户表则按月保留每月的历史额度记录，跨月时各自独立、互不影响。
 *
 * @author zhaoy
 * @description 活动账户（月）实体对象
 * @create 2024-04-05 08:38
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityAccountMonthEntity {

    /** 用户ID：账户归属的用户 */
    private String userId;
    /** 活动ID：账户归属的活动 */
    private Long activityId;
    /** 月（yyyy-mm）：月账户的时间维度 */
    private String month;
    /** 月次数：当月的抽奖次数上限 */
    private Integer monthCount;
    /** 月次数-剩余：当月剩余可抽奖次数 */
    private Integer monthCountSurplus;

}
