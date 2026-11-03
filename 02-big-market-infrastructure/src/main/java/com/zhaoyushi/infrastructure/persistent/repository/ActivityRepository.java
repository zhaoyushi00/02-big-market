package com.zhaoyushi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.zhaoyushi.domain.activity.model.aggregate.CreatePartakeOrderAggregate;
import com.zhaoyushi.domain.activity.model.aggregate.CreateQuotaOrderAggregate;
import com.zhaoyushi.domain.activity.model.entity.*;
import com.zhaoyushi.domain.activity.model.valobj.ActivitySkuStockKeyVO;
import com.zhaoyushi.domain.activity.model.valobj.ActivityStateVO;
import com.zhaoyushi.domain.activity.model.valobj.UserRaffleOrderStateVO;
import com.zhaoyushi.domain.activity.repository.IActivityRepository;
// 引入活动 SKU 库存清空消息事件，用于库存耗尽时构建并发布 MQ 消息
import com.zhaoyushi.domain.event.ActivitySkuStockZeroMessageEvent;
// 引入事件发布器，用于发送 MQ 消息
import com.zhaoyushi.infrastructure.event.EventPublisher;
import com.zhaoyushi.infrastructure.persistent.dao.*;
import com.zhaoyushi.infrastructure.persistent.po.*;
import com.zhaoyushi.infrastructure.persistent.redis.IRedisService;
import com.zhaoyushi.types.common.Constants;
import com.zhaoyushi.types.enums.ResponseCode;
import com.zhaoyushi.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
// 引入 Redisson 阻塞队列接口，作为延迟队列的目标队列
import org.redisson.api.RBlockingQueue;
// 引入 Redisson 延迟队列接口，用于实现延迟投递（延迟 3 秒）
import org.redisson.api.RDelayedQueue;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

