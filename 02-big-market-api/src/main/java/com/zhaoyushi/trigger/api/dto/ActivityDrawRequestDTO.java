package com.zhaoyushi.trigger.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 活动抽奖请求对象
 * <p>作为活动抽奖接口（draw）的入参 DTO，仅承载用户与活动两个维度的标识信息。
 *
 * @author zhaoy
 * @date 2026/11/7
 * @description 活动抽奖请求对象
 */
// 自动生成该类的 getter/setter 等样板方法
@Data
// 实现 Serializable：作为 Dubbo RPC 接口的入参，需支持 JDK 序列化以在网络间传输
public class ActivityDrawRequestDTO implements Serializable {
    /**
     * 用户ID，标识参与抽奖的用户
     */
    private String userId;

    /**
     * 活动ID，标识用户参与哪一场抽奖活动
     */
    private Long activityId;
}
