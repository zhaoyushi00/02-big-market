// 声明当前类所属的包路径：基础设施层 - 持久化 - DAO
package com.zhaoyushi.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import com.zhaoyushi.infrastructure.persistent.po.UserAwardRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户中奖记录表 DAO（数据访问接口）
 *
 * <p>定义 user_award_record 表（用户中奖记录表）的写入操作，由 MyBatis 映射 XML 提供具体 SQL 实现。
 * 该表按 userId 分库分表存储，用于记录每次抽奖的中奖明细。
 *
 * @author zhaoy
 * @description 用户中奖记录表
 * @create 2024-04-03 15:57
 */
// 注册为 MyBatis Mapper 接口
@Mapper
// 声明分库分表策略：splitTable=true 表示本表需要分表（按 userId 路由）
@DBRouterStrategy(splitTable = true)
public interface IUserAwardRecordDao {
    /**
     * 插入一条用户中奖记录
     *
     * @param userAwardRecord 用户中奖记录持久化对象
     */
    void insert(UserAwardRecord userAwardRecord);

    /**
     * 将中奖记录状态更新为发奖完成
     *
     * @param userAwardRecordReq 用户中奖记录持久化对象（携带 userId、orderId）
     * @return 受影响的行数（为 0 表示记录不存在或已重复更新）
     */
    int updateAwardRecordCompletedState(UserAwardRecord userAwardRecordReq);
}
