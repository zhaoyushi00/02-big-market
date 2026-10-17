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

}
