package com.zhaoyushi.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 活动账户（日）实体对象
 *
 * <p>按「用户 + 活动 + 日期（yyyy-mm-dd）」维度记录当日的抽奖次数额度，对应表 raffle_activity_account_day。
 * 与总账户 raffle_activity_account 的关系：总账户存的是「当前日」的日额度镜像（day_count_surplus），
 * 日账户表则按天保留每天的历史额度记录，跨天时各自独立、互不影响。
 *
 * @author zhaoy
 * @description 活动账户（日）实体对象
 * @create 2024-04-05 08:37
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityAccountDayEntity {

    /** 用户ID：账户归属的用户 */
    private String userId;
    /** 活动ID：账户归属的活动 */
    private Long activityId;
    /** 日期（yyyy-mm-dd）：日账户的时间维度 */
    private String day;
    /** 日次数：当日的抽奖次数上限 */
    private Integer dayCount;
    /** 日次数-剩余：当日剩余可抽奖次数 */
    private Integer dayCountSurplus;

}
