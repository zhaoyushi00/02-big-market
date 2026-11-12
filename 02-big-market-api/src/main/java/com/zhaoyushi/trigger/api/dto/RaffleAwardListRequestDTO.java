package com.zhaoyushi.trigger.api.dto;

import lombok.Data;

/**
 * @author zhaoy
 * @date 2026/10/14
 * @description 抽奖奖品列表，请求对象
 */

@Data
public class RaffleAwardListRequestDTO {
    // 废弃字段：早期按「策略ID」查询奖品列表的入参，现改为按「活动ID」查询，保留仅用于兼容旧调用，编译器会有黄色删除线标记
    @Deprecated
    private Long strategyId;
    // 活动ID：当前查询奖品列表的主键入参，活动与策略为 1:1 绑定，可反查出对应策略
    private Long activityId;
    // 用户ID：用于计算该用户今日已抽奖次数，进而判断各奖品的解锁状态
    private String userId;
}
