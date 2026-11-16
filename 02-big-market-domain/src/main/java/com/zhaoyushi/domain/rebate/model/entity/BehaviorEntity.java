package com.zhaoyushi.domain.rebate.model.entity;

import com.zhaoyushi.domain.rebate.model.valobj.BehaviorTypeVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 行为实体（领域层）
 *
 * <p>描述一次「用户行为动作」的入参实体，作为行为返利服务的入口对象：
 * 上游业务（签到、支付等）在触发行为后构建本实体，传入返利服务完成入账。
 *
 * @author zhaoy
 * @date 2026/11/14
 * @description 行为实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BehaviorEntity {

    /**
     * 用户ID（行为动作的归属用户）
     */
    private String userId;
    /**
     * 行为类型（枚举值对象）
     * sign 签到、openai_pay 支付
     */
    private BehaviorTypeVO behaviorTypeVO;
    /**
     * 业务ID：用于标识本次行为动作的唯一单号
     * 签到则为日期字符串，支付则为外部的业务ID
     */
    private String outBusinessNo;

}
