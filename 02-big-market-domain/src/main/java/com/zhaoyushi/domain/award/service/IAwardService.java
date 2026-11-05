// 声明当前类所属的包路径：领域层 - 奖品模块 - 服务接口
package com.zhaoyushi.domain.award.service;

// 引入用户中奖记录实体对象，作为服务方法的入参类型
import com.zhaoyushi.domain.award.model.entity.UserAwardRecordEntity;
// 引入 @Service 注解（当前接口未直接使用，保留以备用）
import org.springframework.stereotype.Service;

/**
 * 奖品服务接口（领域层 - 奖品模块）
 *
 * <p>定义「用户中奖记录」相关的领域服务能力，是上层（HTTP 控制器等）写入中奖记录的入口。
 *
 * @author zhaoy
 * @date 2026/11/4
 * @description 奖品服务接口
 */
public interface IAwardService {

    /**
     * 保存用户中奖记录
     *
     * <p>构建发奖消息与任务对象，并通过仓储在同一事务内完成落库，
     * 事务提交后再发送发奖 MQ 消息。
     *
     * @param userAwardRecordEntity 用户中奖记录实体对象
     */
    void saveUserAwardRecord(UserAwardRecordEntity userAwardRecordEntity);

}
