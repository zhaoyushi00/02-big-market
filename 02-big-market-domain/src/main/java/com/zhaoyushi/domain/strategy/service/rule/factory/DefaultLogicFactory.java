package com.zhaoyushi.domain.strategy.service.rule.factory;


import com.zhaoyushi.domain.strategy.model.entity.RuleActionEntity;
import com.zhaoyushi.domain.strategy.service.annoation.LogicStrategy;
import com.zhaoyushi.domain.strategy.service.rule.ILogicFilter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 规则工厂
 * @create 2023-12-31 11:23
 */
@Service
public class DefaultLogicFactory {

    //String：键，存"工种名"，比如 "rule_blacklist"（黑名单）
    //ILogicFilter<?>：值，存"员工"，也就是具体的规则过滤器
    public Map<String, ILogicFilter<?>> logicFilterMap = new ConcurrentHashMap<>();

    /**
     * 构造规则工厂
     * <p>Spring 会自动收集所有 ILogicFilter 实现 Bean，本构造器根据 @LogicStrategy 注解将其按规则标识登记到 Map。
     *
     * @param logicFilters 所有规则过滤器实现（由 Spring 自动注入）
     */
    public DefaultLogicFactory(List<ILogicFilter<?>> logicFilters) {
        logicFilters.forEach(logic -> {
            LogicStrategy strategy = AnnotationUtils.findAnnotation(logic.getClass(), LogicStrategy.class);
            if (null != strategy) {
                logicFilterMap.put(strategy.logicMode().getCode(), logic);
            }
        });
    }

    /**
     * 打开规则过滤器集合
     * <p>将装配好的过滤器 Map 返回给调用方，按规则标识取用对应过滤器。
     *
     * @param <T> 过滤器产出的抽奖阶段实体类型
     * @return 规则标识 → 过滤器 的映射
     */
    public <T extends RuleActionEntity.RaffleEntity> Map<String, ILogicFilter<T>> openLogicFilter() {
        return (Map<String, ILogicFilter<T>>) (Map<?, ?>) logicFilterMap;
    }

    @Getter
    @AllArgsConstructor
    public enum LogicModel {

        RULE_WIGHT("rule_weight","【抽奖前规则】根据抽奖权重返回可抽奖范围KEY"),
        RULE_BLACKLIST("rule_blacklist","【抽奖前规则】黑名单规则过滤，命中黑名单则直接返回"),

        ;

        private final String code;
        private final String info;

    }

}
