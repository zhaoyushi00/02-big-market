// 声明当前类所属的包路径：领域层 - 奖品模块 - 发奖策略 - 实现
package com.zhaoyushi.domain.award.service.distribute.impl;

import com.zhaoyushi.domain.award.model.aggregate.GiveOutPrizesAggregate;
import com.zhaoyushi.domain.award.model.entity.DistributeAwardEntity;
import com.zhaoyushi.domain.award.model.entity.UserAwardRecordEntity;
import com.zhaoyushi.domain.award.model.entity.UserCreditAwardEntity;
import com.zhaoyushi.domain.award.model.valobj.AwardStateVO;
import com.zhaoyushi.domain.award.repository.IAwardRepository;
import com.zhaoyushi.domain.award.service.distribute.IDistributeAward;
import com.zhaoyushi.types.common.Constants;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * 用户积分奖品发奖实现（领域层 - 奖品模块）
 *
 * <p>策略模式的具体策略：负责「积分」类型奖品的发放。
 * 支持 award_config（如 0.01,1 表示随机积分范围）透传，满足黑名单积分奖励。
 * Bean 名称为「user_credit_random」，与数据库 award 表的 award_key 字段对应，
 * 由发奖服务据此路由到本实现。
 *
 * @author zhaoy
 * @date 2026/11/22
 * @description 用户积分奖品，支持 award_config 透传，满足黑名单积分奖励。
 */
// 注册为 Spring 组件 Bean，Bean 名称为 user_credit_random（对应奖品 key）
@Component("user_credit_random")
public class UserCreditRandomAward implements IDistributeAward {

    // 注入奖品仓储接口，用于查询奖品配置与保存发奖聚合
    @Resource
    private IAwardRepository repository;

    /**
     * 发放积分奖品
     *
     * <p>流程：解析奖品配置得到积分范围 → 生成随机积分值 → 构建发奖聚合 →
     * 委托仓储在同一事务内写入积分账户与发奖记录。
     *
     * @param distributeAwardEntity 分发奖品实体，承载用户、订单、奖品及配置信息
     */
    @Override
    public void giveOutPrizes(DistributeAwardEntity distributeAwardEntity) {

        // 获取要发放的奖品ID
        Integer awardId = distributeAwardEntity.getAwardId();
        // 获取透传的奖品配置信息（积分范围字符串）
        String awardConfig = distributeAwardEntity.getAwardConfig();

        // 判断透传的奖品配置是否为空
        if (StringUtils.isBlank(awardConfig)){
            // 配置为空时，回查数据库获取奖品配置（例如 0.01,1 或 1,100）
            awardConfig = repository.queryAwardConfig(awardId);
        }

        // 使用逗号将配置拆分为积分范围的最小值与最大值
        String[] creditRange = awardConfig.split(Constants.SPLIT);
        // 校验拆分结果必须恰好为两段（最小值和最大值）
        if (creditRange.length != 2){
            // 配置格式非法时抛出运行时异常，提示正确格式
            throw new RuntimeException("award_config 「" + awardConfig + "」配置不是一个范围值，如 1,100");
        }

        // 根据积分范围生成随机积分值（[最小值, 最大值)）
        BigDecimal creditAmount = generateRandom(new BigDecimal(creditRange[0]), new BigDecimal(creditRange[1]));

        // 构建发奖记录实体（状态为发奖完成 complete）
        UserAwardRecordEntity userAwardRecordEntity = GiveOutPrizesAggregate.buildDistributeUserAwardRecordEntity(
                // 设置发奖记录的用户ID
                distributeAwardEntity.getUserId(),
                // 设置发奖记录关联的订单ID
                distributeAwardEntity.getOrderId(),
                // 设置发奖记录对应的奖品ID
                distributeAwardEntity.getAwardId(),
                // 设置发奖状态为完成
                AwardStateVO.complete
        );
        // 构建用户积分奖品实体（携带生成的随机积分值）
        UserCreditAwardEntity userCreditAwardEntity = GiveOutPrizesAggregate.buildUserCreditAwardEntity(distributeAwardEntity.getUserId(), creditAmount);

        // 创建发放奖品聚合对象，用于聚合发奖记录与积分奖品
        GiveOutPrizesAggregate giveOutPrizesAggregate = new GiveOutPrizesAggregate();
        // 设置聚合对象所属的用户ID
        giveOutPrizesAggregate.setUserId(distributeAwardEntity.getUserId());
        // 设置聚合对象中的发奖记录实体
        giveOutPrizesAggregate.setUserAwardRecordEntity(userAwardRecordEntity);
        // 设置聚合对象中的积分奖品实体
        giveOutPrizesAggregate.setUserCreditAwardEntity(userCreditAwardEntity);

        // 存储发奖聚合对象（同一事务内写入积分账户与发奖记录）
        repository.saveGiveOutPrizesAggregate(giveOutPrizesAggregate);

    }

    /**
     * 生成指定范围内的随机积分值
     *
     * <p>当最小值等于最大值时直接返回该值；否则在 [min, max) 区间内
     * 生成随机值，并按 3 位有效数字舍入。
     *
     * @param min 积分范围最小值
     * @param max 积分范围最大值
     * @return 生成的随机积分值
     */
    private BigDecimal generateRandom(BigDecimal min, BigDecimal max) {

        // 最小值等于最大值时，直接返回该固定值
        if(min.equals(max)) return min;

        // 计算随机积分：min + 随机小数 * (max - min)，结果落在 [min, max) 区间
        BigDecimal randomBigDecimal = min.add(BigDecimal.valueOf(Math.random()).multiply(max.subtract(min)));

        // 按 3 位有效数字舍入后返回
        return randomBigDecimal.round(new MathContext(3));
    }
}
