package com.zhaoyushi.domain.activity.repository;

import com.zhaoyushi.domain.activity.model.aggregate.CreateOrderAggregate;
import com.zhaoyushi.domain.activity.model.entity.ActivityCountEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivityEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivitySkuEntity;
// 引入活动 SKU 库存 Key 值对象，作为延迟队列元素与返回值类型
import com.zhaoyushi.domain.activity.model.valobj.ActivitySkuStockKeyVO;

// 引入 java.util.Date，作为活动结束时间参数类型
import java.util.Date;

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

    /**
     * 保存订单聚合（订单 + 账户次数变更）
     *
     * <p>一次性持久化“活动订单”与“活动账户次数”，二者在同一事务中提交，保证一致性。
     * 内部会以 userId 为分片键路由到对应分库分表，并处理唯一索引冲突（幂等防重）。
     *
     * @param createOrderAggregate 下单聚合对象，含订单实体与账户维度次数信息
     */
    void doSaveOrder(CreateOrderAggregate createOrderAggregate);

    /**
     * 缓存活动 SKU 库存数量（预热用）
     * <p>将库存数量写入 Redis 原子计数器，若 key 已存在则不重复写入。
     *
     * @param cacheKey   库存缓存 key
     * @param stockCount 库存数量
     */
    void cacheActivitySkuStockCount(String cacheKey, Integer stockCount);

    /**
     * 扣减活动 SKU 缓存库存
     * <p>对 Redis 原子计数器执行 decr，库存耗尽时发 MQ 通知、库存为负时归零，并加库存锁防超卖。
     *
     * @param sku         商品 SKU 编号
     * @param cacheKey    库存缓存 key
     * @param endDateTime 活动结束时间，用于计算库存锁过期时间
     * @return 扣减结果：成功 true，库存耗尽/不足 false
     */
    boolean subtractionActivitySkuStock(Long sku, String cacheKey, Date endDateTime);

    /**
     * 将活动 SKU 库存扣减记录写入延迟队列（延迟 3 秒后供定时任务消费）
     *
     * @param activitySkuStockKeyVO 活动 SKU 库存 Key 信息
     */
    void activitySkuStockConsumeSendQueue(ActivitySkuStockKeyVO activitySkuStockKeyVO);

    /**
     * 从延迟队列取出一条库存扣减记录
     *
     * @return 库存扣减记录，队列为空时返回 null
     */
    ActivitySkuStockKeyVO takeQueueValue();

    /**
     * 清空延迟队列（库存耗尽后无需再延迟更新数据库）
     */
    void clearQueueValue();

    /**
     * 更新数据库库存（每次扣减 1 件，异步趋势更新）
     *
     * @param sku 商品 SKU 编号
     */
    void updateActivitySkuStock(Long sku);

    /**
     * 清空数据库库存（库存耗尽后直接将剩余库存置 0）
     *
     * @param sku 商品 SKU 编号
     */
    void clearActivitySkuStock(Long sku);
}
