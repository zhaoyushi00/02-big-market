package com.zhaoyushi.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import com.zhaoyushi.infrastructure.persistent.po.UserRaffleOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zhaoy
 * @description 用户抽奖订单表
 * @create 2024-04-03 15:57
 */
@Mapper
@DBRouterStrategy(splitTable = true)
public interface IUserRaffleOrderDao {
    /**
     * 插入一条用户抽奖订单（参与活动时生成）
     *
     * @param userRaffleOrder 用户抽奖订单持久化对象
     */
    void insert(UserRaffleOrder userRaffleOrder);

    /**
     * 查询用户未使用的抽奖单（order_state = 'create'）
     *
     * <p>用于幂等：若用户已存在未消费的抽奖单，则直接复用，不再扣减账户额度。
     *
     * @param userRaffleOrderReq 查询条件（userId + activityId）
     * @return 未使用的抽奖单，不存在时返回 null
     */
    @DBRouter
    UserRaffleOrder queryNoUsedRaffleOrder(UserRaffleOrder userRaffleOrderReq);

    /**
     * 更新用户抽奖单状态为已使用（used）
     * <p>在中奖记录写入时调用，将 order_state 从 create 更新为 used，防止同一抽奖单被重复抽奖。
     *
     * @param userRaffleOrderReq 更新条件（userId + orderId）
     * @return 受影响行数，返回 1 表示更新成功
     */
    int updateUserRaffleOrderStateUsed(UserRaffleOrder userRaffleOrderReq);
}
