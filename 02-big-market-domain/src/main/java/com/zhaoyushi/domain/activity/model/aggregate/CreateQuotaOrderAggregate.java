package com.zhaoyushi.domain.activity.model.aggregate;

import com.zhaoyushi.domain.activity.model.entity.ActivityOrderEntity;
import com.zhaoyushi.domain.activity.service.IRaffleActivityAccountQuotaService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下单聚合对象（聚合根）
 *
 * <p>DDD 聚合：将“创建活动订单”这一用例所涉及的数据打包在一起，保证事务一致性：
 * 1. userId + activityId + totalCount/dayCount/monthCount：账户维度信息，下单后用于初始化/累加用户抽奖次数；
 * 2. activityOrderEntity：活动订单实体，下单后生成的订单凭证（对外快照）。
 *
 * <p>业务位置：下单服务 {@link IRaffleActivityAccountQuotaService#createSkuRechargeOrder}
 * 内部构建本聚合，由仓储一次性持久化账户与订单，二者同生共死、同事务提交。
 *
 * <p>关键点：账户次数是“内部状态”，订单是“对外凭证”，二者打包成聚合统一落库。
 *
 * @author zhaoy
 * @date 2026/10/24
 * @description 下单聚合对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateQuotaOrderAggregate {

    /**
     * 用户ID：下单主体（谁在领取活动、充值次数）
     */
    private String userId;

    /**
     * 活动ID：充值次数所归属的活动
     */
    private Long activityId;

    /**
     * 增加的总次数：本笔订单为账户增加的总抽奖次数
     */
    private Integer totalCount;

    /**
     * 增加的日次数：本笔订单为账户增加的当日抽奖次数
     */
    private Integer dayCount;

    /**
     * 增加的月次数：本笔订单为账户增加的当月抽奖次数
     */
    private Integer monthCount;


    /**
     * 活动订单实体：用户某次参与的下单凭证（订单号 + 次数快照）
     */
    private ActivityOrderEntity activityOrderEntity;

}
