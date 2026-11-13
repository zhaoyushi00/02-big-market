package com.zhaoyushi.domain.activity.service.partake;

import com.zhaoyushi.domain.activity.model.aggregate.CreatePartakeOrderAggregate;
import com.zhaoyushi.domain.activity.model.entity.*;
import com.zhaoyushi.domain.activity.model.valobj.UserRaffleOrderStateVO;
import com.zhaoyushi.domain.activity.repository.IActivityRepository;
import com.zhaoyushi.types.enums.ResponseCode;
import com.zhaoyushi.types.exception.AppException;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 抽奖活动参与服务实现（领域服务）
 *
 * <p>继承 {@link AbstractRaffleActivityPartake} 模板，实现两个钩子方法：
 * 1. {@link #buildUserRaffleOrder}：查询活动信息并构建抽奖单（生成 12 位订单号、初始状态 create）；
 * 2. {@link #doFilterAccount}：按总/月/日三层维度校验并构建账户额度聚合对象。
 *
 * @author zhaoy
 * @date 2026/11/2
 * @description 抽奖活动参与服务实现
 */
@Service
public class RaffleActivityPartakeService extends AbstractRaffleActivityPartake{

    /** 月维度日期格式化器：用于生成月账户的 month（yyyy-MM） */
    private final SimpleDateFormat dateFormatMonth = new SimpleDateFormat("yyyy-MM");
    /** 日维度日期格式化器：用于生成日账户的 day（yyyy-MM-dd） */
    private final SimpleDateFormat dateFormatDay = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 构造器：注入活动仓储并调用父类构造器
     *
     * @param activityRepository 活动仓储实现
     */
    protected RaffleActivityPartakeService(IActivityRepository activityRepository) {
        super(activityRepository);
    }

    /**
     * 构建抽奖单实体（模板钩子实现）
     *
     * @param userId      用户ID
     * @param activityId  活动ID
     * @param currentDate 当前时间
     * @return 用户抽奖订单实体
     */
    @Override
    protected UserRaffleOrderEntity buildUserRaffleOrder(String userId, Long activityId, Date currentDate) {
        // 查询活动实体：拿活动名称、策略ID用于填充订单冗余字段
        ActivityEntity activityEntity = activityRepository.queryRaffleActivityByActivityId(activityId);
        // 构建订单：新建抽奖单实体并逐字段填充
        UserRaffleOrderEntity userRaffleOrder = new UserRaffleOrderEntity();
        userRaffleOrder.setUserId(userId);
        userRaffleOrder.setActivityId(activityId);
        userRaffleOrder.setActivityName(activityEntity.getActivityName());
        userRaffleOrder.setStrategyId(activityEntity.getStrategyId());
        // 生成 12 位随机数字作为订单号（唯一凭证）
        userRaffleOrder.setOrderId(RandomStringUtils.randomNumeric(12));
        userRaffleOrder.setOrderTime(currentDate);
        // 订单初始状态：create（创建、未使用）
        userRaffleOrder.setOrderState(UserRaffleOrderStateVO.create);
        // 冗余活动结束时间到订单：后续抽奖时透传到库存扣减链路，作为锁 Key 的过期时间基准
        userRaffleOrder.setEndDateTime(activityEntity.getEndDateTime());
        return userRaffleOrder;
    }


    /**
     * 额度账户过滤并构建聚合（模板钩子实现）
     *
     * <p>按“总账户 -> 月账户 -> 日账户”三层依次校验剩余额度：
     * 任一层额度不足则抛业务异常；月/日账户不存在时则用总账户的额度镜像初始化新建。
     *
     * @param userId      用户ID
     * @param activityId  活动ID
     * @param currentDate 当前时间
     * @return 参与活动订单聚合对象
     */
    @Override
    protected CreatePartakeOrderAggregate doFilterAccount(String userId, Long activityId, Date currentDate) {

        // 查询总账户额度（用户在该活动上的总/日/月剩余次数）
        ActivityAccountEntity activityAccountEntity = activityRepository.queryActivityAccountByUserId(userId, activityId);

        // 额度判断（只判断总剩余额度）：总账户不存在或总剩余额度已耗尽则抛异常
        if (null == activityAccountEntity || activityAccountEntity.getTotalCountSurplus() <= 0) {
            throw new AppException(ResponseCode.ACCOUNT_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_QUOTA_ERROR.getInfo());
        }

        // 格式化当前月份（yyyy-MM），作为月账户额度的查询维度
        String month = dateFormatMonth.format(currentDate);

        // 查询月额度（当月维度）
        ActivityAccountMonthEntity activityAccountMonthEntity = activityRepository.queryActivityAccountMonthByUserId(userId, activityId, month);
        // 月账户已存在且剩余额度不足，抛出业务异常
        if (null != activityAccountMonthEntity && activityAccountMonthEntity.getMonthCountSurplus() <= 0) {
            throw new AppException(ResponseCode.ACCOUNT_MONTH_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_MONTH_QUOTA_ERROR.getInfo());
        }

        // 创建月账户额度；true = 存在月账户、false = 不存在月账户
        boolean isExistAccountMonth = activityAccountMonthEntity != null;
        if (null == activityAccountMonthEntity) {
            // 月账户不存在：新建当月账户，并用总账户的月额度初始化
            activityAccountMonthEntity = new ActivityAccountMonthEntity();
            activityAccountMonthEntity.setUserId(userId);
            activityAccountMonthEntity.setActivityId(activityId);
            activityAccountMonthEntity.setMonth(month);
            activityAccountMonthEntity.setMonthCount(activityAccountEntity.getMonthCount());
            activityAccountMonthEntity.setMonthCountSurplus(activityAccountEntity.getMonthCount());
        }

        // 格式化当前日期（yyyy-MM-dd），作为日账户额度的查询维度
        String day = dateFormatDay.format(currentDate);

        // 查询日账户额度（当日维度）
        ActivityAccountDayEntity activityAccountDayEntity = activityRepository.queryActivityAccountDayByUserId(userId, activityId, day);
        // 日账户已存在且剩余额度不足，抛出业务异常
        if (null != activityAccountDayEntity && activityAccountDayEntity.getDayCountSurplus() <= 0) {
            throw new AppException(ResponseCode.ACCOUNT_DAY_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_DAY_QUOTA_ERROR.getInfo());
        }

        // 创建日账户额度；true = 存在日账户、false = 不存在日账户
        boolean isExistAccountDay = activityAccountDayEntity != null;
        if (null == activityAccountDayEntity) {
            // 日账户不存在：新建当日账户，并用总账户的日额度初始化
            activityAccountDayEntity = new ActivityAccountDayEntity();
            activityAccountDayEntity.setUserId(userId);
            activityAccountDayEntity.setActivityId(activityId);
            activityAccountDayEntity.setDay(day);
            activityAccountDayEntity.setDayCount(activityAccountEntity.getDayCount());
            activityAccountDayEntity.setDayCountSurplus(activityAccountEntity.getDayCount());
        }

        // 构建聚合对象：把总/月/日账户信息与存在标记打包进聚合
        CreatePartakeOrderAggregate createPartakeOrderAggregate = new CreatePartakeOrderAggregate();
        createPartakeOrderAggregate.setUserId(userId);
        createPartakeOrderAggregate.setActivityId(activityId);
        createPartakeOrderAggregate.setActivityAccountEntity(activityAccountEntity);
        createPartakeOrderAggregate.setExistAccountMonth(isExistAccountMonth);
        createPartakeOrderAggregate.setActivityAccountMonthEntity(activityAccountMonthEntity);
        createPartakeOrderAggregate.setExistAccountDay(isExistAccountDay);
        createPartakeOrderAggregate.setActivityAccountDayEntity(activityAccountDayEntity);

        return createPartakeOrderAggregate;
    }
}
