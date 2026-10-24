package com.zhaoyushi.infrastructure.persistent.repository;

import com.zhaoyushi.domain.activity.model.entity.ActivityCountEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivityEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivitySkuEntity;
import com.zhaoyushi.domain.activity.model.valobj.ActivityStateVO;
import com.zhaoyushi.domain.activity.repository.IActivityRepository;
import com.zhaoyushi.infrastructure.persistent.dao.IRaffleActivityCountDao;
import com.zhaoyushi.infrastructure.persistent.dao.IRaffleActivityDao;
import com.zhaoyushi.infrastructure.persistent.dao.IRaffleActivitySkuDao;
import com.zhaoyushi.infrastructure.persistent.po.RaffleActivity;
import com.zhaoyushi.infrastructure.persistent.po.RaffleActivityCount;
import com.zhaoyushi.infrastructure.persistent.po.RaffleActivitySku;
import com.zhaoyushi.infrastructure.persistent.redis.IRedisService;
import com.zhaoyushi.types.common.Constants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

/**
 * 活动仓储服务实现（基础设施层）
 *
 * <p>实现领域层定义的 {@link IActivityRepository} 端口，负责从数据库/缓存读取活动相关数据，
 * 并将持久化对象（PO）转换为领域实体返回。
 *
 * <p>关键点：
 * 1. 活动与次数配置查询采用“缓存优先”：先读 Redis，未命中再查库并回填缓存；
 * 2. SKU 查询直接走数据库，未做缓存。
 *
 * @author zhaoy
 * @date 2026/10/24
 * @description 活动仓储服务实现
 */
@Repository
public class ActivityRepository implements IActivityRepository {

    /** Redis 服务：用于活动/次数配置的缓存读写 */
    @Resource
    private IRedisService redisService;
    /** 活动 DAO：操作 raffle_activity 表 */
    @Resource
    private IRaffleActivityDao raffleActivityDao;
    /** 活动 SKU DAO：操作 raffle_activity_sku 表 */
    @Resource
    private IRaffleActivitySkuDao raffleActivitySkuDao;
    /** 活动次数配置 DAO：操作 raffle_activity_count 表 */
    @Resource
    private IRaffleActivityCountDao raffleActivityCountDao;

    @Override
    public ActivitySkuEntity queryActivitySku(Long sku) {
        // 通过 DAO 从数据库查询 SKU 持久化对象
        RaffleActivitySku raffleActivitySku = raffleActivitySkuDao.queryActivitySku(sku);
        // 将持久化对象 PO 转换为领域实体并返回
        return ActivitySkuEntity.builder()
                .sku(raffleActivitySku.getSku())
                .activityId(raffleActivitySku.getActivityId())
                .activityCountId(raffleActivitySku.getActivityCountId())
                .stockCount(raffleActivitySku.getStockCount())
                .stockCountSurplus(raffleActivitySku.getStockCountSurplus())
                .build();
    }

    @Override
    public ActivityEntity queryRaffleActivityByActivityId(Long activityId) {
        // 组装缓存 key：活动缓存前缀 + 活动ID
        String cacheKey = Constants.RedisKey.ACTIVITY_KEY + activityId;
        // 优先从缓存获取活动实体
        ActivityEntity activityEntity = redisService.getValue(cacheKey);
        // 缓存命中则直接返回
        if (null != activityEntity) return activityEntity;
        // 缓存未命中，从数据库查询活动持久化对象
        RaffleActivity raffleActivity = raffleActivityDao.queryRaffleActivityByActivityId(activityId);
        // 将持久化对象 PO 转换为领域实体
        activityEntity = ActivityEntity.builder()
                .activityId(raffleActivity.getActivityId())
                .activityName(raffleActivity.getActivityName())
                .activityDesc(raffleActivity.getActivityDesc())
                .beginDateTime(raffleActivity.getBeginDateTime())
                .endDateTime(raffleActivity.getEndDateTime())
                .strategyId(raffleActivity.getStrategyId())
                .state(ActivityStateVO.valueOf(raffleActivity.getState()))
                .build();
        // 回填缓存，后续查询直接命中
        redisService.setValue(cacheKey, activityEntity);
        return activityEntity;
    }

    @Override
    public ActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId) {
        // 组装缓存 key：次数配置缓存前缀 + 次数配置编号
        String cacheKey = Constants.RedisKey.ACTIVITY_COUNT_KEY + activityCountId;
        // 优先从缓存获取次数配置实体
        ActivityCountEntity activityCountEntity = redisService.getValue(cacheKey);
        // 缓存命中则直接返回
        if (null != activityCountEntity) return activityCountEntity;
        // 缓存未命中，从数据库查询次数配置持久化对象
        RaffleActivityCount raffleActivityCount = raffleActivityCountDao.queryRaffleActivityCountByActivityCountId(activityCountId);
        // 将持久化对象 PO 转换为领域实体
        activityCountEntity = ActivityCountEntity.builder()
                .activityCountId(raffleActivityCount.getActivityCountId())
                .totalCount(raffleActivityCount.getTotalCount())
                .dayCount(raffleActivityCount.getDayCount())
                .monthCount(raffleActivityCount.getMonthCount())
                .build();
        // 回填缓存，后续查询直接命中
        redisService.setValue(cacheKey, activityCountEntity);
        return activityCountEntity;
    }
}
