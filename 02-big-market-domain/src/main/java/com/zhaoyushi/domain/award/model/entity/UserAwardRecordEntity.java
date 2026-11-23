// 声明当前类所属的包路径：领域层 - 奖品模块 - 实体对象
package com.zhaoyushi.domain.award.model.entity;

import com.zhaoyushi.domain.award.model.valobj.AwardStateVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户中奖记录实体对象（领域层 - 奖品模块）
 *
 * <p>描述一条用户中奖记录的核心信息：谁（userId）在哪个活动（activityId）
 * 通过哪个策略（strategyId）中奖，关联的订单号（orderId，用于幂等）、
 * 中奖的奖品（awardId/awardTitle）、中奖时间（awardTime）以及奖品发放状态（awardState）。
 *
 * @author zhaoy
 * @date 2026/11/4
 * @description 用户中奖记录实体对象
 */
// 使用 Lombok 自动生成 getter/setter/toString/equals/hashCode 等方法
@Data
// 使用 Lombok 生成无参构造方法
@NoArgsConstructor
// 使用 Lombok 生成包含全部字段的构造方法
@AllArgsConstructor
// 使用 Lombok 生成建造者模式，支持链式构建
@Builder
public class UserAwardRecordEntity {
    // 用户ID：中奖用户，同时也是分库分表路由键
    private String userId;
    // 活动ID：用户参与的活动编号
    private Long activityId;
    // 抽奖策略ID：本次抽奖所使用的策略编号
    private Long strategyId;
    // 抽奖订单ID：唯一标识一次抽奖，作为幂等键防止重复写入
    private String orderId;
    // 奖品ID：中奖的奖品编号
    private Integer awardId;
    // 奖品标题（名称）：中奖奖品的中文名称
    private String awardTitle;
    // 中奖时间：用户中奖的时间点
    private Date awardTime;
    // 奖品状态：create-创建、complete-发奖完成、fail-发奖失败
    private AwardStateVO awardState;
    /** 奖品配置信息；发奖的时候，可以根据配置（如积分范围）生成对应奖品 */
    private String awardConfig;
}
