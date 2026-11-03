// 声明当前类所属的包路径：触发器层 - 定时任务
package com.zhaoyushi.trigger.job;

// 引入活动 SKU 库存 Key 值对象，作为队列取出的元素类型
import com.zhaoyushi.domain.activity.model.valobj.ActivitySkuStockKeyVO;
// 引入活动 SKU 库存处理接口，提供取队列与更新库存能力
import com.zhaoyushi.domain.activity.service.IRaffleActivitySkuStockService;
// 引入 JSR-250 的 @Resource 注解，用于按名称注入依赖
import jakarta.annotation.Resource;
// 引入 Lombok 的 @Slf4j 注解，自动生成日志对象 log
import lombok.extern.slf4j.Slf4j;
// 引入 @Scheduled 注解，用于声明定时任务执行方法
import org.springframework.scheduling.annotation.Scheduled;
// 引入 @Component 注解，将本类注册为 Spring Bean
import org.springframework.stereotype.Component;

/**
 * 更新活动 SKU 库存定时任务
 *
 * <p>周期性从 Redis 延迟队列中取出库存扣减记录，并异步更新数据库库存。
 * 通过「延迟队列 + 定时批量更新」的方式，降低对数据库的更新频次，避免高并发下直接写库产生竞争。
 *
 * @author zhaoy
 * @description 更新活动sku库存任务
 * @create 2026-10-28
 */
// 使用 Lombok 生成日志对象 log
@Slf4j
// 注册为 Spring 组件
@Component()
public class UpdateActivitySkuStockJob {

    // 注入活动 SKU 库存处理服务，提供取队列与更新库存的能力
    @Resource
    private IRaffleActivitySkuStockService skuStock;

    /**
     * 定时任务执行方法
     * <p>每 5 秒执行一次：从延迟队列取一条库存扣减记录，若不为空则更新数据库库存。
     */
    // cron 表达式：每 5 秒执行一次（秒 分 时 日 月 周）
    @Scheduled(cron = "0/5 * * * * ?")
    public void exec() {
        // 使用 try-catch 包裹，保证任务异常不中断后续调度
        try {
            // 记录任务开始日志：说明采用延迟队列获取，降低数据库更新频次、避免竞争
            log.info("定时任务，更新活动sku库存【延迟队列获取，降低对数据库的更新频次，不要产生竞争】");
            // 从延迟队列取出一个库存扣减记录（队列为空时返回 null）
            ActivitySkuStockKeyVO activitySkuStockKeyVO = skuStock.takeQueueValue();
            // 队列为空则直接返回，等待下一次调度
            if (null == activitySkuStockKeyVO) return;
            // 记录即将更新的 SKU 与活动 ID 日志
            log.info("定时任务，更新活动sku库存 sku:{} activityId:{}", activitySkuStockKeyVO.getSku(), activitySkuStockKeyVO.getActivityId());
            // 更新数据库库存（stock_count_surplus 减 1）
            skuStock.updateActivitySkuStock(activitySkuStockKeyVO.getSku());
        } catch (Exception e) {
            // 记录任务执行失败日志（含异常堆栈）
            log.error("定时任务，更新活动sku库存失败", e);
        }
    }

}
