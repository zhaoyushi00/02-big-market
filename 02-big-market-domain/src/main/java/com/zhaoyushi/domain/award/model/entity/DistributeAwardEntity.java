// 声明当前类所属的包路径：领域层 - 奖品模块 - 实体
package com.zhaoyushi.domain.award.model.entity;

// 引入 Lombok @AllArgsConstructor，自动生成包含所有字段的全参构造方法
import lombok.AllArgsConstructor;
// 引入 Lombok @Builder，自动生成建造者模式的链式构造方法
import lombok.Builder;
// 引入 Lombok @Data，自动生成 getter/setter/toString/equals/hashCode 方法
import lombok.Data;
// 引入 Lombok @NoArgsConstructor，自动生成无参构造方法
import lombok.NoArgsConstructor;

/**
 * 分发奖品实体对象（领域层 - 奖品模块）
 *
 * <p>作为「发奖」动作的入参载体，承载一次发奖所需的全部信息：
 * 用户、订单、奖品以及奖品配置（如积分范围）。
 * 由触发器层（消息消费者）组装，传入领域层的发奖服务。
 *
 * @author zhaoy
 * @date 2026/11/22
 * @description 分发奖品实体对象
 */
// 使用 Lombok 自动生成 getter/setter/toString/equals/hashCode 方法
@Data
// 使用 Lombok 生成建造者模式的链式构造方法
@Builder
// 使用 Lombok 生成无参构造方法
@NoArgsConstructor
// 使用 Lombok 生成包含所有字段的全参构造方法
@AllArgsConstructor
public class DistributeAwardEntity {

    /**
     * 用户ID：发奖目标用户
     */
    private String userId;
    /**
     * 订单ID：发奖关联的抽奖订单
     */
    private String orderId;
    /**
     * 奖品ID：要发放的奖品标识
     */
    private Integer awardId;
    /**
     * 奖品配置信息：如积分范围「0.01,1」，发奖时据此生成随机积分值
     */
    private String awardConfig;

}
