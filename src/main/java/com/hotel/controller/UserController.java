package com.hotel.controller;

import com.hotel.entity.User;
import com.hotel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            System.out.println("接收到用户注册请求: " + user.getUsername());
            User registeredUser = userService.register(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("userId", registeredUser.getId());
            response.put("username", registeredUser.getUsername());
            
            System.out.println("用户注册成功, userId: " + registeredUser.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("用户注册失败: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "注册失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            System.out.println("接收到用户登录请求: " + credentials.get("username"));
            String token = userService.authenticate(
                    credentials.get("username"),
                    credentials.get("password")
            );
            
            // 获取用户信息
            User user = userService.getUserByUsername(credentials.get("username"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", token);
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("name", user.getName());
            response.put("role", user.getRole().toString());
            
            System.out.println("用户登录成功, userId: " + user.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("用户登录失败: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "登录失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            System.out.println("接收到获取用户信息请求, userId: " + id);
            User user = userService.getUserById(id);
            
            // 移除敏感信息
            user.setPassword(null);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", user);
            
            System.out.println("返回用户信息成功, userId: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("获取用户信息失败: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取用户信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        try {
            System.out.println("接收到获取用户信息请求, username: " + username);
            User user = userService.getUserByUsername(username);
            
            // 移除敏感信息
            user.setPassword(null);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", user);
            
            System.out.println("返回用户信息成功, username: " + username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("获取用户信息失败: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取用户信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            System.out.println("接收到更新用户信息请求, userId: " + id);
            user.setId(id);
            User updatedUser = userService.updateUser(user);
            
            // 移除敏感信息
            updatedUser.setPassword(null);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "用户信息更新成功");
            response.put("data", updatedUser);
            
            System.out.println("用户信息更新成功, userId: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("更新用户信息失败: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新用户信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            System.out.println("接收到删除用户请求, userId: " + id);
            userService.deleteUser(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "用户已删除");
            
            System.out.println("用户删除成功, userId: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("删除用户失败: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除用户失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> passwords) {
        try {
            System.out.println("接收到修改密码请求, userId: " + id);
            userService.changePassword(
                    id,
                    passwords.get("oldPassword"),
                    passwords.get("newPassword")
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "密码修改成功");
            
            System.out.println("密码修改成功, userId: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("修改密码失败: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "修改密码失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            System.out.println("接收到获取所有用户请求");
            List<User> users = userService.getAllUsers();
            
            // 移除敏感信息
            users.forEach(user -> user.setPassword(null));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", users);
            
            System.out.println("返回所有用户信息成功, 用户数量: " + users.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("获取所有用户失败: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取所有用户失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<?> getUsersByRole(@PathVariable User.UserRole role) {
        try {
            System.out.println("接收到获取指定角色用户请求, role: " + role);
            List<User> users = userService.getUsersByRole(role);
            
            // 移除敏感信息
            users.forEach(user -> user.setPassword(null));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", users);
            
            System.out.println("返回角色用户信息成功, 角色: " + role + ", 用户数量: " + users.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("获取角色用户失败: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取角色用户失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        try {
            System.out.println("接收到切换用户状态请求, userId: " + id + ", enabled: " + enabled);
            userService.toggleUserStatus(id, enabled);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "用户状态已更新");
            
            System.out.println("用户状态更新成功, userId: " + id + ", enabled: " + enabled);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("切换用户状态失败: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "切换用户状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/staff/active")
    public ResponseEntity<?> getAllActiveStaff() {
        try {
            System.out.println("接收到获取在职员工请求");
            List<User> users = userService.getAllActiveStaff();
            
            // 移除敏感信息
            users.forEach(user -> user.setPassword(null));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", users);
            
            System.out.println("返回在职员工信息成功, 员工数量: " + users.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("获取在职员工失败: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取在职员工失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/count/{role}")
    public ResponseEntity<?> countUsersByRole(@PathVariable User.UserRole role) {
        try {
            System.out.println("接收到统计角色用户数量请求, role: " + role);
            Long count = userService.countUsersByRole(role);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", count);
            
            System.out.println("返回角色用户数量成功, 角色: " + role + ", 数量: " + count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("统计角色用户数量失败: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "统计角色用户数量失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}