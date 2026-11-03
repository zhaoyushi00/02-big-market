package com.zhaoyushi.domain.activity.model.aggregate;

import com.zhaoyushi.domain.activity.model.entity.ActivityAccountDayEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivityAccountEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivityAccountMonthEntity;
import com.zhaoyushi.domain.activity.model.entity.UserRaffleOrderEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 参与活动订单聚合对象（聚合根）
 *
 * <p>DDD 聚合：将「用户参与活动、扣减账户额度、产生抽奖单」这一用例所涉及的数据打包在一起，
 * 由仓储 {@code saveCreatePartakeOrderAggregate} 在同一个事务中一次性落库，保证一致性。
 *
 * <p>聚合内部包含四类数据：
 * 1. userId + activityId：参与主体与活动（分片路由键）；
 * 2. activityAccountEntity：总账户（raffle_activity_account），存总/日/月额度；
 * 3. activityAccountMonthEntity + isExistAccountMonth：月账户额度及是否已存在；
 * 4. activityAccountDayEntity + isExistAccountDay：日账户额度及是否已存在；
 * 5. userRaffleOrderEntity：本次参与产生的抽奖单凭证。
 *
 * @author zhaoy
 * @date 2026/11/2
 * @description 参与活动订单聚合对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePartakeOrderAggregate {
    /**
     * 用户ID：参与抽奖的主体，也是分库分表路由键
     */
    private String userId;

    /**
     * 活动ID：参与的活动编号
     */
    private Long activityId;

    /**
     * 账户总额度：用户在该活动上的总账户（总/日/月剩余次数）
     */
    private ActivityAccountEntity activityAccountEntity;

    /**
     * 是否存在月账户：true=月账户已存在（走更新），false=不存在（走插入）
     */
    private boolean isExistAccountMonth = true;

    /**
     * 账户月额度：当月维度的账户额度记录
     */
    private ActivityAccountMonthEntity activityAccountMonthEntity;

    /**
     * 是否存在日账户：true=日账户已存在（走更新），false=不存在（走插入）
     */
    private boolean isExistAccountDay = true;

    /**
     * 账户日额度：当日维度的账户额度记录
     */
    private ActivityAccountDayEntity activityAccountDayEntity;

    /**
     * 抽奖单实体：本次参与生成的抽奖凭证（订单号 + 状态）
     */
    private UserRaffleOrderEntity userRaffleOrderEntity;

}
