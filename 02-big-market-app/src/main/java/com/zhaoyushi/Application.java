package com.zhaoyushi;

import cn.bugstack.middleware.db.router.config.DataSourceAutoConfig;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Configurable
@EnableScheduling
// db-router-spring-boot-starter 1.0.2 面向 Spring Boot 2.x，其自动装配依赖已废弃的 spring.factories，
// 在 Spring Boot 3.x 下不会被加载，导致数据源 bean 缺失；这里显式导入其配置类，确保动态数据源被创建。
@Import(DataSourceAutoConfig.class)
public class Application {

    /**
     * 应用启动入口：启动 Spring Boot 应用
     *
     * @param args 启动参数
     */
    public static void main(String[] args){
        SpringApplication.run(Application.class);
    }

}
