package com.zhaoyushi.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    /** 策略ID */
    private Long strategyId;
    /** 奖品ID */
    private Integer awardId;
    /** 奖品对接标识 - 每一个都是一个对应的发奖策略 */
    private String awardKey;
    /** 奖品配置信息 */
    private String awardConfig;
    /** 奖品内容描述 */
    private String awardDesc;

}
