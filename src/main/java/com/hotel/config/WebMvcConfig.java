package com.hotel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 静态资源映射
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 将/admin和/admin/**路径映射到index.html
        registry.addViewController("/admin").setViewName("forward:/index.html");
        registry.addViewController("/admin/login").setViewName("forward:/index.html");
        registry.addViewController("/admin/dashboard").setViewName("forward:/index.html");
        registry.addViewController("/admin/users").setViewName("forward:/index.html");
        registry.addViewController("/admin/staff").setViewName("forward:/index.html");
        registry.addViewController("/admin/reception/bookings").setViewName("forward:/index.html");
        registry.addViewController("/admin/reception/checkin").setViewName("forward:/index.html");
        registry.addViewController("/admin/reception/visitors").setViewName("forward:/index.html");
        registry.addViewController("/admin/cleaning/tasks").setViewName("forward:/index.html");
        registry.addViewController("/admin/cleaning/records").setViewName("forward:/index.html");
    }
} 