package com.zhaoyushi.domain.activity.model.entity;

import com.zhaoyushi.domain.activity.model.valobj.ActivityStateVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 活动实体对象
 *
 * <p>对应数据库表 raffle_activity（抽奖活动表），描述“一个抽奖活动”的领域模型：
 * 活动的时间范围、名称描述、所属策略与状态等。
 *
 * <p>业务位置：下单服务通过 {@link com.zhaoyushi.domain.activity.repository.IActivityRepository#queryRaffleActivityByActivityId}
 * 查询本实体，用于校验活动状态、获取活动名称与策略。
 *
 * <p>关键点：
 * 1. strategyId 是“抽奖策略”的引用，抽奖时按此关联策略/奖品/规则树；
 * 2. state 用 ActivityStateVO 枚举表达（create/open/close），下单前需校验活动处于 open 状态；
 * 3. activityCountId 为次数配置的冗余引用（SKU 层也持有，实际以 SKU 为准）。
 *
 * @author zhaoy
 * @date 2026/10/24
 * @description 活动实体对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityEntity {

    /**
     * 活动ID：唯一标识一个抽奖活动
     */
    private Long activityId;

    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 活动描述
     */
    private String activityDesc;

    /**
     * 开始时间：活动可参与的时间下界
     */
    private Date beginDateTime;

    /**
     * 结束时间：活动可参与的时间上界
     */
    private Date endDateTime;

    /**
     * 活动参与次数配置：指向次数配置表的编号（冗余引用，SKU 层也持有）
     */
    private Long activityCountId;

    /**
     * 抽奖策略ID：关联抽奖策略，决定奖品概率与规则
     */
    private Long strategyId;

    /**
     * 活动状态：create(创建)/open(开启)/close(关闭)
     */
    private ActivityStateVO state;

}
