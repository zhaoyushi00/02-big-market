// 声明当前类所属的包路径：领域层 - 奖品模块 - 服务接口
package com.zhaoyushi.domain.award.service;

import com.zhaoyushi.domain.award.model.entity.DistributeAwardEntity;
import com.zhaoyushi.domain.award.model.entity.UserAwardRecordEntity;
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

    /**
     * 分发奖品
     *
     * <p>根据奖品ID查询奖品类型标识（award_key），路由到对应的发奖策略执行发奖。
     *
     * @param distributeAwardEntity 分发奖品实体对象
     */
    void distributeAward(DistributeAwardEntity distributeAwardEntity);

}
