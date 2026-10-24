package com.zhaoyushi.domain.activity.repository;

import com.zhaoyushi.domain.activity.model.entity.ActivityCountEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivityEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivitySkuEntity;

/**
 * 活动仓储接口（领域层端口）
 *
 * <p>定义领域层需要的活动相关查询能力，由基础设施层 {@link com.zhaoyushi.infrastructure.persistent.repository.ActivityRepository} 实现。
 * 领域层依赖本接口（依赖倒置），不感知底层数据库/缓存的实现细节。
 *
 * <p>关键点：三个查询方法分别对应下单链路中依次需要的信息——SKU → 活动 → 次数配置。
 *
 * @author zhaoy
 * @date 2026/10/24
 * @description 活动仓储接口
 */
public interface IActivityRepository {

    /**
     * 查询活动 SKU
     *
     * @param sku 商品 SKU 编号（活动 + 次数配置组合的唯一标识）
     * @return 活动 SKU 实体，含活动ID、次数配置ID、库存信息
     */
    ActivitySkuEntity queryActivitySku(Long sku);

    /**
     * 根据活动ID查询活动
     *
     * @param activityId 活动ID
     * @return 活动实体，含活动名称、时间范围、策略、状态
     */
    ActivityEntity queryRaffleActivityByActivityId(Long activityId);

    /**
     * 根据次数配置编号查询次数配置
     *
     * @param activityCountId 活动次数配置编号
     * @return 次数配置实体，含总/日/月次数上限
     */
    ActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId);

}
