package com.zhaoyushi.domain.strategy.service.raffle;

import com.zhaoyushi.domain.strategy.model.entity.RaffleFactorEntity;
import com.zhaoyushi.domain.strategy.model.entity.RuleActionEntity;
import com.zhaoyushi.domain.strategy.model.entity.RuleMatterEntity;
import com.zhaoyushi.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.zhaoyushi.domain.strategy.repository.IStrategyRepository;
import com.zhaoyushi.domain.strategy.service.armory.IStrategyDispatch;
import com.zhaoyushi.domain.strategy.service.rule.ILogicFilter;
import com.zhaoyushi.domain.strategy.service.rule.factory.DefaultLogicFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Slf4j
@Service
public class DefaultRaffleStrategy extends AbstractRaffleStrategy{

    @Resource
    private DefaultLogicFactory logicFactory;

    //在创建对象时，接收外部传入的依赖，并转交给父类完成初始化
    //IStrategyRepository repository：策略仓储接口，负责查询策略数据（从数据库/Redis 读取策略、奖品、规则等）。
    //IStrategyDispatch strategyDispatch：策略调度接口，负责执行抽奖的"装配/抽取"逻辑。
    //父类中已经有了 就不用先protected再构造函数了
    public DefaultRaffleStrategy(IStrategyRepository repository, IStrategyDispatch strategyDispatch) {
        super(repository, strategyDispatch);
    }

    /**
     * 抽奖前规则过滤实现
     * <p>黑名单规则优先过滤（命中直接接管返回），其余规则按顺序执行，任一规则非放行即拦截返回。
     *
     * @param raffleFactorEntity 抽奖因子
     * @param logics             需要执行的规则标识数组
     * @return 规则过滤结果
     */
    @Override
    protected RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> doCheckRaffleBeforeLogic(RaffleFactorEntity raffleFactorEntity, String... logics) {
        Map<String, ILogicFilter<RuleActionEntity.RaffleBeforeEntity>> logicFilterGroup = logicFactory.openLogicFilter();

        // 黑名单规则优先过滤
        String ruleBackList = Arrays.stream(logics)
                //逐个检查，保留"包含 rule_blacklist"的元素
                .filter(str -> str.contains(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode()))
                //取第一个匹配到的元素
                .findFirst()
                //一个都没匹配到就返回 null
                .orElse(null);

        if (StringUtils.isNotBlank(ruleBackList)) {
            //从规则工厂返回的 Map 里，按 key "rule_blacklist" 取出黑名单过滤器对象 也就是找到 RuleBackListLogicFilter 对象
            ILogicFilter<RuleActionEntity.RaffleBeforeEntity> logicFilter = logicFilterGroup.get(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode());

            RuleMatterEntity ruleMatterEntity = new RuleMatterEntity();
            ruleMatterEntity.setUserId(raffleFactorEntity.getUserId());
            // 没用 骗编译
            ruleMatterEntity.setAwardId(ruleMatterEntity.getAwardId());

            ruleMatterEntity.setStrategyId(raffleFactorEntity.getStrategyId());
            ruleMatterEntity.setRuleModel(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode());

            // 调用RuleBackListLogicFilter对象，执行黑名单的筛选
            RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> ruleActionEntity = logicFilter.filter(ruleMatterEntity);
            if (!RuleLogicCheckTypeVO.ALLOW.getCode().equals(ruleActionEntity.getCode())) {
                return ruleActionEntity;
            }
        }

        // 顺序过滤剩余规则
        List<String> ruleList = Arrays.stream(logics)
                .filter(s -> !s.equals(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode()))
                .collect(Collectors.toList());

        RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> ruleActionEntity = null;
        for (String ruleModel : ruleList) {
            ILogicFilter<RuleActionEntity.RaffleBeforeEntity> logicFilter = logicFilterGroup.get(ruleModel);
            RuleMatterEntity ruleMatterEntity = new RuleMatterEntity();
            ruleMatterEntity.setUserId(raffleFactorEntity.getUserId());
            ruleMatterEntity.setAwardId(ruleMatterEntity.getAwardId());
            ruleMatterEntity.setStrategyId(raffleFactorEntity.getStrategyId());
            ruleMatterEntity.setRuleModel(ruleModel);
            ruleActionEntity = logicFilter.filter(ruleMatterEntity);
            // 非放行结果则顺序过滤
            log.info("抽奖前规则过滤 userId: {} ruleModel: {} code: {} info: {}", raffleFactorEntity.getUserId(), ruleModel, ruleActionEntity.getCode(), ruleActionEntity.getInfo());
            if (!RuleLogicCheckTypeVO.ALLOW.getCode().equals(ruleActionEntity.getCode())) return ruleActionEntity;
        }

        return ruleActionEntity;
    }
}
