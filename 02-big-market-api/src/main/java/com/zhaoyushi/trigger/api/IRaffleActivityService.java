package com.zhaoyushi.trigger.api;

// 引入活动抽奖请求参数对象，作为 draw 接口的入参
import com.zhaoyushi.trigger.api.dto.ActivityDrawRequestDTO;
// 引入活动抽奖响应结果对象，作为 draw 接口的出参
import com.zhaoyushi.trigger.api.dto.ActivityDrawResponseDTO;
// 引入统一响应包装对象，用于封装接口返回的 code/info/data
import com.zhaoyushi.types.model.Response;

/**
 * 抽奖活动服务接口
 * <p>定义抽奖活动对外暴露的服务能力，包含两个核心接口：
 * <pre>
 * 1. armory - 活动装配（数据预热）：把活动及其关联的 SKU 库存、策略奖品提前加载到缓存；
 * 2. draw   - 活动抽奖：完成「创建订单 -> 策略抽奖 -> 写入中奖记录」的完整抽奖链路。
 * </pre>
 *
 * @author zhaoy
 * @date 2026/11/7
 * @description 抽奖活动服务
 */
public interface IRaffleActivityService {

    /**
     * 活动装配，数据预热缓存
     * <p>将活动配置的 SKU 库存、活动次数以及对应抽奖策略的奖品数据一并预热到 Redis 缓存，
     * 供后续抽奖时高速读取。
     *
     * @param activityId 活动ID，唯一标识一场抽奖活动
     * @return 装配结果，Response.data 为 true 表示装配成功
     */
    Response<Boolean> armory(Long activityId);

    /**
     * 活动抽奖接口
     * <p>用户参与活动抽奖，内部串联「创建参与订单、执行策略抽奖、保存中奖记录」等步骤，
     * 最终返回中奖奖品信息。
     *
     * @param request 请求对象，封装用户ID与活动ID
     * @return 抽奖结果，Response.data 中携带奖品ID、奖品标题与排序编号
     */
    Response<ActivityDrawResponseDTO> draw(ActivityDrawRequestDTO request);
}
