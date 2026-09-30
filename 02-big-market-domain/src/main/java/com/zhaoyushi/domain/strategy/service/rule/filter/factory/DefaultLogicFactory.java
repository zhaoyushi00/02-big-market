package com.zhaoyushi.domain.strategy.service.rule.filter.factory;


import com.zhaoyushi.domain.strategy.model.entity.RuleActionEntity;
import com.zhaoyushi.domain.strategy.service.annoation.LogicStrategy;
import com.zhaoyushi.domain.strategy.service.rule.filter.ILogicFilter;
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

        RULE_WIGHT("rule_weight","【抽奖前规则】根据抽奖权重返回可抽奖范围KEY", "before"),
        RULE_LOCK("rule_lock","【抽奖中规则】抽奖n次后，对应奖品可解锁抽奖", "center"),
        RULE_LOCK_AWARD("rule_lock_award","【抽奖后规则】幸运奖兜底", "after"),
        RULE_BLACKLIST("rule_blacklist","【抽奖前规则】黑名单规则过滤，命中黑名单则直接返回", "before"),

        ;

        private final String code;
        private final String info;
        private final String type;

        public static boolean idCenter(String code){
            return matchType(code, "center");
        }

        public static boolean idAfter(String code){
            return matchType(code, "after");
        }

        /**
         * 按规则的 code 字段匹配其阶段类型（before/center/after）
         * <p>与按枚举常量名匹配的 valueOf 不同，这里遍历枚举常量并比较 code，未知 code 返回 false 而非抛异常。
         *
         * @param code 规则标识，如 rule_lock、rule_weight
         * @param type 期望的阶段类型
         * @return 是否命中该阶段
         */
        private static boolean matchType(String code, String type){
            if (null == code) return false;
            for (LogicModel logicModel : LogicModel.values()) {
                if (logicModel.code.equals(code)) {
                    return type.equals(logicModel.type);
                }
            }
            return false;
        }

    }

}
