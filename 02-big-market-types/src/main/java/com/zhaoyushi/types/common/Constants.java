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
    }
}
