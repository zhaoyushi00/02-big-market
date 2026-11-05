// 声明当前类所属的包路径：基础设施层 - 持久化 - DAO
package com.zhaoyushi.infrastructure.persistent.dao;

// 引入分库分表策略注解，声明本表需要按 userId 进行分库分表路由
import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
// 引入用户中奖记录表持久化对象，作为 DAO 方法的入参
import com.zhaoyushi.infrastructure.persistent.po.UserAwardRecord;
// 引入 MyBatis 的 @Mapper 注解，将本接口注册为 MyBatis Mapper
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
}
