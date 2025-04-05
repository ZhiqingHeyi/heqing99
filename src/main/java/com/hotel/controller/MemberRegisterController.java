package com.hotel.controller;

import com.hotel.entity.User;
import com.hotel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户注册控制器 - 与前端Vue应用的API路径匹配
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MemberRegisterController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册接口
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> registerData) {
        try {
            System.out.println("===== 接收到会员注册请求 =====");
            System.out.println("请求数据: " + registerData);
            
            // 创建用户对象
            User user = new User();
            user.setUsername(registerData.get("username"));
            user.setPassword(registerData.get("password"));
            user.setName(registerData.get("realName"));
            user.setPhone(registerData.get("phone"));
            user.setEmail(registerData.get("email")); // 如果前端提交了邮箱
            
            // 设置为普通用户角色
            user.setRole(User.UserRole.CUSTOMER);
            
            System.out.println("创建的用户对象: " + user);
            
            // 调用用户服务进行注册
            User registeredUser = userService.register(user);
            
            System.out.println("注册成功，返回的用户: " + registeredUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("userId", registeredUser.getId());
            response.put("username", registeredUser.getUsername());
            
            System.out.println("会员注册成功, userId: " + registeredUser.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("===== 会员注册失败 =====");
            System.err.println("异常类型: " + e.getClass().getName());
            System.err.println("异常消息: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "注册失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        try {
            System.out.println("检查用户名是否可用: " + username);
            boolean exists = userService.getUserByUsername(username) != null;
            
            Map<String, Object> response = new HashMap<>();
            response.put("available", !exists);
            
            System.out.println("用户名 " + username + " 可用性: " + !exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 如果是找不到用户的异常，说明用户名可用
            System.out.println("检查用户名发生异常，默认用户名可用: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("available", true);
            return ResponseEntity.ok(response);
        }
    }
} 