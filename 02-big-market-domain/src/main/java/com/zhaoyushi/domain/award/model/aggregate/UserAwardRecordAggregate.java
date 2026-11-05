// 声明当前类所属的包路径：领域层 - 奖品模块 - 聚合根
package com.zhaoyushi.domain.award.model.aggregate;

// 引入任务实体对象，作为聚合内的一个组成部分（同事务写入任务表）
import com.zhaoyushi.domain.award.model.entity.TaskEntity;
// 引入用户中奖记录实体对象，作为聚合内的一个组成部分（同事务写入中奖表）
import com.zhaoyushi.domain.award.model.entity.UserAwardRecordEntity;
// 引入 Lombok 全参构造器注解，自动生成包含所有字段的构造方法
import lombok.AllArgsConstructor;
// 引入 Lombok Builder 注解，支持以建造者模式链式创建对象
import lombok.Builder;
// 引入 Lombok Data 注解，自动生成 getter/setter/toString/equals/hashCode
import lombok.Data;
// 引入 Lombok 无参构造器注解，自动生成无参构造方法
import lombok.NoArgsConstructor;

/**
 * 用户中奖记录聚合对象（领域层 - 奖品模块）
 *
 * <p>聚合根代表一个完整的业务事务边界：一次「写入中奖记录」需要在同一个事务内
 * 同时写入两条数据——用户中奖记录（user_award_record）与消息任务记录（task），
 * 二者通过本聚合聚合在一起，交由仓储层在一个事务里统一落库，保证数据一致性。
 *
 * @author zhaoy
 * @date 2026/11/4
 * @description 用户中奖记录集合对象 【聚合代表一个事务操作】
 */
// 使用 Lombok 生成包含全部字段的构造方法
@AllArgsConstructor
// 使用 Lombok 生成无参构造方法
@NoArgsConstructor
// 使用 Lombok 自动生成 getter/setter/toString/equals/hashCode 等方法
@Data
// 使用 Lombok 生成建造者模式，支持链式构建
@Builder
public class UserAwardRecordAggregate {

    // 用户中奖记录实体：本次中奖的明细信息，需写入 user_award_record 表
    private UserAwardRecordEntity userAwardRecordEntity;

    // 任务实体：承载待发送的发奖 MQ 消息，需写入 task 表用于后续补偿发送
    private TaskEntity taskEntity;

}
