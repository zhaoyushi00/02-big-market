package com.zhaoyushi.domain.strategy.model.entity;


import com.zhaoyushi.types.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * 策略实体 - 策略的核心配置
 *
 * <p>用途：承载「策略(strategyId)的描述与规则模型集合(ruleModels)」。
 * <p>关键方法：{@link #ruleModels()} 将逗号分隔的规则模型拆成数组；{@link #getRuleWeight()} 取出权重规则。
 * <p>业务位置：抽奖主流程按 strategyId 查询策略，决定需要执行哪些规则过滤。
 *
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @create 2023-12-31 15:24
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyEntity {

    /** 抽奖策略ID */
    private Long strategyId;
    /** 抽奖策略描述 */
    private String strategyDesc;
    /** 抽奖规则模型 rule_weight,rule_blacklist */
    private String ruleModels;

    /**
     * 将逗号分隔的规则模型串拆分成数组
     * <p>例如 "rule_blacklist,rule_weight" → ["rule_blacklist", "rule_weight"]；为空时返回 null。
     *
     * @return 规则模型数组，为空时返回 null
     */
    public String[] ruleModels() {
        if (StringUtils.isBlank(ruleModels)) return null;
        return ruleModels.split(Constants.SPLIT);
    }

    /**
     * 从规则模型集合中取出「权重规则」
     *
     * @return 若配置了 rule_weight 则返回该字符串，否则返回 null
     */
    public String getRuleWeight() {
        String[] ruleModels = this.ruleModels();
        if (null == ruleModels) return null;
        for (String ruleModel : ruleModels) {
            if ("rule_weight".equals(ruleModel)) return ruleModel;
        }
        return null;
    }

}
