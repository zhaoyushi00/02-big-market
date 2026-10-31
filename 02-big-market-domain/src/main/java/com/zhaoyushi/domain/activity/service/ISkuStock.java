// 声明当前接口所属的包路径：领域层 - 活动模块 - 服务
package com.zhaoyushi.domain.activity.service;

// 引入活动 SKU 库存 Key 值对象，作为队列元素类型与返回值类型
import com.zhaoyushi.domain.activity.model.valobj.ActivitySkuStockKeyVO;

/**
 * 活动 SKU 库存处理接口（领域层端口）
 *
 * <p>定义活动 SKU 库存一致性相关的能力契约，由领域服务 {@link RaffleActivityService} 实现，
 * 供触发器层（定时任务、MQ 监听器）调用。主要提供：
 * 1. 从延迟队列取库存扣减记录（异步落库用）；
 * 2. 清空队列；
 * 3. 更新/清空数据库库存。
 *
 * @author zhaoy
 * @date 2026/10/28
 * @description 活动sku库存处理接口
 */
public interface ISkuStock {

    /**
     * 获取活动 sku 库存消耗队列（阻塞队列）
     *
     * @return 奖品库存 Key 信息（含 sku、activityId），队列为空时返回 null
     * @throws InterruptedException 异常
     */
    ActivitySkuStockKeyVO takeQueueValue() throws InterruptedException;

    /**
     * 清空队列（库存耗尽后，不再需要延迟更新数据库记录，直接清空队列）
     */
    void clearQueueValue();

    /**
     * 延迟队列 + 定时任务：趋势更新活动 sku 库存（每次扣减 1 件，降低数据库更新频次）
     *
     * @param sku 活动商品 SKU 编号
     */
    void updateActivitySkuStock(Long sku);

    /**
     * 缓存库存已消耗完毕，清空数据库库存（直接将剩余库存置为 0）
     *
     * @param sku 活动商品 SKU 编号
     */
    void clearActivitySkuStock(Long sku);

}
