package com.zhaoyushi.domain.strategy.service.annoation;

import com.zhaoyushi.domain.strategy.service.rule.factory.DefaultLogicFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 策略自定义枚举
 * @create 2023-12-31 11:29
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogicStrategy {

    /**
     * 声明该过滤器对应的规则类型（逻辑模型）
     * <p>工厂根据此值将过滤器登记到对应的规则标识下。
     *
     * @return 规则类型枚举
     */
    DefaultLogicFactory.LogicModel logicMode();

}
