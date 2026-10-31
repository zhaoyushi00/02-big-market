// 声明当前类所属的包路径：领域层 - 活动模块 - 服务 - 装配
package com.zhaoyushi.domain.activity.service.armory;

// 引入活动 SKU 实体，用于承载数据库查询出的活动商品信息
import com.zhaoyushi.domain.activity.model.entity.ActivitySkuEntity;
// 引入活动仓储接口，提供 SKU/活动/次数配置的查询与缓存能力
import com.zhaoyushi.domain.activity.repository.IActivityRepository;
// 引入全局常量类，获取 Redis Key 前缀
import com.zhaoyushi.types.common.Constants;
// 引入 JSR-250 的 @Resource 注解，用于按名称注入依赖
import jakarta.annotation.Resource;
// 引入 Spring 的 @Service 注解，将本类注册为 Spring Bean
import org.springframework.stereotype.Service;

// 引入 java.util.Date，作为活动结束时间参数类型
import java.util.Date;

/**
 * 活动 SKU 预热（装配）与调度实现
 *
 * <p>同时实现 {@link IActivityArmory}（预热）与 {@link IActivityDispatch}（调度扣减）两个端口，
 * 是活动 SKU 库存一致性的核心类：
 * 1. 预热：将活动 SKU 库存加载到 Redis 原子计数器，并顺带预热活动、活动次数缓存；
 * 2. 调度：扣减 Redis 缓存库存（含防超卖加锁）。
 *
 * @author zhaoy
 * @date 2026/10/27
 * @description 活动sku预热
 */
@Service
public class ActivityArmory implements IActivityArmory, IActivityDispatch{

    // 注入活动仓储接口，用于查询与操作活动 SKU 库存缓存
    @Resource
    private IActivityRepository activityRepository;

    /**
     * 装配（预热）活动 SKU 库存
     * <p>查询活动 SKU 信息，将库存数量缓存到 Redis，并预热关联的活动与活动次数缓存。
     *
     * @param sku 商品 SKU 编号
     * @return 装配结果标识（当前固定返回 false）
     */
    @Override
    public boolean assembleActivitySku(Long sku) {
        // 根据 SKU 查询活动商品信息（含库存总量、活动ID、次数配置ID）
        ActivitySkuEntity activitySkuEntity = activityRepository.queryActivitySku(sku);
        // 将活动 SKU 库存数量缓存到 Redis 原子计数器
        cacheActivitySkuCount(sku, activitySkuEntity.getStockCount());

        // 预热活动【查询时预热到缓存】：触发活动信息查询，命中则写入缓存
        activityRepository.queryRaffleActivityByActivityId(activitySkuEntity.getActivityId());

        // 预热活动次数【查询时预热到缓存】：触发次数配置查询，命中则写入缓存
        activityRepository.queryRaffleActivityCountByActivityCountId(activitySkuEntity.getActivityCountId());

        // 返回装配结果标识（当前占位，固定返回 false）
        return false;
    }

    /**
     * 缓存活动 SKU 库存数量
     * <p>组装缓存 key（库存前缀 + SKU），并委托仓储写入 Redis 原子计数器。
     *
     * @param sku        商品 SKU 编号
     * @param stockCount 库存数量
     */
    private void cacheActivitySkuCount(Long sku, Integer stockCount) {
        // 组装缓存 key：活动 SKU 库存前缀 + SKU 编号
        String cacheKey = Constants.RedisKey.ACTIVITY_SKU_STOCK_COUNT_KEY + sku;
        // 委托仓储将库存数量写入 Redis 原子计数器（已存在则不重复写入）
        activityRepository.cacheActivitySkuStockCount(cacheKey, stockCount);
    }

    /**
     * 扣减活动 SKU 库存（调度）
     * <p>组装缓存 key 后委托仓储执行 Redis 扣减（含防超卖加锁与库存耗尽 MQ 通知）。
     *
     * @param sku         商品 SKU 编号
     * @param endDateTime 活动结束时间，用于计算库存锁过期时间
     * @return 扣减结果：成功 true，库存耗尽/不足 false
     */
    @Override
    public boolean subtractionActivitySkuStock(Long sku, Date endDateTime) {
        // 组装缓存 key：活动 SKU 库存前缀 + SKU 编号
        String cacheKey = Constants.RedisKey.ACTIVITY_SKU_STOCK_COUNT_KEY + sku;
        // 委托仓储执行库存扣减，返回扣减结果
        return activityRepository.subtractionActivitySkuStock(sku, cacheKey,  endDateTime);
    }
}
