// 声明当前类所属的包路径：领域层 - 奖品模块 - 值对象
package com.zhaoyushi.domain.award.model.valobj;

// 引入 Lombok 全参构造器注解，自动生成包含所有字段的构造方法
import lombok.AllArgsConstructor;
// 引入 Lombok Getter 注解，自动生成所有字段的 getter 方法
import lombok.Getter;

/**
 * 奖品状态枚举值对象（领域层 - 奖品模块）
 *
 * <p>值对象用于描述对象属性的取值，一个属性可能存在多个离散状态值。
 * 本枚举描述「用户中奖记录」中奖品发放的状态：创建、发奖完成、发奖失败。
 *
 * @author zhaoy
 * @date 2026/11/4
 * @description 奖品状态枚举值对象 [值对象，用于描述对象属性的值，一个对象中，一个属性，有多个状态值。】
 */
// 使用 Lombok 自动生成所有字段的 getter 方法
@Getter
// 使用 Lombok 生成包含全部字段的构造方法（枚举常量即通过它传入 code 与 info）
@AllArgsConstructor
public enum AwardStateVO {

    // 创建状态：中奖记录刚写入，奖品尚未发放
    create("create", "创建"),
    // 发奖完成状态：奖品已成功发放
    complete("complete", "发奖完成"),
    // 发奖失败状态：奖品发放失败
    fail("fail", "发奖失败"),
    ;

    // 状态编码：持久化到数据库的状态值（如 user_award_record 表的 award_state 字段）
    private final String code;
    // 状态说明：状态的中文描述，便于日志与展示
    private final String info;
}
