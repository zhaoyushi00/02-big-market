package com.zhaoyushi.trigger.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhaoy
 * @date 2026/11/17
 * @description 用户活动账户请求对象
 */
@Data
public class UserActivityAccountRequestDTO implements Serializable {
    /**
     * 用户ID
     */
    private String userId;

    /**
     * 活动ID
     */
    private Long activityId;
}
