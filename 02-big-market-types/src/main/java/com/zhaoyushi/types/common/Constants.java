package com.zhaoyushi.types.common;

/**
 * 全局常量
 * <p>存放抽奖模块通用的分隔符常量，以及 Redis Key 的前缀。
 */
public class Constants {

    /** 分隔符：逗号，用于拆分多个规则/奖品列表 */
    public final static String SPLIT = ",";
    /** 分隔符：冒号，用于拆分「档位:奖品列表」 */
    public final static String COLON = ":";
    /** 分隔符：空格，用于拆分多个权重档位 */
    public final static String SPACE = " ";
    /** 分隔符：下划线，用于拼接复合 key（如 策略ID_权重档位） */
    public final static String UNDERLINE = "_";

    /** Redis Key 前缀常量 */
    public static class RedisKey {
        /** 策略信息缓存 key 前缀 */
        public static String STRATEGY_KEY = "big_market_strategy_key_";
        /** 策略奖品列表缓存 key 前缀 */
        public static String STRATEGY_AWARD_KEY = "big_market_strategy_award_key_";
        /** 策略概率查找表缓存 key 前缀 */
        public static String STRATEGY_RATE_TABLE_KEY = "big_market_strategy_rate_table_key_";
        /** 策略概率范围缓存 key 前缀 */
        public static String STRATEGY_RATE_RANGE_KEY = "big_market_strategy_rate_range_key_";
        /** 规则树缓存 key 前缀 */
        public static String RULE_TREE_VO_KEY = "rule_tree_vo_key_";
        /** 奖品库存缓存 key 前缀，用于存储并扣减奖品库存数量（完整 key 形如：strategy_award_count_key_策略ID_奖品ID） */
        public static String STRATEGY_AWARD_COUNT_KEY = "strategy_award_count_key_";
        public static String STRATEGY_AWARD_COUNT_QUERY_KEY = "strategy_award_count_query_key";
    }
}
