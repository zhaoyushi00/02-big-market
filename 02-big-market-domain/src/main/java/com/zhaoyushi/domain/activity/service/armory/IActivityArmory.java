// 声明当前接口所属的包路径：领域层 - 活动模块 - 服务 - 装配
package com.zhaoyushi.domain.activity.service.armory;

/**
 * 活动装配预热接口（领域层端口）
 *
 * <p>定义活动 SKU 库存的「预热（armory）」能力：将活动 SKU 库存数量加载到 Redis 缓存，
 * 供后续高并发扣减使用，避免每次扣减都直接访问数据库。
 *
 * @author zhaoy
 * @date 2026/10/27
 * @description 活动装配预热
 */
public interface IActivityArmory {

    /**
     * 按活动 ID 装配（预热）活动 SKU 库存
     * <p>一次性把某个活动下配置的全部 SKU 库存、活动次数以及活动本身信息预热到缓存，
     * 作为活动装配（armory）接口的统一入口。
     *
     * @param activityId 活动ID，用于定位活动下配置的 SKU 集合
     * @return 装配结果，true 表示装配成功
     */
    boolean assembleActivitySkuByActivityId(Long activityId);

    /**
     * 装配（预热）活动 SKU：查询活动 SKU 信息并缓存其库存数量
     *
     * @param sku 商品 SKU 编号
     * @return 装配结果标识（当前实现固定返回 false，预留后续扩展）
     */
    boolean assembleActivitySku(Long sku);

}
