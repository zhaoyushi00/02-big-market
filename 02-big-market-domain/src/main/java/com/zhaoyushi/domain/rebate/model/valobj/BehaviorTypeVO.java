package com.zhaoyushi.domain.rebate.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 行为类型枚举值对象（领域层）
 *
 * <p>限定可触发返利的行为种类，每个枚举项携带：
 * code - 落库/传输使用的类型编码；info - 中文描述。
 *
 * @author zhaoy
 * @date 2026/11/14
 * @description 行为类型枚举值对象
 */
@Getter
@AllArgsConstructor
public enum BehaviorTypeVO {

    // 签到（日历）行为
    SIGN("sign", "签到（日历）"),
    // openai 外部支付完成行为
    OPENAI_PAY("openai_pay", "openai 外部支付完成"),
    ;

    // 行为类型编码（存储/传输值）
    private final String code;
    // 行为类型中文描述
    private final String info;

}
