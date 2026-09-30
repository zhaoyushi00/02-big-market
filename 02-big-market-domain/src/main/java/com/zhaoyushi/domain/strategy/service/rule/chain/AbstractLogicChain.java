package com.zhaoyushi.domain.strategy.service.rule.chain;

/**
 * 抽奖规则责任链抽象基类
 * 为 ILogicChain 提供公共骨架：持有下一个节点引用 next，并实现 appendNext() 与 next() 的默认逻辑。
 * 子类只需实现业务方法 logic()，即可成为责任链中的一个节点，无需重复编写链表结构代码。
 *
 * 节点关系示例：nodeA.appendNext(nodeB) 后，nodeA 内部持有 nodeB，形成 A → B 的链表。
 */
public abstract class AbstractLogicChain implements ILogicChain {

    /**
     * 当前节点后方挂载的下一个节点
     * 通过 appendNext() 赋值，通过 next() 返回，构成责任链的单向链表结构。
     */
    private ILogicChain next;

    /**
     * 将下一个节点挂到当前节点后方，并返回刚接入的节点
     * 返回 next 而非 this，是为了支持链式调用：nodeA.appendNext(nodeB).appendNext(nodeC)。
     *
     * @param next 要接入的下一个节点 上面实例的 next
     * @return 刚接入的下一个节点
     */
    @Override
    public ILogicChain appendNext(ILogicChain next) {
        this.next = next;
        return next;
    }

    /**
     * 返回当前节点后方挂载的下一个节点
     * 供 logic() 在自身无法处理时将请求向后传递使用；链尾节点返回 null 表示无后续节点。
     *
     * @return 下一个节点，若为链尾则返回 null
     */
    @Override
    public ILogicChain next() {
        return next;
    }

    protected abstract String ruleModel();

}
