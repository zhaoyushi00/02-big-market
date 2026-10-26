package com.zhaoyushi.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import com.zhaoyushi.infrastructure.persistent.po.RaffleActivityOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * 抽奖活动单(RaffleActivityOrder)表数据库访问层
 *
 * @author makejava
 * @since 2026-10-21 20:27:00
 */
@Mapper
@DBRouterStrategy(splitTable = true)   // 类级：开启分表
public interface IRaffleActivityOrderDao {

    /**
     * 插入一条活动订单记录
     *
     * <p>注意：分表路由不再通过注解（@DBRouter）指定，而是由调用方（{@code ActivityRepository}）
     * 在事务外通过 {@code IDBRouterStrategy.doRouter(userId)} 手动设定，以配合编程式事务保证同一连接。
     *
     * @param raffleActivityOrder 活动订单持久化对象
     */
    void insert(RaffleActivityOrder raffleActivityOrder);

    /**
     * 根据用户ID查询其全部活动订单记录
     *
     * @param userId 用户ID
     * @return 活动订单列表
     */
    @DBRouter
    List<RaffleActivityOrder> queryRaffleActivityOrderByUserId(String userId);

}

