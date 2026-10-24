package com.zhaoyushi.domain.activity.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 活动状态枚举值对象
 *
 * <p>用于描述活动实体 ActivityEntity 的状态属性，是值对象（无生命周期、不影响数据库操作）。
 * 活动生命周期：create(创建) → open(开启) → close(关闭)。
 *
 * @author zhaoy
 * @date 2026/10/24
 * @description 活动状态值对象
 */
@Getter
@AllArgsConstructor
public enum ActivityStateVO {

    /** 创建：活动刚创建，尚未对外开启 */
    create("create", "创建"),
    /** 开启：活动进行中，允许用户参与 */
    open("open", "开启"),
    /** 关闭：活动已结束/下线，禁止参与 */
    close("close", "关闭"),
    ;

    /** 状态编码：存入数据库的状态值 */
    private final String code;
    /** 状态描述：状态的中文说明 */
    private final String desc;

}
