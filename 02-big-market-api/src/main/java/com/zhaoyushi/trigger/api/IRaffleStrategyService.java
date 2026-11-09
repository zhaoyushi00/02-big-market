package com.zhaoyushi.trigger.api;

// 通配引入 dto 包下全部请求/响应对象，减少逐个 import 的重复代码
import com.zhaoyushi.trigger.api.dto.*;
// 引入统一响应包装对象，用于封装接口返回的 code/info/data
import com.zhaoyushi.trigger.api.response.Response;

// 引入集合类型，作为查询奖品列表接口的返回类型
import java.util.List;

/**
 * 抽奖策略服务接口
 * <p>由原抽奖服务接口（IRaffleService）重命名而来，职责聚焦于「策略维度」的抽奖能力，
 * 与新增的活动维度抽奖接口（IRaffleActivityService）区分开。
 *
 * @author zhaoy
 * @date 2026/10/13
 * @description 抽奖服务接口
 */
public interface IRaffleStrategyService {

    /**
     * 策略装配接口
     * <p>将指定策略的奖品数据与概率装配进内存/缓存，供后续随机抽奖使用。
     *
     * @param strategyId 策略ID，唯一标识一套抽奖策略
     * @return 装配结果，Response.data 为 true 表示装配成功
     */
    Response<Boolean> strategyArmory(Long strategyId);

    /**
     * 查询奖品列表配置
     * <p>根据请求参数查询某个策略下配置的奖品列表及其概率信息。
     *
     * @param requestDTO 抽奖奖品列表查询请求参数
     * @return 奖品列表数据
     */
    Response<List<RaffleAwardListResponseDTO>> queryRaffleAwardList(RaffleAwardListRequestDTO requestDTO);

    /**
     * 随机抽奖接口
     * <p>根据策略执行一次随机抽奖，返回命中的奖品信息。
     *
     * @param requestDTO 请求参数，携带策略ID
     * @return 抽奖结果，Response.data 中携带奖品ID与排序编号
     */
    Response<RaffleStrategyResponseDTO> randomRaffle(RaffleStrategyRequestDTO requestDTO);


}
