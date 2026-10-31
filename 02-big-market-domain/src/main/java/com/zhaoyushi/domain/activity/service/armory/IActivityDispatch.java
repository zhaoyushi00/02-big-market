// 声明当前接口所属的包路径：领域层 - 活动模块 - 服务 - 调度
package com.zhaoyushi.domain.activity.service.armory;

// 引入 java.util.Date，作为活动结束时间参数类型
import java.util.Date;

/**
 * 活动调度接口（领域层端口）
 *
 * <p>定义活动 SKU 库存的「调度（dispatch）」能力：扣减活动 SKU 的缓存库存。
 * 与预热接口 {@link IActivityArmory} 共同组成活动库存的两阶段：先预热、后调度扣减。
 *
 * @author zhaoy
 * @date 2026/10/27
 * @description 活动调度【扣减库存】
 */
public interface IActivityDispatch {

    /**
     * 根据策略ID和奖品ID，扣减奖品缓存库存
     *
     * @param sku         互动 SKU 编号
     * @param endDateTime 活动结束时间，用于设置库存锁 key 的过期时间（结束时间 + 1 天）
     * @return 扣减结果：扣减成功返回 true，库存耗尽/不足返回 false
     */
    boolean subtractionActivitySkuStock(Long sku, Date endDateTime);

}
