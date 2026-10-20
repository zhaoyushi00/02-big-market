package com.zhaoyushi.trigger.http;

import com.alibaba.fastjson2.JSON;
import com.zhaoyushi.domain.strategy.model.entity.RaffleAwardEntity;
import com.zhaoyushi.domain.strategy.model.entity.RaffleFactorEntity;
import com.zhaoyushi.domain.strategy.model.entity.StrategyAwardEntity;
import com.zhaoyushi.domain.strategy.service.IRaffleAward;
import com.zhaoyushi.domain.strategy.service.IRaffleStrategy;
import com.zhaoyushi.domain.strategy.service.armory.IStrategyArmory;
import com.zhaoyushi.trigger.api.IRaffleService;
import com.zhaoyushi.trigger.api.dto.RaffleAwardListRequestDTO;
import com.zhaoyushi.trigger.api.dto.RaffleAwardListResponseDTO;
import com.zhaoyushi.trigger.api.dto.RaffleRequestDTO;
import com.zhaoyushi.trigger.api.dto.RaffleResponseDTO;
import com.zhaoyushi.trigger.api.response.Response;
import com.zhaoyushi.types.enums.ResponseCode;
import com.zhaoyushi.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhaoy
 * @date 2026/10/14
 * @description 抽奖服务
 */
// 启用 Lombok 的 SLF4J 日志功能，编译期自动注入 log 日志对象
@Slf4j
// 声明该类为 REST 风格的控制器，方法返回值会序列化为 JSON 响应
@RestController()
// 允许跨域访问，跨域地址从配置文件 app.config.cross-origin 读取
@CrossOrigin("${app.config.cross-origin}")
// 定义该类所有接口的公共请求路径前缀，拼接后形如 /api/v1/raffle/
@RequestMapping("/api/${app.config.api-version}/raffle/")
public class IRaffleController implements IRaffleService {

    // 注入策略装配服务，用于将抽奖策略数据装配到 Redis 缓存
    @Resource
    private IStrategyArmory strategyArmory;

    // 注入抽奖策略服务，用于执行随机抽奖并返回中奖结果
    @Resource
    private IRaffleStrategy raffleStrategy;

    // 注入奖品服务，用于查询奖品配置列表
    @Resource
    private IRaffleAward raffleAward;

    // 定义 GET 请求路径 strategy_armory，用于装配抽奖策略
    @RequestMapping(value = "strategy_armory", method = RequestMethod.GET)
    // 覆盖接口 IRaffleService 中声明的装配方法
    @Override
    public Response<Boolean> strategyArmory(@RequestParam("strategyId") Long strategyId) {

        try {
            // 记录抽奖策略装配开始的日志，包含策略ID
            log.info("抽奖策略装配开启 strategyId:{}", strategyId);
            // 调用领域服务装配抽奖策略，返回装配是否成功
            boolean armoryStatus = strategyArmory.assembleLotteryStrategy(strategyId);
            // 构建装配成功的响应体
            Response<Boolean> response = Response.<Boolean>builder()
                    // 设置响应码为成功
                    .code(ResponseCode.SUCCESS.getCode())
                    // 设置响应信息为成功
                    .info(ResponseCode.SUCCESS.getInfo())
                    // 构建响应对象
                    .build();
            // 记录抽奖策略装配完成的日志，包含响应内容
            log.info("抽奖策略装配完成 strategyId:{} response:{}", strategyId, JSON.toJSONString(response));
            // 返回装配结果
            return response;
        } catch (Exception e) {
            // 记录抽奖策略装配失败的日志，包含策略ID
            log.info("抽奖策略装配失败 strategyId:{}", strategyId);
            // 构建装配失败的响应体
            return Response.<Boolean>builder()
                    // 设置响应码为未知错误
                    .code(ResponseCode.UN_ERROR.getCode())
                    // 设置响应信息为未知错误
                    .info(ResponseCode.UN_ERROR.getInfo())
                    // 构建响应对象
                    .build();
        }
    }

