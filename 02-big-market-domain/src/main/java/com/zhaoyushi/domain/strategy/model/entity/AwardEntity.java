package com.zhaoyushi.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 策略结果实体 - 抽奖结果的最简载体
 *
 * <p>用途：记录一次策略抽奖的最终结果，即「哪个用户(userId)抽中了哪个奖品(awardId)」。
 * <p>业务位置：抽奖流程早期版本的返回结果对象；奖品信息更完整的版本见 {@link RaffleAwardEntity}。
 *
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @create 2023-12-23 09:13
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AwardEntity {

    /** 用户ID */
    private String userId;
    /** 奖品ID */
    private Integer awardId;

}
