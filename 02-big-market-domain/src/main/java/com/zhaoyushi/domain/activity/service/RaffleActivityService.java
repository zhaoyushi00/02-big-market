package com.zhaoyushi.domain.activity.service;

import com.zhaoyushi.domain.activity.model.aggregate.CreateOrderAggregate;
import com.zhaoyushi.domain.activity.model.entity.*;
import com.zhaoyushi.domain.activity.model.valobj.ActivitySkuStockKeyVO;
import com.zhaoyushi.domain.activity.model.valobj.OrderStateVO;
import com.zhaoyushi.domain.activity.repository.IActivityRepository;
import com.zhaoyushi.domain.activity.service.rule.factory.DefaultActivityChainFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 抽奖活动服务实现（领域服务）
 *
 * <p>继承 {@link AbstractRaffleActivity} 模板，实现两个钩子方法：
 * 1. buildOrderAggregate：组装订单实体 + 账户次数信息，构建下单聚合对象；
 * 2. doSaveOrder：委托仓储持久化聚合（订单 + 账户次数）。
 *
 * @author zhaoy
 * @date 2026/10/24
 * @description 抽奖活动服务实现
 */
@Service
// 继承抽象活动服务模板，同时实现 ISkuStock 库存处理端口（供定时任务/MQ 监听器调用）
public class RaffleActivityService extends AbstractRaffleActivity implements ISkuStock{
    /**
     * 构造器注入活动仓储与责任链工厂
     *
     * @param activityRepository          活动仓储实现
     * @param defaultActivityChainFactory 活动动作责任链工厂
     */
    public RaffleActivityService(IActivityRepository activityRepository, DefaultActivityChainFactory defaultActivityChainFactory) {
        super(activityRepository, defaultActivityChainFactory);
    }

    /**
     * 构建下单聚合对象（模板方法钩子实现）
     *
     * <p>根据查询到的 SKU/活动/次数信息组装 {@link ActivityOrderEntity} 订单实体（生成订单号、订单时间、次数快照），
     * 再与账户维度信息（userId/activityId/总日/月次数）一起打包为 {@link CreateOrderAggregate} 返回。
     *
     * @param skuRechargeEntity   活动商品充值实体（用户ID + SKU + 外部业务单号）
     * @param activitySkuEntity   活动商品（SKU）实体
     * @param activityEntity      活动实体（名称、时间、策略、状态）
     * @param activityCountEntity 活动次数配置实体（总/日/月次数）
     * @return 组装完成的下单聚合对象
     */
    @Override
    protected CreateOrderAggregate buildOrderAggregate(SkuRechargeEntity skuRechargeEntity, ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity) {
        // 订单实体对象
        ActivityOrderEntity activityOrderEntity = new ActivityOrderEntity();
        activityOrderEntity.setUserId(skuRechargeEntity.getUserId());
        activityOrderEntity.setSku(skuRechargeEntity.getSku());
        activityOrderEntity.setActivityId(activityEntity.getActivityId());
        activityOrderEntity.setActivityName(activityEntity.getActivityName());
        activityOrderEntity.setStrategyId(activityEntity.getStrategyId());
        // 雪花算法UUID服务，直接生成个12位
        activityOrderEntity.setOrderId(RandomStringUtils.randomNumeric(12));
        activityOrderEntity.setOrderTime(new Date());
        activityOrderEntity.setTotalCount(activityCountEntity.getTotalCount());
        activityOrderEntity.setDayCount(activityCountEntity.getDayCount());
        activityOrderEntity.setMonthCount(activityCountEntity.getMonthCount());
        activityOrderEntity.setState(OrderStateVO.completed);
        activityOrderEntity.setOutBusinessNo(skuRechargeEntity.getOutBusinessNo());

        // 构建聚合对象
        return CreateOrderAggregate.builder()
                .userId(skuRechargeEntity.getUserId())
                .activityId(activitySkuEntity.getActivityId())
                .totalCount(activityCountEntity.getTotalCount())
                .dayCount(activityCountEntity.getDayCount())
                .monthCount(activityCountEntity.getMonthCount())
                .activityOrderEntity(activityOrderEntity)
                .build();
    }

    /**
     * 保存下单聚合（模板方法钩子实现）
     *
     * <p>直接委托仓储 {@link IActivityRepository#doSaveOrder} 持久化，仓储内部负责分库分表路由与事务控制。
     *
     * @param createOrderAggregate 待保存的下单聚合对象
     */
    @Override
    protected void doSaveOrder(CreateOrderAggregate createOrderAggregate) {
        activityRepository.doSaveOrder(createOrderAggregate);
    }

    /**
     * 获取延迟队列中的库存扣减记录（实现 ISkuStock 端口）
     *
     * @return 库存扣减记录，队列为空时返回 null
     * @throws InterruptedException 取队列被中断异常
     */
    @Override
    public ActivitySkuStockKeyVO takeQueueValue() throws InterruptedException {
        // 委托仓储从延迟队列取出一条库存扣减记录
        return activityRepository.takeQueueValue();
    }

    /**
     * 清空延迟队列（实现 ISkuStock 端口）
     */
    @Override
    public void clearQueueValue() {
        // 委托仓储清空延迟队列
        activityRepository.clearQueueValue();
    }

    /**
     * 更新数据库库存（实现 ISkuStock 端口，每次扣减 1 件）
     *
     * @param sku 商品 SKU 编号
     */
    @Override
    public void updateActivitySkuStock(Long sku) {
        // 委托仓储更新数据库库存
        activityRepository.updateActivitySkuStock(sku);
    }

    /**
     * 清空数据库库存（实现 ISkuStock 端口，库存耗尽后置 0）
     *
     * @param sku 商品 SKU 编号
     */
    @Override
    public void clearActivitySkuStock(Long sku) {
        // 委托仓储清空数据库库存
        activityRepository.clearActivitySkuStock(sku);
    }
}
