package com.zhaoyushi.trigger.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 抽奖策略请求参数
 * <p>由原抽奖请求参数（RaffleRequestDTO）重命名而来，作为策略维度随机抽奖接口的入参。
 *
 * @author zhaoy
 * @date 2026/10/14
 * @description 抽奖请求参数
 */
// 自动生成 getter/setter 等样板方法
@Data
// 生成包含全部字段的全参构造器
@AllArgsConstructor
// 生成无参构造器
@NoArgsConstructor
// 支持使用构建者模式创建本对象
@Builder
// 实现 Serializable：作为 Dubbo RPC 接口的入参，需支持 JDK 序列化以在网络间传输
public class RaffleStrategyRequestDTO implements Serializable {
    /** 策略ID：唯一标识一套抽奖策略 */
    private Long strategyId;
}
