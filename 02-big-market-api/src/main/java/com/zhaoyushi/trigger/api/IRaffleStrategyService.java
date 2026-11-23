package com.zhaoyushi.trigger.api;

import com.zhaoyushi.trigger.api.dto.*;
import com.zhaoyushi.trigger.api.response.Response;

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

    /**
     * 查询抽奖策略权重规则，给用户展示出抽奖N次后必中奖奖品范围
     *
     * @param request 请求对象
     * @return 权重奖品配置列表「这里会返回全部，前端可按需展示一条已达标的，或者一条要达标的」
     */
    Response<List<RaffleStrategyRuleWeightResponseDTO>> queryRaffleStrategyRuleWeight(RaffleStrategyRuleWeightRequestDTO request);


}
