package com.zhaoyushi.infrastructure.persistent.dao;


import cn.bugstack.middleware.db.router.annotation.DBRouter;
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

    /**
     * 根据用户与活动查询总账户（带分库分表路由）
     *
     * @param raffleActivityAccountReq 查询条件（userId + activityId）
     * @return 活动总账户，未命中时返回 null
     */
    @DBRouter
    RaffleActivityAccount queryActivityAccountByUserId(RaffleActivityAccount raffleActivityAccountReq);

    /**
     * 扣减总账户额度（总/日/月剩余次数各减 1）
     *
     * <p>带库存保护：仅当三项剩余次数都大于 0 时才扣减（SQL 层 where 条件拦截）。
     *
     * @param raffleActivityAccount 扣减目标（userId + activityId）
     * @return 受影响行数（0 表示额度不足或账户不存在，调用方应回滚）
     */
    int updateActivityAccountSubtractionQuota(RaffleActivityAccount raffleActivityAccount);

    /**
     * 更新总账表中的“月额度镜像”（月账户新建后同步当月剩余额度）
     *
     * @param raffleActivityAccount 含 monthCountSurplus 的总账户对象
     */
    void updateActivityAccountMonthSurplusImageQuota(RaffleActivityAccount raffleActivityAccount);

    /**
     * 更新总账表中的“日额度镜像”（日账户新建后同步当日剩余额度）
     *
     * @param raffleActivityAccount 含 dayCountSurplus 的总账户对象
     */
    void updateActivityAccountDaySurplusImageQuota(RaffleActivityAccount raffleActivityAccount);
}

