package com.zhaoyushi;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Configurable
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
