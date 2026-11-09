package com.zhaoyushi.domain.activity.service.partake;

import com.alibaba.fastjson.JSON;
import com.zhaoyushi.domain.activity.model.aggregate.CreatePartakeOrderAggregate;
import com.zhaoyushi.domain.activity.model.entity.ActivityEntity;
import com.zhaoyushi.domain.activity.model.entity.PartakeRaffleActivityEntity;
import com.zhaoyushi.domain.activity.model.entity.UserRaffleOrderEntity;
import com.zhaoyushi.domain.activity.model.valobj.ActivityStateVO;
import com.zhaoyushi.domain.activity.repository.IActivityRepository;
import com.zhaoyushi.domain.activity.service.IRaffleActivityPartakeService;
import com.zhaoyushi.types.enums.ResponseCode;
import com.zhaoyushi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * 抽奖活动参与抽象类（模板方法）
 *
 * <p>以模板方法固化「用户参与抽奖活动」的标准流程，子类只需实现两个钩子方法即可复用：
 * 1. {@link #buildUserRaffleOrder}：构建抽奖单实体；
 * 2. {@link #doFilterAccount}：过滤并构建账户额度聚合对象。
 *
 * <p>标准流程（createOrder）：
 * 活动查询与状态/日期校验 -> 查询未使用的抽奖单（存在则直接复用） -> 账户额度过滤 -> 构建订单 -> 填充聚合 -> 保存聚合。
 *
 * @author zhaoy
 * @date 2026/11/1
 * @description 抽奖活动参与抽象类
 */
@Slf4j
public abstract class AbstractRaffleActivityPartake implements IRaffleActivityPartakeService {

    /** 活动仓储：提供活动查询、订单/账户查询与聚合保存能力 */
    protected final IActivityRepository activityRepository;

    /**
     * 构造器：注入活动仓储（由 Spring 通过子类构造器传递）
     *
     * @param activityRepository 活动仓储实现
     */
    protected AbstractRaffleActivityPartake(IActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    /**
     * 创建抽奖单（模板方法）
     *
     * @param partakeRaffleActivityEntity 参与抽奖活动实体（用户ID + 活动ID）
     * @return 用户抽奖订单实体（存在未消费订单时返回已有订单）
     */
    @Override
    public UserRaffleOrderEntity createOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity) {
        // 0、基础信息：提取入参中的用户与活动信息，并取当前时间
        String userId = partakeRaffleActivityEntity.getUserId();
        Long activityId = partakeRaffleActivityEntity.getActivityId();
        Date currentDate = new Date();

        // 1、活动查询：按活动ID查活动实体（含状态、起止时间）
        ActivityEntity activityEntity = activityRepository.queryRaffleActivityByActivityId(activityId);
        // 校验：活动状态（只有 open 状态的活动才允许下单）
        if(!ActivityStateVO.open.equals(activityEntity.getState())){
            // 状态非法则抛出业务异常（活动未开启）
            throw new AppException(ResponseCode.ACTIVITY_DATE_ERROR.getCode(), ResponseCode.ACTIVITY_STATE_ERROR.getInfo());
        }
        // 判断当前时间是否早于开始时间或晚于结束时间（任一成立即为非活动日期范围）
        if (activityEntity.getBeginDateTime().after(currentDate) || activityEntity.getEndDateTime().before(currentDate)){
            // 日期非法则抛出业务异常（非活动日期范围）
            throw new AppException(ResponseCode.ACTIVITY_DATE_ERROR.getCode(), ResponseCode.ACTIVITY_DATE_ERROR.getInfo());
        }

        // 2、查询未被使用的活动参与订单记录（幂等：已有未消费订单则不再扣额度）
        UserRaffleOrderEntity userRaffleOrderEntity = activityRepository.queryNoUsedRaffleOrder(partakeRaffleActivityEntity);
        if (null != userRaffleOrderEntity) {
            // 已存在未消费订单：打印日志并直接返回，避免重复扣减账户额度
            log.info("创建参与活动订单【已存在未消费】 userId:{} activityId:{} userRaffleOrderEntity:{}", userId, activityId, JSON.toJSONString(userRaffleOrderEntity));
            return userRaffleOrderEntity;
        }

        // 3、额度账户过滤：校验总/月/日额度并返回账户构建对象（模板钩子）
        CreatePartakeOrderAggregate createPartakeOrderAggregate = this.doFilterAccount(userId, activityId, currentDate);

        // 4、构建订单：生成抽奖单实体（模板钩子）
        UserRaffleOrderEntity userRaffleOrder = this.buildUserRaffleOrder(userId, activityId, currentDate);

        // 5、填充抽奖单实体对象：把生成的订单塞进聚合对象
        createPartakeOrderAggregate.setUserRaffleOrderEntity(userRaffleOrder);

        // 6、保存聚合对象 - 一个领域内的一个聚合是一个事务操作（订单 + 账户额度同事务提交）
        activityRepository.saveCreatePartakeOrderAggregate(createPartakeOrderAggregate);

        // 7、返回订单信息
        return userRaffleOrder;
    }

    /**
     * 创建抽奖单（便捷重载）
     * <p>将「用户ID + 活动ID」两个入参封装为参与活动实体后，委托给 {@link #createOrder(PartakeRaffleActivityEntity)}
     * 完成后续创建订单流程，避免调用方手工组装实体。
     *
     * @param userId     用户ID
     * @param activityId 活动ID
     * @return 用户抽奖订单实体对象
     */
    @Override
    public UserRaffleOrderEntity createOrder(String userId, Long activityId) {
        // 使用构建者模式组装参与活动实体，再复用主流程创建订单
        return createOrder(PartakeRaffleActivityEntity.builder()
                // 设置活动ID
                .activityId(activityId)
                // 设置用户ID
                .userId(userId)
                // 构建参与活动实体对象
                .build());
    }

    /**
     * 构建抽奖单实体（模板方法钩子，由子类实现）
     *
     * @param userId      用户ID
     * @param activityId  活动ID
     * @param currentDate 当前时间
     * @return 用户抽奖订单实体
     */
    protected abstract UserRaffleOrderEntity buildUserRaffleOrder(String userId, Long activityId, Date currentDate);

    /**
     * 额度账户过滤并构建聚合（模板方法钩子，由子类实现）
     *
     * @param userId      用户ID
     * @param activityId  活动ID
     * @param currentDate 当前时间
     * @return 参与活动订单聚合对象（含账户额度信息）
     */
    protected abstract CreatePartakeOrderAggregate doFilterAccount(String userId, Long activityId, Date currentDate);
}
