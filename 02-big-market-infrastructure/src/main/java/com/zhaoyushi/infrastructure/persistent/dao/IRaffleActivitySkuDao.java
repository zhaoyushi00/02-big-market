package com.zhaoyushi.infrastructure.persistent.dao;

import com.zhaoyushi.infrastructure.persistent.po.RaffleActivitySku;
import org.apache.ibatis.annotations.Mapper;

/**
 * 抽奖活动 sku 表（RaffleActivitySku）数据库访问层
 *
 * <p>对应 mapper：raffle_activity_sku_mapper.xml。
 * 提供按 SKU 查询活动商品信息的数据库访问能力。
 *
 * @author zhaoy
 * @date 2026/10/23
 * @description 商品 sku dao 操作
 */
@Mapper
public interface IRaffleActivitySkuDao {

    /**
     * 根据 SKU 查询活动商品信息
     *
     * @param sku 商品 SKU 编号（活动 + 次数配置组合的唯一标识）
     * @return 抽奖活动 sku 持久化对象
     */
    RaffleActivitySku queryActivitySku(Long sku);

}
