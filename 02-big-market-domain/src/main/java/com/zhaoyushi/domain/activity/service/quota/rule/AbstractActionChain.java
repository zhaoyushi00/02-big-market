package com.zhaoyushi.domain.activity.service.quota.rule;

/**
 * 下单规则责任链抽象类
 *
 * <p>以“单向链表”结构串接多个下单动作校验节点（如基础信息校验、SKU 库存校验）。
 * 每个节点持有指向下一个节点的引用 {@code next}，执行完自身逻辑后通过 {@link #next()} 传递给下一节点。
 *
 * <p>关键点：
 * 1. appendNext 返回的是“新追加的节点”，方便链式串联（chain.appendNext(b).appendNext(c)）；
 * 2. 具体校验逻辑由子类实现 {@link IActionChain#action}。
 *
 * @author zhaoy
 * @date 2026/10/25
 * @description 下单规则责任链抽象类
 */
public abstract class AbstractActionChain implements IActionChain {

    /** 责任链的下一个节点（尾节点为 null） */
    private IActionChain next;

    /**
     * 追加下一个责任链节点
     *
     * @param next 下一个节点
     * @return 返回新追加的节点，便于继续链式拼接
     */
    @Override
    public IActionChain appendNext(IActionChain next) {
        this.next = next;
        return next;
    }

    /**
     * 获取责任链的下一个节点
     *
     * @return 下一个节点，尾节点返回 null
     */
    @Override
    public IActionChain next() {
        return next;
    }

}
