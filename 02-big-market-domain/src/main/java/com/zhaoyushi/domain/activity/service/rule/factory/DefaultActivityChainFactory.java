package com.zhaoyushi.domain.activity.service.rule.factory;

import com.zhaoyushi.domain.activity.service.rule.IActionChain;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 默认活动责任链工厂
 *
 * <p>负责装配并对外提供“活动下单动作”的责任链头节点。
 * 活动下单动作的责任链顺序固定，因此在构造器中直接完成拼接，业务方通过 {@link #openActionChain()} 获取头节点后依次执行。
 *
 * @author zhaoy
 * @date 2026/10/25
 * @description 默认活动责任链工厂
 */
@Service
public class DefaultActivityChainFactory {

    /** 责任链头节点：由构造器拼接完成的链式结构（基础校验 → SKU 库存校验） */
    private final IActionChain actionChain;

    /**
     * 1. 通过构造函数注入。
     * 2. Spring 可以自动注入 IActionChain 接口实现类到 map 对象中，key 就是 bean 的名字。
     * 3. 活动下单动作的责任链是固定的，所以直接在构造函数中组装即可。
     */
    public DefaultActivityChainFactory(Map<String, IActionChain> actionChainGroup) {
        actionChain = actionChainGroup.get(ActionModel.activity_base_action.code);
        actionChain.appendNext(actionChainGroup.get(ActionModel.activity_sku_stock_action.getCode()));
    }

    /**
     * 获取活动下单动作的责任链头节点
     *
     * @return 责任链头节点（基础信息校验节点）
     */
    public IActionChain openActionChain() {
        return this.actionChain;
    }

    /**
     * 活动下单动作责任链节点枚举
     *
     * <p>code 与各责任链实现类的 {@code @Component} 名称保持一致，作为从 Spring 容器按名取 bean 的 key。
     */
    @Getter
    @AllArgsConstructor
    public enum ActionModel {

        /** 基础动作：活动的库存、时间校验 */
        activity_base_action("activity_base_action", "活动的库存、时间校验"),
        /** SKU 库存动作：活动 sku 库存校验 */
        activity_sku_stock_action("activity_sku_stock_action", "活动sku库存"),
        ;

        /** 节点编码（与 Spring bean 名一致） */
        private final String code;
        /** 节点描述 */
        private final String info;

    }

}
