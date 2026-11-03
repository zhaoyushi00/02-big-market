package com.zhaoyushi.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import com.zhaoyushi.infrastructure.persistent.po.RaffleActivityAccountMonth;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zhaoy
 * @description 抽奖活动账户表-月次数
 * @create 2024-04-03 15:57
 */
@Mapper
public interface IRaffleActivityAccountMonthDao {
    /**
     * 扣减月账户额度（当月剩余次数减 1）
     *
     * <p>带库存保护：仅当当月剩余次数大于 0 时才扣减。
     *
     * @param raffleActivityAccountMonth 扣减目标（userId + activityId + month）
     * @return 受影响行数（0 表示额度不足或账户不存在）
     */
    int updateActivityAccountMonthSubtractionQuota(RaffleActivityAccountMonth raffleActivityAccountMonth);

    /**
     * 插入一条月账户记录（月账户首次建立时调用）
     *
     * @param raffleActivityAccountMonth 月账户持久化对象
     */
    void insertActivityAccountMonth(RaffleActivityAccountMonth raffleActivityAccountMonth);

    /**
     * 根据用户、活动与月份查询月账户（带分库分表路由）
     *
     * @param raffleActivityAccountMonthReq 查询条件（userId + activityId + month）
     * @return 月账户，未命中时返回 null
     */
    @DBRouter
    RaffleActivityAccountMonth queryActivityAccountMonthByUserId(RaffleActivityAccountMonth raffleActivityAccountMonthReq);
}
