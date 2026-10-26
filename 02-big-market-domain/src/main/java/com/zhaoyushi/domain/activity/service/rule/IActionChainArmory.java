package com.zhaoyushi.domain.activity.service.rule;

/**
 * 抽奖动作责任链装配接口
 *
 * <p>定义责任链的装配与遍历能力：appendNext 负责拼接节点，next 负责获取下一节点。
 * 与 {@link IActionChain} 分离，是为了把“链表装配”与“业务校验动作”解耦。
 *
 * @author zhaoy
 * @date 2026/10/25
 * @description 抽奖动作责任链装配
 */
public interface IActionChainArmory {

    /**
     * 追加下一个责任链节点
     *
     * @param next 下一个节点
     * @return 返回新追加的节点，便于链式拼接
     */
    IActionChain appendNext(IActionChain next);

    /**
     * 获取责任链的下一个节点
     *
     * @return 下一个节点，尾节点返回 null
     */
    IActionChain next();

}
