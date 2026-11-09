package com.zhaoyushi.domain.strategy.service.rule.tree.impl;

// 引入规则校验结果枚举（ALLOW 放行 / TAKE_OVER 拦截），用于封装节点判定结果
import com.zhaoyushi.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
// 引入策略仓储接口，用于查询用户今日抽奖次数
import com.zhaoyushi.domain.strategy.repository.IStrategyRepository;
// 引入决策树节点接口，本类作为「次数锁」节点实现
import com.zhaoyushi.domain.strategy.service.rule.tree.ILogicTreeNode;
// 引入决策树工厂的节点结果对象（TreeActionEntity）
import com.zhaoyushi.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
// 引入 @Resource 注解，用于按名称注入仓储依赖
import jakarta.annotation.Resource;
// 引入 Lombok 的 @Slf4j 注解，编译期自动注入 log 日志对象
import lombok.extern.slf4j.Slf4j;
// 引入 @Component 注解，将本节点注册为 Spring Bean
import org.springframework.stereotype.Component;

/**
 * @author zhaoy
 * @date 2026/9/30
 * @description 次数锁节点
 * <p>决策树中的「次数锁」规则节点，用于限定用户完成 N 次抽奖后才解锁后续流程。
 * 通过 {@code @Component("rule_lock")} 注册，Bean 名与节点 ruleKey 保持一致。
 *
 * <p>判定逻辑：从数据库查询用户今日已参与抽奖次数，与规则配置的限定次数比较：
 * <pre>
 * 1. 用户已抽奖次数 >= 规则限定次数 → 放行（ALLOW），可继续走向下一节点；
 * 2. 用户已抽奖次数 <  规则限定次数 → 拦截（TAKE_OVER），无法解锁该奖品。
 * </pre>
 */
// 启用 Lombok 日志，自动注入 log 对象
@Slf4j
// 注册为 Spring Bean，Bean 名与决策树节点的 ruleKey 保持一致
@Component("rule_lock")
public class RuleLockLogicTreeNode implements ILogicTreeNode {

    // 注入策略仓储，用于查询用户今日抽奖次数（原硬编码占位字段已移除）
    @Resource
    private IStrategyRepository repository;
    /**
     * 执行次数锁规则判断
     * <p>解析规则配置的限定抽奖次数，与用户今日已抽奖次数比较，返回放行或拦截。
     *
     * @param userId     用户ID
     * @param strategyId 策略ID
     * @param awardId    候选奖品ID
     * @param ruleValue  规则配置值（限定抽奖次数）
     * @return 放行（ALLOW）或拦截（TAKE_OVER）的节点判定结果
     */
    @Override
    public DefaultTreeFactory.TreeActionEntity logic(String userId, Long strategyId, Integer awardId, String ruleValue) {

        // 记录次数锁规则开始执行的日志
        log.info("规则过滤-次数锁 userId:{} strategyId:{} awardId:{}",userId, strategyId, awardId);

        // 初始化规则限定抽奖次数
        long raffleCount = 0L;
        try {
            // 将规则配置值解析为 long 类型的限定抽奖次数
            raffleCount = Long.parseLong(ruleValue);
        } catch (Exception e) {
            // 配置值非数字时抛出异常，提示配置不正确
            throw new RuntimeException("规则过滤-次数锁异常 ruleValue: " + ruleValue + " 配置不正确");
        }

        // 查询用户抽奖次数 - 当天的；策略ID:活动ID 1:1 的配置，可以直接用 strategyId 查询。
        Integer userRaffleCount = repository.queryTodayUserRaffleCount(userId, strategyId);

        // 用户抽奖次数大于等于规则限定值，规则放行
        if (userRaffleCount >= raffleCount) {
            // 记录次数锁放行的日志（含规则限定次数与用户已抽次数）
            log.info("规则过滤-次数锁【放行】 userId:{} strategyId:{} awardId:{} raffleCount:{} userRaffleCount:{}", userId, strategyId, awardId, userRaffleCount, userRaffleCount);
            // 构建放行判定结果并返回
            return DefaultTreeFactory.TreeActionEntity.builder()
                    // 设置节点判定结果为 ALLOW（放行）
                    .ruleLogicCheckTypeVO(RuleLogicCheckTypeVO.ALLOW)
                    // 构建节点结果对象
                    .build();
        }

        // 记录次数锁拦截的日志（含规则限定次数与用户已抽次数）
        log.info("规则过滤-次数锁【拦截】 userId:{} strategyId:{} awardId:{} raffleCount:{} userRaffleCount:{}", userId, strategyId, awardId, userRaffleCount, userRaffleCount);

        // 用户抽奖次数小于规则限定值，规则拦截
        return DefaultTreeFactory.TreeActionEntity.builder()
                // 设置节点判定结果为 TAKE_OVER（拦截）
                .ruleLogicCheckTypeVO(RuleLogicCheckTypeVO.TAKE_OVER)
                // 构建节点结果对象
                .build();

    }

}
