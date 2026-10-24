package com.zhaoyushi.infrastructure.persistent.dao;


import com.zhaoyushi.infrastructure.persistent.po.RaffleActivityCount;
import org.apache.ibatis.annotations.Mapper;

/**
 * 抽奖活动次数配置表（RaffleActivityCount）数据库访问层
 *
 * <p>对应 mapper：raffle_activity_count_mapper.xml。
 * 提供按次数配置编号查询次数配置的数据库访问能力。
 *
 * @author zhaoy
 * @date 2026/10/24
 * @description 抽奖活动次数配置表数据库访问层
 */
@Mapper
public interface IRaffleActivityCountDao {

    /**
     * 根据次数配置编号查询次数配置
     *
     * @param activityCountId 活动次数配置编号
     * @return 抽奖活动次数配置持久化对象
     */
    RaffleActivityCount queryRaffleActivityCountByActivityCountId(Long activityCountId);

}

