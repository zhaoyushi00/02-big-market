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
 * 活动抽奖响应对象
 * <p>作为活动抽奖接口（draw）的出参 DTO，封装中奖奖品的核心信息返回给调用方。
 *
 * @author zhaoy
 * @date 2026/11/7
 * @description 活动抽奖响应对象
 */
// 自动生成 getter/setter 等样板方法
@Data
// 支持使用构建者模式创建本对象
@Builder
// 生成包含全部字段的全参构造器
@AllArgsConstructor
// 生成无参构造器
@NoArgsConstructor
public class ActivityDrawResponseDTO {
    // 奖品ID：中奖奖品的唯一标识
    private Integer awardId;
    // 奖品标题：中奖奖品的名称/描述
    private String awardTitle;
    // 排序编号【策略奖品配置的奖品顺序编号】：奖品在策略中的展示顺序
    private Integer awardIndex;
}
