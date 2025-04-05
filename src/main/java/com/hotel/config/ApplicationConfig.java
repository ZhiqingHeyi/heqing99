package com.hotel.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;

@Configuration
public class ApplicationConfig {

    // 设置允许循环依赖
    @Bean
    public static SpringApplication springApplication() {
        SpringApplication application = new SpringApplication();
        application.setAllowCircularReferences(true);
        return application;
    }
} 