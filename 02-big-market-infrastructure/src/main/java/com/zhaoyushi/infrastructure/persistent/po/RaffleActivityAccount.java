package com.zhaoyushi.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author zhaoy
 * @description 抽奖活动账户表 持久化对象
 * 一个活动 → 多个用户账户（1:N）。唯一键保证「同一个用户 + 同一个活动，只有一条账户记录」，抽奖扣次数时就是更新这条记录
 *
 * <p>镜像额度说明：本表除保存总/日/月额度外，day_count_surplus 与 month_count_surplus 还充当「当前日/当前月」
 * 的额度镜像——它们只对应最新一天的日账户、最新一月的月账户。跨天/跨月时，由新建日/月账户后回写镜像额度保持一致。
 *
 * @create 2024-03-02 13:15 活动次数账户（用户活动次数汇总）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaffleActivityAccount {

    /**
     * 自增ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 总次数
     */
    private Integer totalCount;

    /**
     * 总次数-剩余
     */
    private Integer totalCountSurplus;

    /**
     * 日次数
     */
    private Integer dayCount;

    /**
     * 日次数-剩余
     */
    private Integer dayCountSurplus;

    /**
     * 月次数
     */
    private Integer monthCount;

    /**
     * 月次数-剩余
     */
    private Integer monthCountSurplus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}
