package com.zhaoyushi.trigger.http;

import com.zhaoyushi.domain.activity.model.entity.UserRaffleOrderEntity;
import com.zhaoyushi.domain.activity.service.IRaffleActivityPartakeService;
import com.zhaoyushi.domain.activity.service.armory.IActivityArmory;
import com.zhaoyushi.domain.award.model.entity.UserAwardRecordEntity;
import com.zhaoyushi.domain.award.model.valobj.AwardStateVO;
import com.zhaoyushi.domain.award.service.IAwardService;
import com.zhaoyushi.domain.strategy.model.entity.RaffleAwardEntity;
import com.zhaoyushi.domain.strategy.model.entity.RaffleFactorEntity;
import com.zhaoyushi.domain.strategy.service.IRaffleStrategy;
import com.zhaoyushi.domain.strategy.service.armory.IStrategyArmory;
import com.zhaoyushi.trigger.api.IRaffleActivityService;
import com.zhaoyushi.trigger.api.dto.ActivityDrawRequestDTO;
import com.zhaoyushi.trigger.api.dto.ActivityDrawResponseDTO;
import com.zhaoyushi.types.enums.ResponseCode;
import com.zhaoyushi.types.exception.AppException;
import com.zhaoyushi.types.model.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 抽奖活动服务控制器
 * <p>活动维度的抽奖入口，实现 {@link IRaffleActivityService} 接口，对外提供两个接口：
 * <pre>
 * 1. armory - 活动装配（数据预热）：把活动 SKU 库存与对应策略奖品一起预热到缓存；
 * 2. draw   - 活动抽奖：完整串联「创建订单 -> 执行抽奖 -> 写入中奖记录」的抽奖链路。
 * </pre>
 *
 * @author zhaoy
 * @date 2026/11/7
 * @description 抽奖活动服务
 */
@Slf4j
// 声明该类为 REST 风格的控制器，方法返回值会序列化为 JSON 响应
@RestController()
// 允许跨域访问，跨域地址从配置文件 app.config.cross-origin 读取 即版本号V1
@CrossOrigin("${app.config.cross-origin}")
// 定义该类所有接口的公共请求路径前缀，拼接后形如 /api/v1/raffle/activity
@RequestMapping("/api/${app.config.api-version}/raffle/activity")
public class RaffleActivityController implements IRaffleActivityService {

    // 注入活动参与服务，负责创建抽奖参与订单
    @Resource
    private IRaffleActivityPartakeService raffleActivityPartakeService;  //参与活动
    // 注入抽奖策略服务，负责执行随机抽奖
    @Resource
    private IRaffleStrategy raffleStrategy;  // 参与抽奖
    // 注入奖品服务，负责保存中奖记录
    @Resource
    private IAwardService awardService;  // 奖品操作
    // 注入活动装配服务，负责活动 SKU 库存预热
    @Resource
    private IActivityArmory activityArmory;
    // 注入策略装配服务，负责策略奖品数据预热
    @Resource
    private IStrategyArmory strategyArmory;

    /**
     * 活动装配 - 数据预热 | 把活动配置的对应的 sku 一起装配
     *
     * @param activityId 活动ID
     * @return 装配结果
     * <p>
     * 接口：<a href="http://localhost:8091/api/v1/raffle/activity/armory">/api/v1/raffle/activity/armory</a>
     * 入参：{"activityId":100001,"userId":"xiaofuge"}
     * <p>
     * curl --request GET \
     * --url 'http://localhost:8091/api/v1/raffle/activity/armory?activityId=100301'
     */
    // 定义 GET 请求路径 armory，用于活动装配
    @RequestMapping(value = "armory", method = RequestMethod.GET)
    // 覆盖接口 IRaffleActivityService 中声明的 armory 方法
    @Override
    public Response<Boolean> armory(@RequestParam Long activityId) {
        try {
            // 记录活动装配开始的日志，包含活动ID
            log.info("活动装配，数据预热，开始 activityId:{}", activityId);
            // 活动装配：将活动 SKU 库存及活动次数预热到缓存
            activityArmory.assembleActivitySkuByActivityId(activityId);
            // 策略装配：将活动对应策略的奖品数据预热到缓存
            strategyArmory.assembleLotteryStrategyByActivityId(activityId);
            // 构建装配成功的响应体
            Response<Boolean> response = Response.<Boolean>builder()
                    // 设置响应码为成功
                    .code(ResponseCode.SUCCESS.getCode())
                    // 设置响应信息为成功
                    .info(ResponseCode.SUCCESS.getInfo())
                    // 设置响应数据为 true，表示装配成功
                    .data(true)
                    // 构建响应对象
                    .build();
            // 记录活动装配完成的日志
            log.info("活动装配 数据预热 完成 activityId:{}", activityId);
            // 返回装配结果
            return response;
        } catch (Exception e) {
            // 记录活动装配失败的日志，包含活动ID和异常堆栈
            log.error("活动装配，数据预热，失败activity:{}", activityId, e);
            // 构建装配失败的响应体
            return Response.<Boolean>builder()
                    // 设置响应码为未知错误
                    .code(ResponseCode.UN_ERROR.getCode())
                    // 设置响应信息为未知错误
                    .info(ResponseCode.UN_ERROR.getInfo())
                    // 构建响应对象（data 为空）
                    .build();
        }
    }

