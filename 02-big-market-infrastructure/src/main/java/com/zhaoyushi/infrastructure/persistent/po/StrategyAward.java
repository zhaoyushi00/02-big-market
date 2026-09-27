package com.zhaoyushi.infrastructure.persistent.po;

import lombok.Data;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * 策略奖品明细配置 PO（对应数据库 strategy_award 表）
 * <p>持久化对象，与策略奖品明细表字段一一对应。
 */
@Data
public class StrategyAward {

        /*自增ID*/
        private Long id;
        /*抽奖策略ID*/
        private Long strategyId;
        /*抽奖奖品Id*/
        private Integer awardId;
        /*抽奖奖品标题*/
        private String awardTitle;
        /*抽奖奖品副标题*/
        private String awardSubtitle;
        /*奖品库存总量*/
        private Integer awardCount;
        /*奖品库存剩余*/
        private Integer awardCountSurplus;
        /*奖品中奖概率*/
        private BigDecimal awardRate;
        /*规则模型，rule配置规则记录*/
        private String ruleModels;
        /*排序*/
        private Integer sort;
        /*创建时间*/
        private Date createTime;
        /*更新时间*/
        private Date updateTime;

}
