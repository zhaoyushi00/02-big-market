package com.zhaoyushi.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;

/**
 * 抽奖策略 PO（对应数据库 strategy 表）
 * <p>持久化对象，与策略表字段一一对应。
 */
@Data
public class Strategy {

      /** 自增ID */
      private Long  id;
      /** 抽奖策略ID */
      private Long  strategyId;
      /** 抽奖策略描述 */
      private String  strategyDesc;
      /** 抽奖规则模型 */
      private String ruleModel;
      /** 创建时间 */
      private Date createTime;
      /** 更新时间 */
      private Date updateTime;

}