// 引入 java.util.Date，作为活动结束时间参数类型
import java.util.Date;
// 引入 java.util.concurrent.TimeUnit，用于时间单位换算（延迟队列、锁过期时间）
import java.util.concurrent.TimeUnit;

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
    /** 事件发布器：库存耗尽时发送 MQ 消息 */
    @Resource
    private EventPublisher eventPublisher;
    /** 活动 SKU 库存清空消息事件：提供 topic 与消息体构建能力 */
    @Resource
    private ActivitySkuStockZeroMessageEvent activitySkuStockZeroMessageEvent;
    @Resource
    private IUserRaffleOrderDao userRaffleOrderDao;
    @Resource
    private IRaffleActivityAccountMonthDao raffleActivityAccountMonthDao;
    @Resource
    private IRaffleActivityAccountDayDao raffleActivityAccountDayDao;


    /**
     * 根据 SKU 查询活动 SKU 实体信息
     *
     * <p>直接通过 DAO 从数据库查询 SKU 持久化对象，并将其转换为领域实体返回。
     *
     * @param sku SKU 编号
     * @return 活动 SKU 领域实体
     */
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

    /**
     * 根据活动 ID 查询活动实体信息
     *
     * <p>采用“缓存优先”策略：先读 Redis，命中则直接返回；未命中时查数据库并回填缓存。
     *
     * @param activityId 活动 ID
     * @return 活动领域实体
     */
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

    /**
     * 根据活动次数配置 ID 查询次数配置实体信息
     *
     * <p>采用“缓存优先”策略：先读 Redis，命中则直接返回；未命中时查数据库并回填缓存。
     *
     * @param activityCountId 活动次数配置 ID
     * @return 活动次数配置领域实体
     */
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
    public void doSaveOrder(CreateQuotaOrderAggregate createOrderAggregate) {
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

    /**
     * 缓存活动 SKU 库存数量
     *
     * <p>缓存已存在时跳过，避免重复预热覆盖已扣减的库存；否则写入 Redis 原子计数器。
     *
     * @param cacheKey   库存缓存 key
     * @param stockCount 库存数量
     */
    @Override
    public void cacheActivitySkuStockCount(String cacheKey, Integer stockCount) {
        // 缓存已存在则跳过，避免重复预热覆盖已扣减的库存
        if (redisService.isExists(cacheKey)) return;
        // 将库存数量写入 Redis 原子计数器（AtomicLong）
        redisService.setAtomicLong(cacheKey, stockCount);
    }

    /**
     * 扣减活动 SKU 库存
     *
     * <p>对 Redis 原子计数器减 1：库存耗尽时发送 MQ 消息通知数据库清空；剩余为负时兜底纠正为 0；
     * 同时为「这一份库存」加锁，防止后续手动恢复库存导致超卖。
     *
     * @param sku          SKU 编号
     * @param cacheKey     库存缓存 key
     * @param endDateTime  活动结束时间（用于计算锁过期时长）
     * @return 扣减是否成功（库存耗尽或加锁失败返回 false）
     */
    @Override
    public boolean subtractionActivitySkuStock(Long sku, String cacheKey, Date endDateTime) {

        // 对 Redis 原子计数器执行 decr（原子减 1），返回扣减后的剩余库存
        long surplus = redisService.decr(cacheKey);
        // 剩余库存为 0：说明刚好消耗完
        if (surplus == 0) {
            // 库存消耗完，发送MQ消息，更新数据库库存（通知消费者将数据库库存直接置 0）
            eventPublisher.publish(activitySkuStockZeroMessageEvent.topic(), activitySkuStockZeroMessageEvent.buildEventMessage(sku));
            // 返回 false 表示扣减失败（本次库存已耗尽，不应再继续下单）
            return false;
        } else if (surplus < 0){
            // 库存小于0，并发多操作，恢复为0（兜底：把负库存纠正回 0）
            redisService.setAtomicLong(cacheKey, 0);
            // 返回 false 表示扣减失败
            return false;
        }

        // 1、按照cacheKey decr后的值，与key组成库存锁的Key进行使用
        // 2、加锁为了兜底，如果后续有恢复库存，手动处理，也不会超卖，因为所有可用的库存key都被枷锁了
        // 3、设置加锁时间为活动到期 + 延时 1 天
        // 组装库存锁 key：缓存 key + 下划线 + 扣减后的剩余值（用于标记「这一份库存」已被占用）
        String lockKey = cacheKey + Constants.UNDERLINE + surplus;
        // 计算锁过期时长：活动结束时间 - 当前时间 + 1 天（保证锁在活动期间及之后一天内有效）
        long expireMillis = endDateTime.getTime() - System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);
        // 执行 SET NX 加锁（带过期时间），返回是否加锁成功
        Boolean lock = redisService.setNx(lockKey, expireMillis, TimeUnit.MILLISECONDS);
        // 加锁失败（该库存分片已被占用）则记录日志
        if (!lock){
            log.info("活动sku库存加锁失败{}", lockKey);
        }

        // 返回加锁结果作为扣减是否成功
        return lock;
    }

    /**
     * 将活动 SKU 库存扣减记录加入延迟队列
     *
     * <p>将库存扣减记录放入 Redisson 延迟队列，延迟 3 秒后投递到阻塞队列，供定时任务消费更新数据库库存。
     *
     * @param activitySkuStockKeyVO 库存扣减记录（含 SKU 编号）
     */
    @Override
    public void activitySkuStockConsumeSendQueue(ActivitySkuStockKeyVO activitySkuStockKeyVO) {
        // 组装延迟队列 key（活动 SKU 库存扣减延迟队列）
        String cacheKey = Constants.RedisKey.ACTIVITY_SKU_COUNT_QUERY_KEY;
        // 获取目标阻塞队列（延迟队列到期后元素会投递到该队列）
        RBlockingQueue<ActivitySkuStockKeyVO> blockingQueue = redisService.getBlockingQueue(cacheKey);
        // 基于阻塞队列创建延迟队列
        RDelayedQueue<ActivitySkuStockKeyVO> delayedQueue = redisService.getDelayedQueue(blockingQueue);
        // 将库存扣减记录加入延迟队列，延迟 3 秒后投递（供定时任务消费）
        delayedQueue.offer(activitySkuStockKeyVO, 3, TimeUnit.SECONDS);
    }

    /**
     * 非阻塞取出延迟队列队首元素
     *
     * <p>从目标阻塞队列非阻塞取出队首元素，队列为空时返回 null。
     *
     * @return 库存扣减记录，队列为空时返回 null
     */
    @Override
    public ActivitySkuStockKeyVO takeQueueValue() {
        // 组装延迟队列 key
        String cacheKey = Constants.RedisKey.ACTIVITY_SKU_COUNT_QUERY_KEY;
        // 获取目标阻塞队列
        RBlockingQueue<ActivitySkuStockKeyVO> destinationQueue = redisService.getBlockingQueue(cacheKey);
        // 非阻塞取出队首元素，队列为空时返回 null
        return destinationQueue.poll();
    }

    /**
     * 清空库存扣减延迟队列
     *
     * <p>库存耗尽后无需再延迟更新数据库，直接清空目标阻塞队列。
     */
    @Override
    public void clearQueueValue() {
        // 组装延迟队列 key
        String cacheKey = Constants.RedisKey.ACTIVITY_SKU_COUNT_QUERY_KEY;
        // 获取目标阻塞队列
        RBlockingQueue<ActivitySkuStockKeyVO> destinationQueue = redisService.getBlockingQueue(cacheKey);
        // 清空队列（库存耗尽后无需再延迟更新数据库）
        destinationQueue.clear();
    }

    /**
     * 更新数据库库存（剩余库存减 1）
     *
     * <p>委托 DAO 将数据库中的活动 SKU 剩余库存减 1。
     *
     * @param sku SKU 编号
     */
    @Override
    public void updateActivitySkuStock(Long sku) {
        // 委托 DAO 更新数据库库存（剩余库存减 1）
        raffleActivitySkuDao.updateActivitySkuStock(sku);
    }

    /**
     * 清空数据库库存（剩余库存置 0）
     *
     * <p>委托 DAO 将数据库中的活动 SKU 剩余库存置为 0。
     *
     * @param sku SKU 编号
     */
    @Override
    public void clearActivitySkuStock(Long sku) {
        // 委托 DAO 清空数据库库存（剩余库存置 0）
        raffleActivitySkuDao.clearActivitySkuStock(sku);
    }

    /**
     * 查询用户未使用的抽奖单（order_state = 'create'）
     *
     * <p>将查询条件封装为 PO 调用 DAO，未命中时返回 null；命中时将 PO 转换为领域实体返回。
     *
     * @param partakeRaffleActivityEntity 参与抽奖活动实体（userId + activityId）
     * @return 未使用的抽奖单实体，不存在时返回 null
     */
    @Override
    public UserRaffleOrderEntity queryNoUsedRaffleOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity) {
        // 封装查询条件：只按 userId + activityId 查（state 条件在 SQL 里固定为 create）
        UserRaffleOrder userRaffleOrderReq = new UserRaffleOrder();
        userRaffleOrderReq.setUserId(partakeRaffleActivityEntity.getUserId());
        userRaffleOrderReq.setActivityId(partakeRaffleActivityEntity.getActivityId());
        UserRaffleOrder userRaffleOrderRes = userRaffleOrderDao.queryNoUsedRaffleOrder(userRaffleOrderReq);
        // 未命中则返回 null（上层会继续走正常下单流程）
        if (null == userRaffleOrderRes) return null;
        // 封装结果：将 PO 转换为领域实体（状态枚举用 valueOf 还原）
        UserRaffleOrderEntity userRaffleOrderEntity = new UserRaffleOrderEntity();
        userRaffleOrderEntity.setUserId(userRaffleOrderRes.getUserId());
        userRaffleOrderEntity.setActivityId(userRaffleOrderRes.getActivityId());
        userRaffleOrderEntity.setActivityName(userRaffleOrderRes.getActivityName());
        userRaffleOrderEntity.setStrategyId(userRaffleOrderRes.getStrategyId());
        userRaffleOrderEntity.setOrderId(userRaffleOrderRes.getOrderId());
        userRaffleOrderEntity.setOrderTime(userRaffleOrderRes.getOrderTime());
        userRaffleOrderEntity.setOrderState(UserRaffleOrderStateVO.valueOf(userRaffleOrderRes.getOrderState()));
        return userRaffleOrderEntity;
    }

    /**
     * 保存参与活动订单聚合（抽奖单 + 总/月/日账户额度扣减）
     *
     * <p>执行步骤（同一事务内）：
     * 1. 扣减总账户额度（总/日/月剩余各减 1），失败则回滚；
     * 2. 月账户存在则扣减、不存在则插入并同步总账月额度镜像；
     * 3. 日账户存在则扣减、不存在则插入并同步总账日额度镜像；
     * 4. 写入抽奖单记录；
     * 5. 捕获唯一索引冲突（重复参与）回滚并抛业务异常。
     *
     * @param createPartakeOrderAggregate 参与活动订单聚合对象
     */
    @Override
    public void saveCreatePartakeOrderAggregate(CreatePartakeOrderAggregate createPartakeOrderAggregate) {
        try {
            // 拆解聚合对象，取出各层账户与抽奖单信息
            String userId = createPartakeOrderAggregate.getUserId();
            Long activityId = createPartakeOrderAggregate.getActivityId();
            ActivityAccountEntity activityAccountEntity = createPartakeOrderAggregate.getActivityAccountEntity();
            ActivityAccountMonthEntity activityAccountMonthEntity = createPartakeOrderAggregate.getActivityAccountMonthEntity();
            ActivityAccountDayEntity activityAccountDayEntity = createPartakeOrderAggregate.getActivityAccountDayEntity();
            UserRaffleOrderEntity userRaffleOrderEntity = createPartakeOrderAggregate.getUserRaffleOrderEntity();

            // 以 userId 为分片键路由，保证后续操作落在同一库表、同一连接上
            dbRouter.doRouter(userId);

            // 开启编程式事务：订单 + 账户额度同事务提交/回滚
            transactionTemplate.execute(status -> {
               try {
                   // 1. 更新总账户：总/日/月剩余次数各减 1（带库存保护）
                   int totalCount = raffleActivityAccountDao.updateActivityAccountSubtractionQuota(
                           RaffleActivityAccount.builder()
                                   .userId(userId)
                                   .activityId(activityId)
                                   .build());
                   if (1 != totalCount) {
                       // 总账户额度不足：回滚事务并抛业务异常
                       status.setRollbackOnly();
                       log.warn("写入创建参与活动记录，更新总账户额度不足，异常 userId: {} activityId: {}", userId, activityId);
                       throw new AppException(ResponseCode.ACCOUNT_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_QUOTA_ERROR.getInfo());
                   }

                   // 2. 创建或更新月账户，true - 存在则更新，false - 不存在则插入
                   if (createPartakeOrderAggregate.isExistAccountMonth()) {
                       // 月账户已存在：扣减当月剩余次数
                       int updateMonthCount = raffleActivityAccountMonthDao.updateActivityAccountMonthSubtractionQuota(
                               RaffleActivityAccountMonth.builder()
                                       .userId(userId)
                                       .activityId(activityId)
                                       .month(activityAccountMonthEntity.getMonth())
                                       .build());
                       if (1 != updateMonthCount) {
                           // 未更新成功则回滚（月额度不足）
                           status.setRollbackOnly();
                           log.warn("写入创建参与活动记录，更新月账户额度不足，异常 userId: {} activityId: {} month: {}", userId, activityId, activityAccountMonthEntity.getMonth());
                           throw new AppException(ResponseCode.ACCOUNT_MONTH_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_MONTH_QUOTA_ERROR.getInfo());
                       }
                   } else {
                       // 月账户不存在：插入新当月账户（剩余次数减 1 后落库）
                       raffleActivityAccountMonthDao.insertActivityAccountMonth(RaffleActivityAccountMonth.builder()
                               .userId(activityAccountMonthEntity.getUserId())
                               .activityId(activityAccountMonthEntity.getActivityId())
                               .month(activityAccountMonthEntity.getMonth())
                               .monthCount(activityAccountMonthEntity.getMonthCount())
                               .monthCountSurplus(activityAccountMonthEntity.getMonthCountSurplus() - 1)
                               .build());
                       // 新创建月账户，则更新总账表中月镜像额度（把当月剩余同步回总账）
                       raffleActivityAccountDao.updateActivityAccountMonthSurplusImageQuota(RaffleActivityAccount.builder()
                               .userId(userId)
                               .activityId(activityId)
                               .monthCountSurplus(activityAccountEntity.getMonthCountSurplus())
                               .build());
                   }

                   // 3. 创建或更新日账户，true - 存在则更新，false - 不存在则插入
                   if (createPartakeOrderAggregate.isExistAccountDay()) {
                       // 日账户已存在：扣减当日剩余次数
                       int updateDayCount = raffleActivityAccountDayDao.updateActivityAccountDaySubtractionQuota(RaffleActivityAccountDay.builder()
                               .userId(userId)
                               .activityId(activityId)
                               .day(activityAccountDayEntity.getDay())
                               .build());
                       if (1 != updateDayCount) {
                           // 未更新成功则回滚（日额度不足）
                           status.setRollbackOnly();
                           log.warn("写入创建参与活动记录，更新日账户额度不足，异常 userId: {} activityId: {} day: {}", userId, activityId, activityAccountDayEntity.getDay());
                           throw new AppException(ResponseCode.ACCOUNT_DAY_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_DAY_QUOTA_ERROR.getInfo());
                       }
                   } else {
                       // 日账户不存在：插入新当日账户（剩余次数减 1 后落库）
                       raffleActivityAccountDayDao.insertActivityAccountDay(RaffleActivityAccountDay.builder()
                               .userId(activityAccountDayEntity.getUserId())
                               .activityId(activityAccountDayEntity.getActivityId())
                               .day(activityAccountDayEntity.getDay())
                               .dayCount(activityAccountDayEntity.getDayCount())
                               .dayCountSurplus(activityAccountDayEntity.getDayCountSurplus() - 1)
                               .build());
                       // 新创建日账户，则更新总账表中日镜像额度（把当日剩余同步回总账）
                       raffleActivityAccountDao.updateActivityAccountDaySurplusImageQuota(RaffleActivityAccount.builder()
                               .userId(userId)
                               .activityId(activityId)
                               .dayCountSurplus(activityAccountEntity.getDayCountSurplus())
                               .build());
                   }

                   // 4. 写入参与活动订单（生成抽奖单）
                   userRaffleOrderDao.insert(UserRaffleOrder.builder()
                           .userId(userRaffleOrderEntity.getUserId())
                           .activityId(userRaffleOrderEntity.getActivityId())
                           .activityName(userRaffleOrderEntity.getActivityName())
                           .strategyId(userRaffleOrderEntity.getStrategyId())
                           .orderId(userRaffleOrderEntity.getOrderId())
                           .orderTime(userRaffleOrderEntity.getOrderTime())
                           .orderState(userRaffleOrderEntity.getOrderState().getCode())
                           .build());

               } catch (DuplicateKeyException e) {
                    // 唯一索引冲突：同一订单号重复参与，回滚事务并抛业务异常
                    status.setRollbackOnly();
                    log.error("写入创建参与活动记录，唯一索引冲突 userId:{} activityId:{}", userId, activityId, e);
                    throw new AppException(ResponseCode.INDEX_DUP.getCode(), e);
               }
                return 1;
            });

        }finally {
            // 无论成功与否，清理路由上下文，避免影响后续请求
            dbRouter.clear();
        }
    }

    /**
     * 查询用户在某活动上的总账户额度
     *
     * @param userId     用户ID
     * @param activityId 活动ID
     * @return 活动总账户实体，不存在时返回 null
     */
    @Override
    public ActivityAccountEntity queryActivityAccountByUserId(String userId, Long activityId) {
        // 1. 封装查询条件并按用户+活动查询账户
        RaffleActivityAccount raffleActivityAccountReq = new RaffleActivityAccount();
        raffleActivityAccountReq.setUserId(userId);
        raffleActivityAccountReq.setActivityId(activityId);
        RaffleActivityAccount raffleActivityAccountRes = raffleActivityAccountDao.queryActivityAccountByUserId(raffleActivityAccountReq);
        // 未命中则返回 null
        if (null == raffleActivityAccountRes) return null;
        // 2. 转换对象：PO -> 领域实体
        return ActivityAccountEntity.builder()
                .userId(raffleActivityAccountRes.getUserId())
                .activityId(raffleActivityAccountRes.getActivityId())
                .totalCount(raffleActivityAccountRes.getTotalCount())
                .totalCountSurplus(raffleActivityAccountRes.getTotalCountSurplus())
                .dayCount(raffleActivityAccountRes.getDayCount())
                .dayCountSurplus(raffleActivityAccountRes.getDayCountSurplus())
                .monthCount(raffleActivityAccountRes.getMonthCount())
                .monthCountSurplus(raffleActivityAccountRes.getMonthCountSurplus())
                .build();
    }

    /**
     * 查询用户在某活动某月上的月账户额度
     *
     * @param userId     用户ID
     * @param activityId 活动ID
     * @param month      月份（yyyy-MM）
     * @return 月账户实体，不存在时返回 null
     */
    @Override
    public ActivityAccountMonthEntity queryActivityAccountMonthByUserId(String userId, Long activityId, String month) {
        // 1. 封装查询条件并按用户+活动+月查询账户
        RaffleActivityAccountMonth raffleActivityAccountMonthReq = new RaffleActivityAccountMonth();
        raffleActivityAccountMonthReq.setUserId(userId);
        raffleActivityAccountMonthReq.setActivityId(activityId);
        raffleActivityAccountMonthReq.setMonth(month);
        RaffleActivityAccountMonth raffleActivityAccountMonthRes = raffleActivityAccountMonthDao.queryActivityAccountMonthByUserId(raffleActivityAccountMonthReq);
        // 未命中则返回 null
        if (null == raffleActivityAccountMonthRes) return null;
        // 2. 转换对象：PO -> 领域实体
        return ActivityAccountMonthEntity.builder()
                .userId(raffleActivityAccountMonthRes.getUserId())
                .activityId(raffleActivityAccountMonthRes.getActivityId())
                .month(raffleActivityAccountMonthRes.getMonth())
                .monthCount(raffleActivityAccountMonthRes.getMonthCount())
                .monthCountSurplus(raffleActivityAccountMonthRes.getMonthCountSurplus())
                .build();
    }

    /**
     * 查询用户在某活动某日上的日账户额度
     *
     * @param userId     用户ID
     * @param activityId 活动ID
     * @param day        日期（yyyy-MM-dd）
     * @return 日账户实体，不存在时返回 null
     */
    @Override
    public ActivityAccountDayEntity queryActivityAccountDayByUserId(String userId, Long activityId, String day) {
        // 1. 封装查询条件并按用户+活动+日查询账户
        RaffleActivityAccountDay raffleActivityAccountDayReq = new RaffleActivityAccountDay();
        raffleActivityAccountDayReq.setUserId(userId);
        raffleActivityAccountDayReq.setActivityId(activityId);
        raffleActivityAccountDayReq.setDay(day);
        RaffleActivityAccountDay raffleActivityAccountDayRes = raffleActivityAccountDayDao.queryActivityAccountDayByUserId(raffleActivityAccountDayReq);
        // 未命中则返回 null
        if (null == raffleActivityAccountDayRes) return null;
        // 2. 转换对象：PO -> 领域实体
        return ActivityAccountDayEntity.builder()
                .userId(raffleActivityAccountDayRes.getUserId())
                .activityId(raffleActivityAccountDayRes.getActivityId())
                .day(raffleActivityAccountDayRes.getDay())
                .dayCount(raffleActivityAccountDayRes.getDayCount())
                .dayCountSurplus(raffleActivityAccountDayRes.getDayCountSurplus())
                .build();
    }
}
