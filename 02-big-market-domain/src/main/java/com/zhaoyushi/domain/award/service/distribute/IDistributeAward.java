// 声明当前类所属的包路径：领域层 - 奖品模块 - 发奖策略
package com.zhaoyushi.domain.award.service.distribute;

// 引入分发奖品实体，作为发奖策略方法的统一入参类型
import com.zhaoyushi.domain.award.model.entity.DistributeAwardEntity;

/**
 * 分发奖品的策略接口（领域层 - 奖品模块）
 *
 * <p>策略模式抽象：不同的奖品类型（积分、实物、优惠券等）各自实现本接口，
 * 通过 Spring 以 Bean 名称（对应 award_key）注册到容器，
 * 由发奖服务按奖品 key 动态路由到对应实现，实现发奖逻辑的扩展与解耦。
 *
 * @author zhaoy
 * @date 2026/11/22
 * @description 分发奖品的接口
 */
public interface IDistributeAward {

    /**
     * 发放奖品
     *
     * @param distributeAwardEntity 分发奖品实体，承载用户、订单、奖品及配置信息
     */
    void giveOutPrizes(DistributeAwardEntity distributeAwardEntity);

}
