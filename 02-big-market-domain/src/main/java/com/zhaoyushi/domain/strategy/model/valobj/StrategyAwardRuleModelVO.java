package com.zhaoyushi.domain.strategy.model.valobj;

import com.zhaoyushi.domain.strategy.service.rule.factory.DefaultLogicFactory;
import com.zhaoyushi.types.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// 抽奖策略规则规则值对象;值对象，没有唯一ID，仅限于从数据库查询对象

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyAwardRuleModelVO {

    private String ruleModels;

    public String[] raffleCenterRuleModelList(){
        List<String> ruleModelList = new ArrayList<>();
        if (null == ruleModels || ruleModels.isEmpty()) {
            return ruleModelList.toArray(new String[0]);
        }
        String[] ruleModelValues = ruleModels.split(Constants.SPLIT);
        for (String ruleModelValue : ruleModelValues){
            if(DefaultLogicFactory.LogicModel.idCenter(ruleModelValue)){
                ruleModelList.add(ruleModelValue);
            }
        }
        //list.toArray(T[] a) 的规则是：
        //传入的数组够长 → 直接往里面填；
        //不够长 → JVM 自己 new 一个长度正好的数组返回。
        //传 new String[0]（长度 0，肯定不够长）
        //等于告诉 JVM："你自己按 list 的实际大小分配一个刚好合适的数组"。
        return ruleModelList.toArray(new String[0]);
    }

}
