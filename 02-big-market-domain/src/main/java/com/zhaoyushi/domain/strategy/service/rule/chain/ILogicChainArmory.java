package com.zhaoyushi.domain.strategy.service.rule.chain;

public interface ILogicChainArmory {

    /**
     * 向当前节点后方拼接下一个节点，用于组装责任链
     * <p>返回刚接入的下一个节点，以支持链式调用：nodeA.appendNext(nodeB).appendNext(nodeC)。
     *
     * @param next 要接入的下一个节点
     * @return 刚接入的下一个节点
     */
    ILogicChain appendNext(ILogicChain next);

    /**
     * 获取当前节点后方挂载的下一个节点
     * <p>供 logic() 在自身无法处理时将请求向后传递使用。
     *
     * @return 下一个节点，若为链尾则返回 null
     */
    ILogicChain next();

}