    /**
     * 抽奖接口
     *
     * @param request 请求对象
     * @return 抽奖结果
     * <p>
     * 接口：<a href="http://localhost:8091/api/v1/raffle/activity/draw">/api/v1/raffle/activity/draw</a>
     * 入参：{"activityId":100001,"userId":"xiaofuge"}
     * <p>
     * curl --request POST \
     * --url http://localhost:8091/api/v1/raffle/activity/draw \
     * --header 'content-type: application/json' \
     * --data '{
     * "userId":"xiaofuge",
     * "activityId": 100301
     * }'
     */
    // 定义 POST 请求路径 draw，用于活动抽奖
    @RequestMapping(value = "draw", method = RequestMethod.POST)
    // 覆盖接口 IRaffleActivityService 中声明的 draw 方法
    @Override
    //@RequestBody ActivityDrawRequestDTO request：JSON body 自动反序列化成 DTO
    public Response<ActivityDrawResponseDTO> draw(@RequestBody ActivityDrawRequestDTO request) {
        try {
            // 记录活动抽奖开始的日志，包含用户ID和活动ID
            log.info("活动抽奖 userId:{} activityId:{}", request.getUserId(), request.getActivityId());
            // 1. 参数校验：用户ID 不能为空，活动ID 不能为 null
            if (StringUtils.isBlank(request.getUserId()) || null == request.getActivityId()) {
                // 参数不合法时抛出业务异常
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }

            // 2、参与活动 - 创建参与订单：先扣减活动账户库存并生成抽奖订单
            UserRaffleOrderEntity orderEntity = raffleActivityPartakeService.createOrder(request.getUserId(), request.getActivityId());
            // 记录创建订单完成的日志，包含用户ID、活动ID、订单ID
            log.info("活动抽奖，创建订单 userId:{} activityId:{} orderId:{}", request.getUserId(), request.getActivityId(), orderEntity.getOrderId());
            // 3、抽奖策略 - 执行抽奖：根据订单携带的策略ID执行抽奖
            RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(RaffleFactorEntity.builder()
                    // 设置抽奖因子中的用户ID（取自订单）
                    .userId(orderEntity.getUserId())
                    // 设置抽奖因子中的策略ID（取自订单）
                    .strategyId(orderEntity.getStrategyId())
                    // 设置抽奖因子中的活动结束时间（取自订单，透传到库存扣减链路给锁 Key 设过期时间）
                    .endDateTime(orderEntity.getEndDateTime())
                    // 构建抽奖因子实体
                    .build());
            // 4、存放结果 - 写入中奖记录：组装用户中奖记录实体
            UserAwardRecordEntity userAwardRecordEntity = UserAwardRecordEntity.builder()
                    // 设置中奖用户ID
                    .userId(orderEntity.getUserId())
                    // 设置活动ID
                    .activityId(orderEntity.getActivityId())
                    // 设置策略ID
                    .strategyId(orderEntity.getStrategyId())
                    // 设置订单ID
                    .orderId(orderEntity.getOrderId())
                    // 设置中奖奖品ID
                    .awardId(raffleAwardEntity.getAwardId())
                    // 设置中奖奖品标题
                    .awardTitle(raffleAwardEntity.getAwardTitle())
                    // 设置中奖时间为当前时间
                    .awardTime(new Date())
                    // 设置中奖状态为初始态 create
                    .awardState(AwardStateVO.create)
                    // 构建中奖记录实体
                    .build();
            // 调用奖品服务保存中奖记录
            awardService.saveUserAwardRecord(userAwardRecordEntity);
            // 5、结果返回：构建抽奖成功的响应体
            return Response.<ActivityDrawResponseDTO>builder()
                    // 设置响应码为成功
                    .code(ResponseCode.SUCCESS.getCode())
                    // 设置响应信息为成功
                    .info(ResponseCode.SUCCESS.getInfo())
                    // 设置响应数据为抽奖结果 DTO
                    .data(ActivityDrawResponseDTO.builder()
                            // 设置中奖奖品ID
                            .awardId(raffleAwardEntity.getAwardId())
                            // 设置中奖奖品标题
                            .awardTitle(raffleAwardEntity.getAwardTitle())
                            // 设置中奖奖品排序编号
                            .awardIndex(raffleAwardEntity.getSort())
                            // 构建抽奖结果 DTO
                            .build())
                    // 构建响应对象
                    .build();
        } catch (AppException e) {
            // 记录业务异常日志，包含用户ID、活动ID和异常堆栈
            log.error("活动抽奖失败 userId:{} activityId:{}", request.getUserId(), request.getActivityId(), e);
            // 构建业务异常的响应体
            return Response.<ActivityDrawResponseDTO>builder()
                    // 设置响应码为业务异常码
                    .code(e.getCode())
                    // 设置响应信息为业务异常信息
                    .info(e.getInfo())
                    // 构建响应对象（data 为空）
                    .build();
        } catch (Exception e) {
            // 记录系统异常日志，包含用户ID、活动ID和异常堆栈
            log.error("活动抽奖失败 userId:{} activityId:{}", request.getUserId(), request.getActivityId(), e);
            // 构建系统异常的响应体
            return Response.<ActivityDrawResponseDTO>builder()
                    // 设置响应码为未知错误
                    .code(ResponseCode.UN_ERROR.getCode())
                    // 设置响应信息为未知错误
                    .info(ResponseCode.UN_ERROR.getInfo())
                    // 构建响应对象（data 为空）
                    .build();
        }
    }
}
