// 声明当前类所属的包路径：领域层 - 奖品模块 - 值对象
package com.zhaoyushi.domain.award.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账户状态枚举（领域层 - 奖品模块）
 *
 * <p>描述「用户积分账户」的可用状态，用于控制积分账户是否可以正常入账/扣减。
 * 当前支持：开启（正常使用）、冻结（禁止操作）。
 *
 * @author zhaoy
 * @date 2026/11/22
 * @description 账户状态枚举
 */
// 使用 Lombok 自动生成所有字段的 getter 方法
@Getter
// 使用 Lombok 生成包含全部字段的构造方法（枚举常量即通过它传入 code 与 desc）
@AllArgsConstructor
public enum AccountStatusVO {

    // 开启状态：账户可正常使用
    open("open", "开启"),
    // 冻结状态：账户被冻结，禁止积分操作
    close("close", "冻结"),
    ;

    // 状态编码：持久化到数据库的状态值（如 user_credit_account 表的 account_status 字段）
    private final String code;
    // 状态说明：状态的中文描述，便于日志与展示
    private final String desc;

}
