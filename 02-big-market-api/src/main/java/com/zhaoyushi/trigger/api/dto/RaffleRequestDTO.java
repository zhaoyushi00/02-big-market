package com.zhaoyushi.trigger.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhaoy
 * @date 2026/10/14
 * @description 抽奖请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RaffleRequestDTO {
    private Long strategyId;
}
