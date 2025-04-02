package com.hotel.controller;

import com.hotel.entity.User;
import com.hotel.service.UserService;
import com.hotel.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class ApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");
            String role = credentials.get("role");
            
            System.out.println("API接收到登录请求: username=" + username + ", role=" + role);

            // 简化登录逻辑 - 硬编码方式验证
            boolean isValid = false;
            Long userId = null;
            String userName = null;
            
            // 验证管理员
            if ("admin".equals(username) && "admin123".equals(password) && "admin".equals(role)) {
                isValid = true;
                userId = 1L;
                userName = "系统管理员";
            }
            // 验证前台
            else if ("front".equals(username) && "front123".equals(password) && "receptionist".equals(role)) {
                isValid = true;
                userId = 2L;
                userName = "前台接待";
            }
            // 验证清洁人员
            else if ("cleaner".equals(username) && "cleaner123".equals(password) && "cleaner".equals(role)) {
                isValid = true;
                userId = 3L;
                userName = "清洁人员";
            }
            
            if (isValid) {
                // 生成token
                String token = "token-" + username + "-" + System.currentTimeMillis();
                
                // 构建响应
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "登录成功");
                
                Map<String, Object> data = new HashMap<>();
                data.put("userId", userId);
                data.put("username", username);
                data.put("role", role);
                data.put("name", userName);
                data.put("token", token);
                
                response.put("data", data);
                
                System.out.println("API登录成功，返回数据: " + response);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户名或密码错误");
                System.out.println("API登录失败: " + response);
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "服务器错误: " + e.getMessage());
            System.out.println("API登录异常: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
} 