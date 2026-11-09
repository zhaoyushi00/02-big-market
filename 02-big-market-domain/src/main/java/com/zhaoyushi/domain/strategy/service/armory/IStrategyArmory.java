package com.zhaoyushi.domain.strategy.service.armory;

public interface IStrategyArmory {

    /**
     * 装配抽奖策略配置「触发的时机可以为活动审核通过后进行调用」
     *
     * @param strategyId 策略ID
     * @return 装配结果
     */
    boolean assembleLotteryStrategy(Long strategyId);

    /**
     * 按活动 ID 装配抽奖策略配置
     * <p>内部先根据活动 ID 反查其绑定的策略 ID，再执行策略装配；
     * 触发时机可以为活动审核通过后进行调用，作为活动装配的统一入口。
     *
     * @param activityId 活动ID，用于定位活动绑定的策略
     * @return 装配结果，true 表示装配成功
     */
    boolean assembleLotteryStrategyByActivityId(Long activityId);



}
