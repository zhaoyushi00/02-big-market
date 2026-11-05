// 声明当前类所属的包路径：领域层 - 奖品模块 - 服务实现
package com.zhaoyushi.domain.award.service;

// 引入发奖消息事件，用于构建发奖 MQ 消息体与获取消息主题
import com.zhaoyushi.domain.award.event.SendAwardMessageEvent;
// 引入用户中奖记录聚合对象，作为组装结果传入仓储层
import com.zhaoyushi.domain.award.model.aggregate.UserAwardRecordAggregate;
// 引入任务实体对象，用于承载待发送的发奖消息
import com.zhaoyushi.domain.award.model.entity.TaskEntity;
// 引入用户中奖记录实体对象，作为方法入参
import com.zhaoyushi.domain.award.model.entity.UserAwardRecordEntity;
// 引入任务状态枚举值对象，用于设置任务初始状态为「创建」
import com.zhaoyushi.domain.award.model.valobj.TaskStateVO;
// 引入奖品仓储接口，用于持久化聚合对象
import com.zhaoyushi.domain.award.repository.IAwardRepository;
// 引入基础事件抽象类，使用其内部 EventMessage 作为消息主体类型
import com.zhaoyushi.types.event.BaseEvent;
// 引入 JSR-250 的 @Resource 注解，用于按名称注入依赖
import jakarta.annotation.Resource;
// 引入 @Service 注解，将本类注册为 Spring 服务 Bean
import org.springframework.stereotype.Service;

/**
 * 奖品服务实现（领域层 - 奖品模块）
 *
 * <p>「写入中奖记录」的核心编排逻辑：
 * 1. 组装发奖消息数据体（发给谁、发什么奖品）；
 * 2. 基于消息数据体构建统一结构的事件消息（含 id、时间戳、数据）；
 * 3. 构建任务实体（携带消息、主题、消息 ID、初始状态 create）；
 * 4. 将「中奖记录实体 + 任务实体」聚合为一个聚合对象；
 * 5. 交由仓储层在一个事务内统一落库（中奖记录 + 任务），事务外再发 MQ。
 *
 * @author zhaoy
 * @date 2026/11/4
 * @description 奖品服务实现
 */
// 注册为 Spring 服务 Bean
@Service
public class AwardService implements IAwardService{

    // 注入奖品仓储接口实现，负责聚合对象的持久化
    @Resource
    private IAwardRepository awardRepository;
    // 注入发奖消息事件，用于构建事件消息体与获取消息主题
    @Resource
    private SendAwardMessageEvent sendAwardMessageEvent;

    /**
     * 保存用户中奖记录
     *
     * <p>组装发奖消息、任务对象与聚合对象，最终委托仓储层完成事务落库。
     *
     * @param userAwardRecordEntity 用户中奖记录实体对象
     */
    @Override
    public void saveUserAwardRecord(UserAwardRecordEntity userAwardRecordEntity) {

        // 1. 构建发奖消息数据体对象（后续作为事件消息的内容）
        SendAwardMessageEvent.SendAwardMessage sendAwardMessage = new SendAwardMessageEvent.SendAwardMessage();
        // 设置奖品 ID（从入参中奖记录中获取）
        sendAwardMessage.setAwardId(userAwardRecordEntity.getAwardId());
        // 设置奖品标题（从入参中奖记录中获取）
        sendAwardMessage.setAwardTitle(userAwardRecordEntity.getAwardTitle());
        // 设置用户 ID（从入参中奖记录中获取）
        sendAwardMessage.setUserId(userAwardRecordEntity.getUserId());

        // 2. 基于发奖数据体构建统一结构的事件消息（自动生成 id 与时间戳）
        BaseEvent.EventMessage<SendAwardMessageEvent.SendAwardMessage> sendAwardMessageEventMessage = sendAwardMessageEvent.buildEventMessage(sendAwardMessage);

        // 3. 构建任务实体对象，用于承载待发送的发奖消息
        TaskEntity taskEntity = new TaskEntity();
        // 设置消息主体为刚构建的事件消息
        taskEntity.setMessage(sendAwardMessageEventMessage);
        // 设置消息编号为事件消息的唯一 ID
        taskEntity.setMessageId(sendAwardMessageEventMessage.getId());
        // 设置任务初始状态为「创建」（create，待发送）
        taskEntity.setState(TaskStateVO.create);
        // 设置消息主题为发奖事件声明的 topic
        taskEntity.setTopic(sendAwardMessageEvent.topic());
        // 设置用户 ID（与中奖记录保持一致，作为分库分表路由键）
        taskEntity.setUserId(userAwardRecordEntity.getUserId());

        // 4. 组装聚合对象，将「中奖记录实体 + 任务实体」聚合为一个事务单元
        UserAwardRecordAggregate userAwardRecordAggregate = UserAwardRecordAggregate.builder()
                // 设置任务实体
                .taskEntity(taskEntity)
                // 设置用户中奖记录实体
                .userAwardRecordEntity(userAwardRecordEntity)
                // 完成聚合构建
                .build();

        // 5. 委托仓储层保存聚合对象（一个事务下同时写入用户中奖记录与任务记录）
        awardRepository.saveUserAwardRecord(userAwardRecordAggregate);
    }
}
