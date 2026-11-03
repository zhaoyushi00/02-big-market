package com.zhaoyushi.domain.activity.service.quota;

import com.zhaoyushi.domain.activity.model.aggregate.CreateQuotaOrderAggregate;
import com.zhaoyushi.domain.activity.model.entity.*;
import com.zhaoyushi.domain.activity.repository.IActivityRepository;
import com.zhaoyushi.domain.activity.service.IRaffleActivityAccountQuotaService;
import com.zhaoyushi.domain.activity.service.quota.rule.IActionChain;
import com.zhaoyushi.domain.activity.service.quota.rule.factory.DefaultActivityChainFactory;
import com.zhaoyushi.types.enums.ResponseCode;
import com.zhaoyushi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 抽象活动服务类，定义标准的下单流程（模板方法）
 *
 * <p>实现 {@link IRaffleActivityAccountQuotaService}，以模板方法固化“创建活动订单/充值订单”的标准步骤。
 * 子类 {@link RaffleActivityAccountQuotaService} 只需实现两个钩子方法（构建聚合、保存订单）即可复用完整流程。
 *
 * <p>关键点：
 * 1. 通过构造器注入仓储与责任链工厂，子类可复用该流程；
 * 2. createRaffleActivityOrder 为历史遗留骨架（初始化账户、生成订单逻辑待完善）；
 * 3. createSkuRechargeOrder 为标准下单模板：校验 → 查询 → 规则过滤 → 构建聚合 → 保存 → 返回单号。
 *
 * @author zhaoy
 * @date 2026/10/23
 * @description 抽象活动抽象类，定义标准的流程
 */
@Slf4j
public abstract class AbstractRaffleActivityAccountQuota extends RaffleActivityAccountQuotaSupport implements IRaffleActivityAccountQuotaService {

    /**
     * 构造器：注入活动仓储与责任链工厂，供子类复用标准的下单流程
     *
     * @param activityRepository          活动仓储，提供 SKU、活动、次数配置的查询能力
     * @param defaultActivityChainFactory 活动动作责任链工厂，用于获取下单前的规则校验责任链
     */
    public AbstractRaffleActivityAccountQuota(IActivityRepository activityRepository, DefaultActivityChainFactory defaultActivityChainFactory) {
        super(activityRepository, defaultActivityChainFactory);
    }

    /**
     * 创建 sku 账户充值订单，给用户的活动账户增加抽奖次数
     *
     * <p>标准下单流程：
     * 参数校验 → 查询基础信息（SKU/活动/次数配置）→ 活动动作规则校验（责任链）→ 构建订单聚合 → 保存订单 → 返回订单号。
     *
     * @param skuRechargeEntity 活动商品充值实体（用户ID + SKU + 外部业务单号）
     * @return 活动订单号（orderId）
     */
    @Override
    public String createSkuRechargeOrder(SkuRechargeEntity skuRechargeEntity) {

        // 1、参数验证
        String userId = skuRechargeEntity.getUserId();
        Long sku = skuRechargeEntity.getSku();
        String outBusinessNo = skuRechargeEntity.getOutBusinessNo();

        if (null == sku || StringUtils.isBlank(userId) || StringUtils.isBlank(outBusinessNo)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }

        // 2、查询基础信息
        // 2.1 通过sku查询活动信息
        ActivitySkuEntity activitySkuEntity = queryActivitySku(sku);
        // 2.2 查询活动信息
        ActivityEntity activityEntity = queryRaffleActivityByActivityId(activitySkuEntity.getActivityId());
        // 2.3 查询次数信息（用户在活动上可参与的次数）
        ActivityCountEntity activityCountEntity = queryRaffleActivityCountByActivityCountId(activitySkuEntity.getActivityCountId());

        // 3、活动动作规则校验 todo 后续处理规则过滤流程，暂时也不处理责任链结果
        // 打开活动动作责任链（当前为 基础校验 -> SKU 库存校验 两个节点）
        IActionChain actionChain = defaultActivityChainFactory.openActionChain();
        // 执行责任链：依次做活动状态/日期/库存校验，并扣减库存（校验失败会抛出 AppException 终止下单）
        actionChain.action(activitySkuEntity, activityEntity, activityCountEntity);

        // 4、构建订单聚合对象
        CreateQuotaOrderAggregate createOrderAggregate = buildOrderAggregate(skuRechargeEntity, activitySkuEntity, activityEntity, activityCountEntity);

        // 5、保存订单
        doSaveOrder(createOrderAggregate);

        // 6、返回单号
        return createOrderAggregate.getActivityOrderEntity().getOrderId();

    }

    /**
     * 构建订单聚合对象（模板方法钩子，由子类实现）
     *
     * <p>将充值入参与查询到的 SKU/活动/次数信息组装为 {@link CreateOrderAggregate} 聚合对象，
     * 聚合内部包含活动订单与账户次数变更流水，供后续落库使用。
     *
     * @param skuRechargeEntity   活动商品充值实体（用户ID + SKU + 外部业务单号）
     * @param activitySkuEntity   活动商品（SKU）实体
     * @param activityEntity      活动实体（名称、时间、策略、状态）
     * @param activityCountEntity 活动次数配置实体（总/日/月次数）
     * @return 组装完成的订单聚合对象
     */
    protected abstract CreateQuotaOrderAggregate buildOrderAggregate(SkuRechargeEntity skuRechargeEntity, ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity);

    /**
     * 保存订单聚合（模板方法钩子，由子类实现）
     *
     * <p>将构建完成的订单聚合对象持久化（活动订单 + 账户次数变更流水），
     * 落库失败时应抛出异常以便事务回滚。
     *
     * @param createOrderAggregate 待保存的订单聚合对象
     */
    protected abstract void doSaveOrder(CreateQuotaOrderAggregate createOrderAggregate);

}
