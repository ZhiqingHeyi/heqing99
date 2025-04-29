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
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/login")
    public ResponseEntity<?> loginPage() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "请登录");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");
            String role = credentials.get("role");
            
            System.out.println("接收到登录请求: username=" + username + ", password=" + password + ", role=" + role);

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
                // 生成JWT token
                String token = JwtUtil.generateToken(username, role);
                
                // 生成refresh token
                String refreshToken = JwtUtil.generateRefreshToken(username, role);
                
                // 获取token过期时间（秒）
                long expiresIn = JwtUtil.getExpirationDuration();
                
                // 构建响应
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("code", 200);
                response.put("message", "登录成功");
                
                Map<String, Object> data = new HashMap<>();
                data.put("userId", userId);
                data.put("username", username);
                data.put("role", role);
                data.put("name", userName);
                data.put("token", token);
                data.put("refreshToken", refreshToken);
                data.put("expiresIn", expiresIn);
                
                Map<String, Object> staffInfo = new HashMap<>();
                staffInfo.put("id", userId.toString());
                staffInfo.put("username", username);
                staffInfo.put("realName", userName);
                staffInfo.put("role", role);
                staffInfo.put("phone", "");
                staffInfo.put("email", "");
                staffInfo.put("department", role.equals("admin") ? "管理部" : (role.equals("receptionist") ? "前台部" : "清洁部"));
                
                data.put("staffInfo", staffInfo);
                
                response.put("data", data);
                
                System.out.println("登录成功，返回数据: " + response);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("code", 401);
                response.put("message", "用户名或密码错误");
                System.out.println("登录失败: " + response);
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", 500);
            response.put("message", "服务器错误: " + e.getMessage());
            System.out.println("登录异常: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}