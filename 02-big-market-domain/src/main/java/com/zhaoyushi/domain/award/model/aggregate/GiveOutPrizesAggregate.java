// 声明当前类所属的包路径：领域层 - 奖品模块 - 聚合
package com.zhaoyushi.domain.award.model.aggregate;

// 引入用户中奖记录实体，作为聚合内部组装发奖记录的对象
import com.zhaoyushi.domain.award.model.entity.UserAwardRecordEntity;
// 引入用户积分奖品实体，作为聚合内部组装积分奖品的对象
import com.zhaoyushi.domain.award.model.entity.UserCreditAwardEntity;
// 引入奖品状态枚举，用于设置发奖记录的初始状态
import com.zhaoyushi.domain.award.model.valobj.AwardStateVO;
// 引入 Lombok @AllArgsConstructor，自动生成包含所有字段的全参构造方法
import lombok.AllArgsConstructor;
// 引入 Lombok @Builder，自动生成建造者模式的链式构造方法
import lombok.Builder;
// 引入 Lombok @Data，自动生成 getter/setter/toString/equals/hashCode 方法
import lombok.Data;
// 引入 Lombok @NoArgsConstructor，自动生成无参构造方法
import lombok.NoArgsConstructor;

// 引入 BigDecimal，作为积分值的精确数值类型
import java.math.BigDecimal;

/**
 * 发放奖品聚合对象（领域层 - 奖品模块）
 *
 * <p>聚合根：将「发奖记录」与「积分奖品」聚合为一个整体，保证发奖过程中
 * 二者在同一个事务内写入，维护发奖数据的一致性。
 * 内部提供静态工厂方法，统一封装发奖对象的组装逻辑，避免各调用方重复编写。
 *
 * @author zhaoy
 * @date 2026/11/22
 * @description 发放奖品聚合对象
 */
// 使用 Lombok 自动生成 getter/setter/toString/equals/hashCode 方法
@Data
// 使用 Lombok 生成建造者模式的链式构造方法
@Builder
// 使用 Lombok 生成包含所有字段的全参构造方法
@AllArgsConstructor
// 使用 Lombok 生成无参构造方法
@NoArgsConstructor
public class GiveOutPrizesAggregate {

    /** 用户ID：发奖目标用户 */
    private String userId;
    /** 用户发奖记录：记录本次发奖的中奖明细 */
    private UserAwardRecordEntity userAwardRecordEntity;
    /** 用户积分奖品：记录本次发放的积分值 */
    private UserCreditAwardEntity userCreditAwardEntity;

    /**
     * 构建发奖记录实体（静态工厂方法）
     *
     * @param userId     用户ID
     * @param orderId    订单ID
     * @param awardId    奖品ID
     * @param awardState 发奖状态
     * @return 组装完成的用户发奖记录实体
     */
    public static UserAwardRecordEntity buildDistributeUserAwardRecordEntity(String userId, String orderId, Integer awardId, AwardStateVO awardState) {
        // 创建一个空的用户发奖记录实体对象
        UserAwardRecordEntity userAwardRecord = new UserAwardRecordEntity();
        // 设置发奖记录的用户ID
        userAwardRecord.setUserId(userId);
        // 设置发奖记录关联的订单ID
        userAwardRecord.setOrderId(orderId);
        // 设置发奖记录对应的奖品ID
        userAwardRecord.setAwardId(awardId);
        // 设置发奖记录的奖品状态
        userAwardRecord.setAwardState(awardState);
        // 返回组装完成的发奖记录实体
        return userAwardRecord;
    }

    /**
     * 构建用户积分奖品实体（静态工厂方法）
     *
     * @param userId       用户ID
     * @param creditAmount 积分值
     * @return 组装完成的用户积分奖品实体
     */
    public static UserCreditAwardEntity buildUserCreditAwardEntity(String userId, BigDecimal creditAmount) {
        // 使用建造者模式链式构建用户积分奖品实体
        return UserCreditAwardEntity.builder()
                // 设置积分奖品所属的用户ID
                .userId(userId)
                // 设置发放的积分值
                .creditAmount(creditAmount)
                // 构建并返回积分奖品实体
                .build();
    }

}
