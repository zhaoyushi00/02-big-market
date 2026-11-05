// 声明当前类所属的包路径：领域层 - 奖品模块 - 仓储接口
package com.zhaoyushi.domain.award.repository;

// 引入用户中奖记录聚合对象，作为仓储方法的入参类型
import com.zhaoyushi.domain.award.model.aggregate.UserAwardRecordAggregate;
// 引入用户中奖记录实体对象（当前接口未直接使用，保留以备用）
import com.zhaoyushi.domain.award.model.entity.UserAwardRecordEntity;

/**
 * 奖品服务仓储接口（领域层 - 奖品模块）
 *
 * <p>定义「用户中奖记录」的持久化边界，向上屏蔽底层数据库实现，
 * 由基础设施层（infrastructure）提供具体实现。
 *
 * @author zhaoy
 * @date 2026/11/4
 * @description 奖品服务接口
 */
public interface IAwardRepository {

    /**
     * 保存用户中奖记录（聚合）
     *
     * <p>在同一个事务内写入用户中奖记录与消息任务记录，保证二者数据一致性。
     *
     * @param userAwardRecordAggregate 用户中奖记录聚合对象
     */
    void saveUserAwardRecord(UserAwardRecordAggregate userAwardRecordAggregate);

}
