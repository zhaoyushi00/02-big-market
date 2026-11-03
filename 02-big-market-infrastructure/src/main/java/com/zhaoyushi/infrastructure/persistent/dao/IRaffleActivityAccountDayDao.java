package com.zhaoyushi.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import com.zhaoyushi.infrastructure.persistent.po.RaffleActivityAccountDay;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zhaoy
 * @description 抽奖活动账户表-日次数
 * @create 2024-04-03 15:56
 */
@Mapper
public interface IRaffleActivityAccountDayDao {
    /**
     * 扣减日账户额度（当日剩余次数减 1）
     *
     * <p>带库存保护：仅当当日剩余次数大于 0 时才扣减。
     *
     * @param raffleActivityAccountDay 扣减目标（userId + activityId + day）
     * @return 受影响行数（0 表示额度不足或账户不存在）
     */
    int updateActivityAccountDaySubtractionQuota(RaffleActivityAccountDay raffleActivityAccountDay);

    /**
     * 插入一条日账户记录（日账户首次建立时调用）
     *
     * @param raffleActivityAccountDay 日账户持久化对象
     */
    void insertActivityAccountDay(RaffleActivityAccountDay raffleActivityAccountDay);

    /**
     * 根据用户、活动与日期查询日账户（带分库分表路由）
     *
     * @param raffleActivityAccountDayReq 查询条件（userId + activityId + day）
     * @return 日账户，未命中时返回 null
     */
    @DBRouter
    RaffleActivityAccountDay queryActivityAccountDayByUserId(RaffleActivityAccountDay raffleActivityAccountDayReq);
}
