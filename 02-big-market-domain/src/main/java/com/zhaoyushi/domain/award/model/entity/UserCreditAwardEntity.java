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

// 引入 BigDecimal，作为积分值的精确数值类型
import java.math.BigDecimal;

/**
 * 用户积分奖品实体对象（领域层 - 奖品模块）
 *
 * <p>描述一次积分发奖中用户获得的积分值，配合「用户积分账户」完成积分入账。
 * 积分值使用 BigDecimal 以保证精度，避免浮点运算误差。
 *
 * @author zhaoy
 * @date 2026/11/22
 * @description 用户积分奖品实体对象
 */
// 使用 Lombok 自动生成 getter/setter/toString/equals/hashCode 方法
@Data
// 使用 Lombok 生成建造者模式的链式构造方法
@Builder
// 使用 Lombok 生成包含所有字段的全参构造方法
@AllArgsConstructor
// 使用 Lombok 生成无参构造方法
@NoArgsConstructor
public class UserCreditAwardEntity {

    /** 用户ID：积分归属的用户 */
    private String userId;
    /** 积分值：本次发放的积分数量（精确数值） */
    private BigDecimal creditAmount;

}
