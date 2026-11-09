package com.zhaoyushi.trigger.api.dto;

// 引入 @AllArgsConstructor，生成包含全部字段的构造器
import lombok.AllArgsConstructor;
// 引入 @Builder，支持以构建者模式创建对象
import lombok.Builder;
// 引入 @Data，自动生成 getter/setter 等样板方法
import lombok.Data;
// 引入 @NoArgsConstructor，生成无参构造器
import lombok.NoArgsConstructor;

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
public class RaffleStrategyRequestDTO {
    /** 策略ID：唯一标识一套抽奖策略 */
    private Long strategyId;
}
