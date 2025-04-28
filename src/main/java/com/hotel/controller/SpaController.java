package com.hotel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    /**
     * 将所有前端路由请求重定向到index.html
     */
    @GetMapping({
        "/admin", 
        "/admin/**"
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
} 