package com.zhaoyushi.trigger.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhaoy
 * @date 2026/10/14
 * @description 抽奖品列表，应答对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RaffleAwardListResponseDTO {

    //奖品ID
    private Integer awardId;
    //奖品标题
    private String awardTitle;
    //奖品副标题【抽奖1次后解锁】
    private String awardSubtitle;
    //排序编号
    private String sort;
    // 降频次数规则（抽奖 N 次后解锁）：取自规则树中 rule_lock 节点的 rule_value，未配置锁定规则时为 null
    private Integer awardRuleLockCount;
    // 奖品是否解锁：true=已解锁（可抽中）；false=未解锁（抽奖次数未达标）。未配置锁定规则时恒为 true
    private Boolean isAwardUnlock;
    // 等待解锁次数：锁定规则要求的抽奖次数 N 减去用户已抽奖次数，已解锁或未配置规则时为 0
    private Integer waitUnlockCount;

}
