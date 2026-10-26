package com.zhaoyushi.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖活动单 持久化对象
 * 一个活动 → 多个订单（1:N）。记录「用户某次抽奖」的凭证
 * @create 2024-03-02 13:21  活动下单记录
 */
@Data
public class RaffleActivityOrder {

    /**
     * 自增ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 商品SKU：标识“领了哪个商品”，语义为 activity + activity count 的组合
     */
    private Long sku;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 抽奖策略ID
     */
    private Long strategyId;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 下单时间
     */
    private Date orderTime;

    /**
     * 订单状态（not_used、used、expire）
     */
    private String state;

    /**
     * 总次数
     */
    private Integer totalCount;

    /**
     * 日次数
     */
    private Integer dayCount;

    /**
     * 月次数
     */
    private Integer monthCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 业务防重ID - 外部透传，确保幂等
     */
    private String outBusinessNo;


}
