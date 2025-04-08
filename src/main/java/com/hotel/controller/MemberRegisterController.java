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
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, allowedHeaders = "*")
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
            
            // 验证必要的字段
            if (registerData == null) {
                throw new RuntimeException("注册数据为空");
            }
            
            if (registerData.get("username") == null || registerData.get("username").trim().isEmpty()) {
                throw new RuntimeException("用户名不能为空");
            }
            
            if (registerData.get("password") == null || registerData.get("password").trim().isEmpty()) {
                throw new RuntimeException("密码不能为空");
            }
            
            if (registerData.get("phone") == null || registerData.get("phone").trim().isEmpty()) {
                throw new RuntimeException("手机号码不能为空");
            }
            
            // 获取密码和确认密码
            String password = registerData.get("password");
            
            // 创建用户对象
            User user = new User();
            user.setUsername(registerData.get("username"));
            user.setPassword(password);  // 直接使用密码而不验证确认密码
            user.setName(registerData.get("realName") != null ? registerData.get("realName") : registerData.get("username"));
            user.setPhone(registerData.get("phone"));
            user.setEmail(registerData.get("email")); // 如果前端提交了邮箱
            
            // 设置为普通用户角色，确保与枚举值完全匹配
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
    
    /**
     * 检查手机号是否已被注册
     */
    @GetMapping("/check-phone")
    public ResponseEntity<?> checkPhone(@RequestParam String phone) {
        try {
            System.out.println("检查手机号是否已被注册: " + phone);
            
            // 这里需要在UserService中添加相应的方法
            boolean exists = userService.existsByPhone(phone);
            
            Map<String, Object> response = new HashMap<>();
            response.put("available", !exists);
            
            System.out.println("手机号 " + phone + " 可用性: " + !exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("检查手机号发生异常: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("available", true);
            return ResponseEntity.ok(response);
        }
    }
} 