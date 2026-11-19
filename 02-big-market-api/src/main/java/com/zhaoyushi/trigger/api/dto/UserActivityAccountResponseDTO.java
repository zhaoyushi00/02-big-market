package com.zhaoyushi.trigger.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhaoy
 * @date 2026/11/17
 * @description 用户活动账户应答对象
 */
@Data
public class UserActivityAccountResponseDTO implements Serializable {

    /** 总次数 */
    private Integer totalCount;

    /** 总次数-剩余 */
    private Integer totalCountSurplus;

    /** 日次数 */
    private Integer dayCount;

    /** 日次数-剩余 */
    private Integer dayCountSurplus;

    /** 月次数 */
    private Integer monthCount;

    /** 月次数-剩余 */
    private Integer monthCountSurplus;
}
