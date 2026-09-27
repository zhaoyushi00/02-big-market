package com.zhaoyushi.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class GuavaConfig {

    /**
     * 创建 Guava 本地缓存 Bean
     * <p>写入后 3 秒过期，用于短时缓存（如接口防重、临时数据）。
     *
     * @return Guava 缓存实例
     */
    @Bean(name = "cache")
    public Cache<String, String> cache() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(3, TimeUnit.SECONDS)
                .build();
    }

}
