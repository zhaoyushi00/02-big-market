package com.zhaoyushi.infrastructure.persistent.po;

// 抽奖策略

import lombok.Data;

import java.util.Date;

@Data
public class Strategy {

      /*自增ID*/
      private Long  id;
      /*抽奖策略ID*/
      private Long  strategyId;
      /*抽奖策略描述*/
      private String  strategyDesc;
      /*抽奖规则模型*/
      private String ruleModel;
      /*策略模型*/
      private Date createTime;
      /*创建时间*/
      private Date updateTime;

}
