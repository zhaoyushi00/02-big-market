package com.zhaoyushi.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 活动次数实体对象
 *
 * <p>对应数据库表 raffle_activity_count（抽奖活动次数配置表），描述“一份参与次数配置模板”：
 * 限定参与该活动的每个用户在总量、单日、单月三个维度上可消耗的次数上限。
 *
 * <p>业务位置：下单服务通过 {@link com.zhaoyushi.domain.activity.repository.IActivityRepository#queryRaffleActivityCountByActivityCountId}
 * 查询本实体，用于后续给用户的活动账户（ActivityAccountEntity）做次数初始化/累加。
 *
 * <p>关键点：
 * 1. 本实体是“模板/规则”，不包含用户维度，一份配置可被多个 SKU/活动复用；
 * 2. totalCount/dayCount/monthCount 分别代表“总共/每天/每月”可参与次数上限。
 *
 * @author zhaoy
 * @date 2026/10/24
 * @description 活动次数实体对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityCountEntity {

    /**
     * 活动次数编号：唯一标识一份次数配置
     */
    private Long activityCountId;

    /**
     * 总次数：整个活动周期内，单个用户可参与的总次数上限
     */
    private Integer totalCount;

    /**
     * 日次数：单日内单个用户可参与的次数上限
     */
    private Integer dayCount;

    /**
     * 月次数：单月内单个用户可参与的次数上限
     */
    private Integer monthCount;

}
