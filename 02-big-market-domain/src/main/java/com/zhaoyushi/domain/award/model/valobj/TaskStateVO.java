// 声明当前类所属的包路径：领域层 - 奖品模块 - 值对象
package com.zhaoyushi.domain.award.model.valobj;

// 引入 Lombok 通配符导入，自动引入 @Getter、@AllArgsConstructor 等注解
import lombok.*;

/**
 * 任务状态枚举值对象（领域层 - 奖品模块）
 *
 * <p>值对象用于描述对象属性的取值。本枚举描述「消息任务」的发送状态：
 * 创建（待发送）、发送完成、发送失败，用于任务补偿机制的扫描与状态流转。
 *
 * @author zhaoy
 * @date 2026/11/4
 * @description 任务状态枚举值对象
 */
// 使用 Lombok 自动生成所有字段的 getter 方法
@Getter
// 使用 Lombok 生成包含全部字段的构造方法（枚举常量即通过它传入 code 与 desc）
@AllArgsConstructor
public enum TaskStateVO {

    // 创建状态：任务刚创建，消息尚未发送
    create("create", "创建"),
    // 发送完成状态：消息已成功发送
    complete("complete", "发送完成"),
    // 发送失败状态：消息发送失败，等待补偿重试
    fail("fail", "发送失败"),
    ;

    // 状态编码：持久化到数据库的状态值（如 task 表的 state 字段）
    private final String code;
    // 状态描述：状态的中文说明，便于日志与展示
    private final String desc;

}
