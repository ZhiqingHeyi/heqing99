package com.hotel.config;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class ErrorConfig implements ErrorPageRegistrar {

    @Override
    public void registerErrorPages(ErrorPageRegistry registry) {
        // 将404错误转发到index.html，处理前端路由
        ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/index.html");
        
        registry.addErrorPages(error404Page);
    }
} 