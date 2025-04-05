package com.hotel.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * 控制器配置类
 * 用于禁用特定的控制器，以便在开发过程中绕过编译错误
 */
@Configuration
public class ControllerConfig {
    
    /**
     * 禁用会员营销和分析相关的控制器
     */
    @Configuration
    @ConditionalOnProperty(name = "app.member-marketing-controllers.enabled", havingValue = "false", matchIfMissing = true)
    @ComponentScan(
        basePackages = "com.hotel.controller",
        excludeFilters = {
            @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = {
                    "com.hotel.controller.Membership.*Controller",
                    "com.hotel.controller.PointsExpiryController",
                    "com.hotel.controller.MemberStatsController"
                }
            )
        }
    )
    public static class MemberMarketingControllerConfig {
        // 空配置类，仅用于禁用控制器
    }
} 