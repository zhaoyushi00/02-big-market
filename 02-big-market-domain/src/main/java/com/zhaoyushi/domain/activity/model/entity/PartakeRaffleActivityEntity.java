package com.zhaoyushi.domain.activity.model.entity;

// 引入 @AllArgsConstructor，生成包含全部字段的构造器
import lombok.AllArgsConstructor;
// 引入 @Builder，支持以构建者模式创建本对象
import lombok.Builder;
// 引入 @Data，自动生成 getter/setter 等样板方法
import lombok.Data;
// 引入 @NoArgsConstructor，生成无参构造器
import lombok.NoArgsConstructor;

/**
 * 参与抽奖活动的实体对象（入参）
 *
 * <p>作为 {@code IRaffleActivityPartakeService#createOrder} 的入参，
 * 只携带「谁参与 + 参与哪个活动」两个信息，其余数据（账户、订单）由服务内部组装。
 *
 * @author zhaoy
 * @date 2026/11/1
 * @description  参与抽奖活动的实体对象
 */
// 自动生成 getter/setter 等样板方法
@Data
// 支持使用构建者模式创建本对象（配合 createOrder(String,Long) 便捷重载使用）
@Builder
// 生成包含全部字段的全参构造器
@AllArgsConstructor
// 生成无参构造器
@NoArgsConstructor
public class PartakeRaffleActivityEntity {

    /**
     * 用户Id：参与抽奖的主体
     */
    private String userId;

    /**
     * 活动Id：参与的活动编号
     */
    private Long activityId;

}
