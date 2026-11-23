// 声明当前类所属的包路径：基础设施层 - 持久化 - PO
package com.zhaoyushi.infrastructure.persistent.po;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户积分账户持久化对象（基础设施层 - 持久化）
 *
 * <p>对应数据库表 user_credit_account，用于保存用户累计获得的积分。
 * 采用「总积分 + 可用积分」双字段设计：总积分记录累计获得，可用积分随扣减而减少，
 * 为后续积分消费/兑换场景预留空间。
 *
 * @author zhaoy
 * @date 2026/11/22
 * @description 用户积分账户
 */
// 使用 Lombok 自动生成 getter/setter/toString/equals/hashCode 方法
@Data
public class UserCreditAccount {

    /** 自增ID：主键 */
    private Long id;
    /** 用户ID：积分账户归属的用户 */
    private String userId;
    /** 总积分：显示总账户值，记录一个人获得的总积分 */
    private BigDecimal totalAmount;
    /** 可用积分：每次扣减的值 */
    private BigDecimal availableAmount;
    /** 账户状态【open - 可用，close - 冻结】 */
    private String accountStatus;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;

}
