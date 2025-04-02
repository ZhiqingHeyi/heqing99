package com.hotel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    /**
     * 为前端SPA的路由提供支持，将管理系统路由请求转发到index.html
     */
    @GetMapping({
        "/admin",
        "/admin/login",
        "/admin/dashboard",
        "/admin/users",
        "/admin/staff",
        "/admin/reception/bookings",
        "/admin/reception/checkin",
        "/admin/reception/visitors",
        "/admin/cleaning/tasks",
        "/admin/cleaning/records"
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
} 