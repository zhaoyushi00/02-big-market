package com.zhaoyushi.infrastructure.persistent.dao;

import com.zhaoyushi.infrastructure.persistent.po.DailyBehaviorRebate;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 日常行为返利活动配置 DAO（基础设施层）
 *
 * <p>对应数据库表 daily_behavior_rebate，提供返利配置查询能力，
 * SQL 定义见 daily_behavior_rebate_mapper.xml。
 *
 * @author zhaoy
 * @date 2026/11/14
 * @description 日常行为返利活动配置
 */
@Mapper
public interface IDailyBehaviorRebateDao {
    /**
     * 按行为类型查询「开启中」的返利配置
     *
     * @param behaviorType 行为类型编码（sign 签到、openai_pay 支付）
     * @return 返利配置持久化对象列表
     */
    List<DailyBehaviorRebate> queryDailyBehaviorRebateByBehaviorType(String behaviorType);
}
