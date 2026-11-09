package com.zhaoyushi.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 引入 SimpleDateFormat，用于把日期格式化为 yyyy-MM-dd 字符串
import java.text.SimpleDateFormat;
// 引入 Date，作为时间字段类型及格式化入参
import java.util.Date;

/**
 * @author zhaoy
 * @description 抽奖活动账户表-日次数
 * @create 2024-04-03 15:28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaffleActivityAccountDay {

    // 日期格式化器，用于生成 yyyy-MM-dd 格式的当前日期字符串
    private final SimpleDateFormat dateFormatDay = new SimpleDateFormat("yyyy-MM-dd");

    /** 自增ID */
    private String id;
    /** 用户ID */
    private String userId;
    /** 活动ID */
    private Long activityId;
    /** 日期（yyyy-mm-dd） */
    private String day;
    /** 日次数 */
    private Integer dayCount;
    /** 日次数-剩余 */
    private Integer dayCountSurplus;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;

    /**
     * 获取当前日期字符串
     * <p>用于组装查询当日账户记录时的 day 参数（yyyy-MM-dd）。
     *
     * @return 当前日期字符串，格式 yyyy-MM-dd
     */
    public String currentDay() {
        // 将当前时间格式化为 yyyy-MM-dd 并返回
        return dateFormatDay.format(new Date());
    }
}
