package com.zhaoyushi.infrastructure.persistent.dao;

import com.zhaoyushi.infrastructure.persistent.po.RaffleActivity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 抽奖活动表(RaffleActivity)表数据库访问层
 *
 * @author makejava
 * @since 2026-10-21 20:26:57
 */
@Mapper
public interface IRaffleActivityDao {

    RaffleActivity queryRaffleActivityByActivityId(Long activityId);

    /**
     * 根据活动 ID 查询绑定的策略 ID
     * <p>活动与策略为 1:1 绑定关系，供「按活动装配策略」时反查策略。
     *
     * @param activityId 活动ID
     * @return 策略ID
     */
    Long queryStrategyIdByActivityId(Long activityId);

    /**
     * 根据策略 ID 查询绑定的活动 ID
     * <p>活动与策略为 1:1 绑定关系，供「按策略查询活动」时反查活动（如查询用户当日抽奖次数）。
     *
     * @param strategyId 策略ID
     * @return 活动ID
     */
    Long queryActivityIdByStrategyId(Long strategyId);
}

