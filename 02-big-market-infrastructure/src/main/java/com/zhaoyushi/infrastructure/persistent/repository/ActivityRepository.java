package com.zhaoyushi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.zhaoyushi.domain.activity.model.aggregate.CreateOrderAggregate;
import com.zhaoyushi.domain.activity.model.entity.ActivityCountEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivityEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivityOrderEntity;
import com.zhaoyushi.domain.activity.model.entity.ActivitySkuEntity;
import com.zhaoyushi.domain.activity.model.valobj.ActivityStateVO;
import com.zhaoyushi.domain.activity.repository.IActivityRepository;
import com.zhaoyushi.infrastructure.persistent.dao.*;
import com.zhaoyushi.infrastructure.persistent.po.*;
import com.zhaoyushi.infrastructure.persistent.redis.IRedisService;
import com.zhaoyushi.types.common.Constants;
import com.zhaoyushi.types.enums.ResponseCode;
import com.zhaoyushi.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

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
@Slf4j
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
    /** 活动订单 DAO：操作 raffle_activity_order 表（分表） */
    @Resource
    private IRaffleActivityOrderDao raffleActivityOrderDao;
    /** 活动账户 DAO：操作 raffle_activity_account 表 */
    @Resource
    private IRaffleActivityAccountDao raffleActivityAccountDao;
    /** 编程式事务模板：手动控制订单+账户的提交/回滚 */
    @Resource
    private TransactionTemplate transactionTemplate;
    /** 分库分表路由策略：下单时以 userId 为分片键路由到目标库表 */
    @Resource
    private IDBRouterStrategy dbRouterStrategy;
    /** 分库分表路由策略：事务结束后清理路由上下文 */
    @Resource
    private IDBRouterStrategy dbRouter;

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

    /**
     * 保存订单聚合：一次性持久化“活动订单 + 活动账户次数”
     *
     * <p>执行步骤：
     * 1. 将领域订单实体/账户信息转换为持久化对象（PO）；
     * 2. 以 userId 为分片键做路由，保证后续操作落在同一库表、同一连接上；
     * 3. 在编程式事务中依次写入订单、更新账户额度（账户不存在则插入）；
     * 4. 捕获唯一索引冲突（重复下单）时回滚事务并抛出业务异常；
     * 5. finally 中清理路由上下文，避免影响后续请求。
     *
     * @param createOrderAggregate 下单聚合对象，含订单实体与账户维度次数信息
     */
    @Override
    public void doSaveOrder(CreateOrderAggregate createOrderAggregate) {
        // 1、构建活动订单持久化对象（PO），从聚合中的订单实体逐字段拷贝
        ActivityOrderEntity activityOrderEntity = createOrderAggregate.getActivityOrderEntity();
        RaffleActivityOrder raffleActivityOrder = new RaffleActivityOrder();
        raffleActivityOrder.setUserId(activityOrderEntity.getUserId());
        raffleActivityOrder.setSku(activityOrderEntity.getSku());
        raffleActivityOrder.setActivityId(activityOrderEntity.getActivityId());
        raffleActivityOrder.setActivityName(activityOrderEntity.getActivityName());
        raffleActivityOrder.setStrategyId(activityOrderEntity.getStrategyId());
        raffleActivityOrder.setOrderId(activityOrderEntity.getOrderId());
        raffleActivityOrder.setOrderTime(activityOrderEntity.getOrderTime());
        raffleActivityOrder.setState(activityOrderEntity.getState().getCode());
        raffleActivityOrder.setTotalCount(activityOrderEntity.getTotalCount());
        raffleActivityOrder.setDayCount(activityOrderEntity.getDayCount());
        raffleActivityOrder.setMonthCount(activityOrderEntity.getMonthCount());
        raffleActivityOrder.setOutBusinessNo(activityOrderEntity.getOutBusinessNo());

        // 2、构建活动账户持久化对象（PO），新增时额度与剩余额度初始一致
        RaffleActivityAccount raffleActivityAccount = new RaffleActivityAccount();
        raffleActivityAccount.setUserId(createOrderAggregate.getUserId());
        raffleActivityAccount.setActivityId(createOrderAggregate.getActivityId());
        raffleActivityAccount.setTotalCount(createOrderAggregate.getTotalCount());
        raffleActivityAccount.setTotalCountSurplus(createOrderAggregate.getTotalCount());
        raffleActivityAccount.setDayCount(createOrderAggregate.getDayCount());
        raffleActivityAccount.setDayCountSurplus(createOrderAggregate.getDayCount());
        raffleActivityAccount.setMonthCount(createOrderAggregate.getMonthCount());
        raffleActivityAccount.setMonthCountSurplus(createOrderAggregate.getMonthCount());

        try {
            // 3、以 userId 作为分片键设定路由：保证后续所有操作落在同一库表、同一连接上，从而保证事务特性
            dbRouterStrategy.doRouter(createOrderAggregate.getUserId());
            // 4、开启编程式事务：写入订单 + 更新账户，二者同事务提交/回滚
            transactionTemplate.execute(status -> {
               try {
                    // 4.1 写入订单记录
                   raffleActivityOrderDao.insert(raffleActivityOrder);
                   // 4.2 更新账户额度（累加总/日/月次数）
                   int count = raffleActivityAccountDao.updateAccountQuota(raffleActivityAccount);
                   // 4.3 更新影响行数为 0，说明账户尚不存在，改为插入新账户
                   if (0 == count) {
                       raffleActivityAccountDao.insert(raffleActivityAccount);
                   }
                   return 1;
               } catch (DuplicateKeyException e) {
                   // 唯一索引冲突：同一业务单号重复下单，回滚事务并抛出业务异常
                   status.setRollbackOnly();
                   log.error("写入订单记录，唯一索引冲突 userId: {} activityId: {} sku: {}", activityOrderEntity.getUserId(), activityOrderEntity.getActivityId(), activityOrderEntity.getSku(), e);
                   throw new AppException(ResponseCode.INDEX_DUP.getCode());
               }
            });
        }finally {
            // 5、无论成功与否，清理路由上下文，避免影响后续请求
            dbRouter.clear();
        }

    }
}