    /**
     * 查询奖品列表
     * <a href="http://localhost:8091/api/v1/raffle/query_raffle_award_list">/api/v1/raffle/query_raffle_award_list</a>
     * 请求参数 raw json
     *
     * @param requestDTO {"strategyId":1000001}
     * @return 奖品列表
     */
    // 定义 POST 请求路径 query_raffle_award_list，用于查询奖品列表
    @RequestMapping(value = "query_raffle_award_list", method = RequestMethod.POST)
    // 覆盖接口 IRaffleService 中声明的查询奖品列表方法
    @Override
    public Response<List<RaffleAwardListResponseDTO>> queryRaffleAwardList(@RequestBody RaffleAwardListRequestDTO requestDTO) {
        try {
            // 记录查询奖品列表开始的日志，包含策略ID
            log.info("查询抽奖奖品列表配开始 strategyId：{}", requestDTO.getStrategyId());
            // 查询奖品配置，根据策略ID获取奖品实体列表
            List<StrategyAwardEntity> strategyAwardEntities = raffleAward.queryRaffleStrategyAwardList(requestDTO.getStrategyId());
            // 初始化奖品列表响应集合，容量与奖品实体数量一致
            List<RaffleAwardListResponseDTO> raffleAwardListResponseDTOS = new ArrayList<>(strategyAwardEntities.size());
            // 遍历奖品实体，逐一转换为响应 DTO
            for (StrategyAwardEntity strategyAward : strategyAwardEntities) {
                // 将奖品实体转换为响应 DTO 并添加到集合中
                raffleAwardListResponseDTOS.add(RaffleAwardListResponseDTO.builder()
                        // 设置奖品ID
                        .awardId(strategyAward.getAwardId())
                        // 设置奖品标题
                        .awardTitle(strategyAward.getAwardTitle())
                        // 设置奖品副标题
                        .awardSubtitle(strategyAward.getAwardSubtitle())
                        // 设置奖品排序，转换为字符串
                        .sort(String.valueOf(strategyAward.getSort()))
                        // 构建响应 DTO 对象
                        .build());
            }
            // 构建查询成功的响应体
            Response<List<RaffleAwardListResponseDTO>> response = Response.<List<RaffleAwardListResponseDTO>>builder()
                    // 设置响应码为成功
                    .code(ResponseCode.SUCCESS.getCode())
                    // 设置响应信息为成功
                    .info(ResponseCode.SUCCESS.getInfo())
                    // 设置响应数据为奖品列表
                    .data(raffleAwardListResponseDTOS)
                    // 构建响应对象
                    .build();
            // 记录查询奖品列表完成的日志，包含响应内容
            log.info("查询抽奖奖品列表配置完成 strategyId：{} response: {}", requestDTO.getStrategyId(), com.alibaba.fastjson.JSON.toJSONString(response));
            // 返回奖品列表查询结果
            return response;
        }catch (Exception e){
            // 记录查询奖品列表失败的日志，包含策略ID和异常堆栈
            log.error("查询抽奖奖品列表配置失败 strategyId：{}", requestDTO.getStrategyId(), e);
            // 构建查询失败的响应体
            return Response.<List<RaffleAwardListResponseDTO>>builder()
                    // 设置响应码为未知错误
                    .code(ResponseCode.UN_ERROR.getCode())
                    // 设置响应信息为未知错误
                    .info(ResponseCode.UN_ERROR.getInfo())
                    // 构建响应对象
                    .build();
        }
    }

    /**
     * 随机抽奖接口
     * <a href="http://localhost:8091/api/v1/raffle/random_raffle">/api/v1/raffle/random_raffle</a>
     *
     * @param requestDTO 请求参数 {"strategyId":1000001}
     * @return 抽奖结果
     */
    // 定义 POST 请求路径 random_raffle，用于执行随机抽奖
    @RequestMapping(value = "random_raffle", method = RequestMethod.POST)
    // 覆盖接口 IRaffleService 中声明的随机抽奖方法
    @Override
    public Response<RaffleResponseDTO> randomRaffle(@RequestBody RaffleRequestDTO requestDTO) {
        try {
            // 记录随机抽奖开始的日志，包含策略ID
            log.info("随机抽奖开始 strategyId: {}", requestDTO.getStrategyId());
            // 调用抽奖接口，执行随机抽奖并返回中奖奖品实体
            RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(RaffleFactorEntity.builder()
                    // 设置固定用户标识
                    .userId("system")
                    // 设置抽奖策略ID
                    .strategyId(requestDTO.getStrategyId())
                    // 构建抽奖因子实体
                    .build());
            // 封装返回结果，构建抽奖成功的响应体
            Response<RaffleResponseDTO> response = Response.<RaffleResponseDTO>builder()
                    // 设置响应码为成功
                    .code(ResponseCode.SUCCESS.getCode())
                    // 设置响应信息为成功
                    .info(ResponseCode.SUCCESS.getInfo())
                    // 设置响应数据为抽奖结果
                    .data(RaffleResponseDTO.builder()
                            // 设置中奖奖品ID
                            .awardId(raffleAwardEntity.getAwardId())
                            // 设置中奖奖品索引
                            .awardIndex(raffleAwardEntity.getSort())
                            // 构建抽奖结果 DTO 对象
                            .build())
                    // 构建响应对象
                    .build();
            // 记录随机抽奖完成的日志，包含响应内容
            log.info("随机抽奖完成 strategyId: {} response: {}", requestDTO.getStrategyId(), com.alibaba.fastjson.JSON.toJSONString(response));
            // 返回抽奖结果
            return response;
        } catch (AppException e) {
            // 记录业务异常日志，包含策略ID和异常信息
            log.error("随机抽奖失败 strategyId：{} {}", requestDTO.getStrategyId(), e.getInfo());
            // 构建业务异常的响应体
            return Response.<RaffleResponseDTO>builder()
                    // 设置响应码为业务异常码
                    .code(e.getCode())
                    // 设置响应信息为业务异常信息
                    .info(e.getInfo())
                    // 构建响应对象
                    .build();
        } catch (Exception e) {
            // 记录系统异常日志，包含策略ID和异常堆栈
            log.error("随机抽奖失败 strategyId：{}", requestDTO.getStrategyId(), e);
            // 构建系统异常的响应体
            return Response.<RaffleResponseDTO>builder()
                    // 设置响应码为未知错误
                    .code(ResponseCode.UN_ERROR.getCode())
                    // 设置响应信息为未知错误
                    .info(ResponseCode.UN_ERROR.getInfo())
                    // 构建响应对象
                    .build();
        }
    }
}
