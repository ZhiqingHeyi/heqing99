package com.hotel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 处理前端页面路由的控制器
 */
@Controller
public class WebController {

    /**
     * 处理注册页面请求
     */
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    /**
     * 处理登录页面请求
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * 处理首页请求
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }
} 