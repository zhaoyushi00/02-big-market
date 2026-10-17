package com.zhaoyushi.trigger.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhaoy
 * @date 2026/10/14
 * @description 抽奖应答结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RaffleResponseDTO {

    //奖品ID
    private Integer awardId;
    //排序编号【策略奖品配置的奖品顺序编号】
    private Integer awardIndex;

}
