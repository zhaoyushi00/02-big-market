// 声明当前类所属的包路径：领域层 - 活动模块 - 模型 - 值对象
package com.zhaoyushi.domain.activity.model.valobj;

// 引入 Lombok 全参构造器注解，自动生成包含所有字段的构造方法
import lombok.AllArgsConstructor;
// 引入 Lombok Builder 注解，支持以建造者模式链式创建对象
import lombok.Builder;
// 引入 Lombok Data 注解，自动生成 getter/setter/toString/equals/hashCode
import lombok.Data;
// 引入 Lombok 无参构造器注解，自动生成无参构造方法
import lombok.NoArgsConstructor;

/**
 * 活动 SKU 库存 Key 值对象
 *
 * <p>作为活动 SKU 库存扣减时，写入 Redis 延迟队列/阻塞队列的元素载体，
 * 记录「哪个商品 SKU 属于哪个活动」，供后续定时任务异步更新数据库库存时定位目标记录。
 *
 * @author zhaoy
 * @date 2026/10/28
 * @description 活动sku库存 key 值对象
 */
// 使用 Lombok 自动生成 getter/setter/toString/equals/hashCode 等方法
@Data
// 使用 Lombok 生成建造者模式，支持 ActivitySkuStockKeyVO.builder().sku(...).build() 链式构建
@Builder
// 使用 Lombok 生成包含全部字段的构造方法
@AllArgsConstructor
// 使用 Lombok 生成无参构造方法
@NoArgsConstructor
public class ActivitySkuStockKeyVO {

    /** 商品 SKU 编号（活动 + 次数配置组合的唯一标识），用于定位具体活动商品 */
    private Long sku;
    /** 活动ID，用于关联活动信息（如活动结束时间等） */
    private Long activityId;
}
