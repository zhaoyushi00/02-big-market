package com.zhaoyushi.domain.rebate.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务状态值对象（领域层）
 *
 * <p>描述发奖消息任务的流转状态，每个枚举项携带：
 * code - 落库状态编码；desc - 中文描述。
 *
 * @author zhaoy
 * @date 2026/11/14
 * @description 任务状态值对象
 */
@Getter
@AllArgsConstructor
public enum TaskStateVO {

    // 创建（已落库，待发送）
    create("create", "创建"),
    // 发送完成
    complete("complete", "发送完成"),
    // 发送失败（等待定时任务补偿重发）
    fail("fail", "发送失败"),
    ;

    // 状态编码（存储/传输值）
    private final String code;
    // 状态中文描述
    private final String desc;

}
