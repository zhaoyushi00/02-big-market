package com.zhaoyushi.test.infrastructure;

import com.zhaoyushi.infrastructure.persistent.dao.IAwardDao;
import com.zhaoyushi.infrastructure.persistent.dao.IStrategyAwardDao;
import com.zhaoyushi.infrastructure.persistent.dao.IStrategyDao;
import com.zhaoyushi.infrastructure.persistent.dao.IStrategyRuleDao;
import com.zhaoyushi.infrastructure.persistent.po.Award;
import com.zhaoyushi.infrastructure.persistent.po.Strategy;
import com.zhaoyushi.infrastructure.persistent.po.StrategyAward;
import com.zhaoyushi.infrastructure.persistent.po.StrategyRule;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AwardDaoTest {

    @Resource
    private IAwardDao awardDao;
    @Resource
    private IStrategyAwardDao strategyAwardDao;
    @Resource
    private IStrategyDao strategyDao;
    @Resource
    private IStrategyRuleDao strategyRuleDao;

    @Test
    public void test_queryAwardList(){
        for (Award award : awardDao.queryAwardList()) {
            System.out.println(award);
        }

    }

    @Test
    public void test_queryStrategyAwardList(){
        for (StrategyAward strategyAward : strategyAwardDao.queryStrategyAwardList()) {
            System.out.println(strategyAward);
        }

    }

    @Test
    public void test_queryStrategyList(){
        for (Strategy strategy : strategyDao.queryStrategyList()) {
            System.out.println(strategy);
        }

    }

    @Test
    public void test_queryStrategyRuleList(){
        for (StrategyRule strategyRule : strategyRuleDao.queryStrategyRuleList()) {
            System.out.println(strategyRule);
        }

    }

}
