package com.zhaoyushi.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
/**
 * 抽奖奖品实体 - 抽奖最终返回给调用方的奖品信息
 *
 * <p>用途：承载抽奖流程的最终出参，描述抽中的奖品及其发奖所需的配置。
 * <p>业务位置：{@code IRaffleStrategy.performRaffle} 的返回类型，抽奖主流程的终点。
 * <p>关键字段：awardKey 是发奖对接标识（对应不同的发奖策略），awardConfig 是奖品配置信息。
 */
public class RaffleAwardEntity {

    /** 奖品ID */
    private Integer awardId;
    /** 抽奖奖品标题 */
    private String awardTitle;
    /** 奖品配置信息 */
    private String awardConfig;
    /** 奖品顺序号 */
    private Integer sort;
    /** 活动结束时间：随抽奖结果一起带回，用于给「库存扣减锁 Key」设置过期时间，避免锁 Key 在活动结束后仍永久驻留 Redis */
    private Date endDateTime;

}
