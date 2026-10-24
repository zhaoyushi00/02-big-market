package com.zhaoyushi.domain.activity.model.aggregate;

import com.zhaoyushi.domain.activity.model.entity.ActivityAccountEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivityOrderEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下单聚合对象（聚合根）
 *
 * <p>DDD 聚合：将“创建活动订单”这一用例所涉及的两个实体聚合在一起，保证事务一致性：
 * 1. activityAccountEntity：活动账户实体，下单后需要初始化/累加用户次数；
 * 2. activityOrderEntity：活动订单实体，下单后生成订单凭证。
 *
 * <p>业务位置：下单服务 {@link com.zhaoyushi.domain.activity.service.IRaffleOrder#createRaffleActivityOrder}
 * 内部构建本聚合，由仓储一次性持久化账户与订单，二者同生共死、同事务提交。
 *
 * <p>关键点：账户是“内部状态”，订单是“对外凭证”，二者打包成聚合统一落库。
 *
 * @author zhaoy
 * @date 2026/10/24
 * @description 下单聚合对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderAggregate {

    /**
     * 活动账户实体：用户在某活动下的次数账户（总额度 + 剩余额度）
     */
    private ActivityAccountEntity activityAccountEntity;

    /**
     * 活动订单实体：用户某次参与的下单凭证（订单号 + 次数快照）
     */
    private ActivityOrderEntity activityOrderEntity;

}
