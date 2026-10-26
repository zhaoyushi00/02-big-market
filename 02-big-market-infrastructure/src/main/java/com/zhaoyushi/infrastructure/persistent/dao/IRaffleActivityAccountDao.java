package com.zhaoyushi.infrastructure.persistent.dao;


import com.zhaoyushi.infrastructure.persistent.po.RaffleActivityAccount;
import org.apache.ibatis.annotations.Mapper;

/**
 * 抽奖活动账户表(RaffleActivityAccount)表数据库访问层
 *
 * @author makejava
 * @since 2026-10-21 20:27:00
 */
@Mapper
public interface IRaffleActivityAccountDao {

    /**
     * 插入一条活动账户记录（账户首次建立时调用）
     *
     * @param raffleActivityAccount 活动账户持久化对象
     */
    void insert(RaffleActivityAccount raffleActivityAccount);

    /**
     * 累加活动账户额度（总/日/月次数）
     *
     * <p>按 userId + activityId 定位账户，将传入的总/日/月次数累加到已有额度上。
     *
     * @param raffleActivityAccount 需累加的活动账户持久化对象
     * @return 受影响的行数（0 表示账户不存在，调用方需转新增）
     */
    int updateAccountQuota(RaffleActivityAccount raffleActivityAccount);
}

