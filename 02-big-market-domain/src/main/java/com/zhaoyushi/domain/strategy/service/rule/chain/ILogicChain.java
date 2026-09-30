package com.zhaoyushi.domain.strategy.service.rule.chain;

/**
 * 抽奖规则责任链接口
 * <p>每个实现类代表责任链中的一个节点，节点内部持有对下一个节点的引用。
 * 请求从链头开始执行，当前节点无法处理时通过 next() 将请求转发给下一个节点，直到某个节点返回结果。
 *
 * 组装示例（appendNext 链式拼接，形成链表结构）：
 *
 * ILogicChain blacklist = new RuleBlackListLogicChain();  节点1：黑名单
 * ILogicChain weight    = new RuleWeightLogicChain();     节点2：权重
 * ILogicChain fallback  = new DefaultLogicChain();        节点3：兜底
 * blacklist.appendNext(weight).appendNext(fallback);
 *  链：blacklist → weight → fallback
 *
 *
 * 执行示例（只调用链头一次，请求像接力棒一样向后传递）：
 *
 * Integer awardId = blacklist.logic("user_10001", 100001L);
 * blacklist 命中则直接返回；否则 next() 转给 weight，依次向后，直到某节点返回结果
 *
 */
public interface ILogicChain extends ILogicChainArmory {

    /**
     * 执行当前节点的抽奖逻辑
     * <p>若当前节点能处理则直接返回结果；否则通过 next() 将请求转发给下一个节点继续处理。
     * 典型实现：命中当前规则直接返回，未命中则 return next().logic(userId, strategy);
     *
     * @param userId   用户ID
     * @param strategyId 策略ID
     * @return 奖品ID
     */
    Integer logic(String userId, Long strategyId);



}
