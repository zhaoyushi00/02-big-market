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
 * 抽奖策略应答结果
 * <p>由原抽奖应答结果（RaffleResponseDTO）重命名而来，作为策略维度随机抽奖接口的出参。
 *
 * @author zhaoy
 * @date 2026/10/14
 * @description 抽奖应答结果
 */
// 自动生成 getter/setter 等样板方法
@Data
// 生成包含全部字段的全参构造器
@AllArgsConstructor
// 生成无参构造器
@NoArgsConstructor
// 支持使用构建者模式创建本对象
@Builder
public class RaffleStrategyResponseDTO {

    // 奖品ID：随机抽奖命中的奖品唯一标识
    private Integer awardId;
    // 排序编号【策略奖品配置的奖品顺序编号】：奖品在策略中的展示顺序
    private Integer awardIndex;

}
