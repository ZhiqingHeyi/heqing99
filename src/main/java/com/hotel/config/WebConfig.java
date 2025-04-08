package com.hotel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 添加全局CORS配置
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }

    /**
     * CORS过滤器，确保前端请求能被正确处理
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许前端Vue应用域名
        config.addAllowedOrigin("http://localhost:3000");
        // 允许前端应用的IP地址
        config.addAllowedOrigin("http://127.0.0.1:3000");
        
        // 允许凭证，如Cookie
        config.setAllowCredentials(true);
        // 允许所有头信息
        config.addAllowedHeader("*");
        // 允许所有请求方法
        config.addAllowedMethod("*");
        // 设置响应暴露的头信息
        config.addExposedHeader("Authorization");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        // 打印调试信息
        System.out.println("=== CORS配置已加载 ===");
        System.out.println("允许的源: " + config.getAllowedOrigins());
        System.out.println("允许的方法: " + config.getAllowedMethods());
        
        return new CorsFilter(source);
    }
} 