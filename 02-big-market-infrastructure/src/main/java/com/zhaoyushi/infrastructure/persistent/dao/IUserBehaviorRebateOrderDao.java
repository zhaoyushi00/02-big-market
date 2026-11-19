package com.zhaoyushi.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import com.zhaoyushi.infrastructure.persistent.po.DailyBehaviorRebate;
import com.zhaoyushi.infrastructure.persistent.po.UserBehaviorRebateOrder;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户行为返利流水订单 DAO（基础设施层）
 *
 * <p>对应数据库表 user_behavior_rebate_order，提供返利流水写入能力，
 * SQL 定义见 user_behavior_rebate_order_mapper.xml。
 *
 * <p>{@code @DBRouterStrategy(splitTable = true)} 表示该表按用户 ID 分库分表，
 * 每次访问前需先通过 {@code IDBRouterStrategy} 完成路由。
 *
 * @author zhaoy
 * @date 2026/11/14
 * @description 用户行为返利流水订单表
 */
@Mapper
@DBRouterStrategy(splitTable = true)
public interface IUserBehaviorRebateOrderDao {

    /**
     * 新增一条用户行为返利流水订单
     *
     * @param userBehaviorRebateOrder 返利订单持久化对象
     */
    void insert(UserBehaviorRebateOrder userBehaviorRebateOrder);

    @DBRouter
    List<UserBehaviorRebateOrder> queryOrderByOutBusinessNo(UserBehaviorRebateOrder userBehaviorRebateOrderReq);
}
