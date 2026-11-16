package com.zhaoyushi.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author zhaoy
 * @description 抽奖活动账户表-月次数
 * @create 2024-04-03 15:28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaffleActivityAccountMonth {

    private final SimpleDateFormat dateFormatMonth = new SimpleDateFormat("yyyy-MM");

    /** 自增ID */
    private String id;
    /** 用户ID */
    private String userId;
    /** 活动ID */
    private Long activityId;
    /** 月（yyyy-mm） */
    private String month;
    /** 月次数 */
    private Integer monthCount;
    /** 月次数-剩余 */
    private Integer monthCountSurplus;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;

    public String currentMonth() {
        // 将当前时间格式化为 yyyy-MM-dd 并返回
        return dateFormatMonth.format(new Date());
    }

}
