package com.hotel.controller;

import com.hotel.common.ApiResponse;
import com.hotel.entity.User;
import com.hotel.service.UserService;
import com.hotel.dto.LoginRequest;
import com.hotel.dto.LoginResponse;
import com.hotel.util.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * 用户注册接口
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> registerData) {
        try {
            System.out.println("===== 接收到用户注册请求 =====");
            System.out.println("请求数据: " + registerData);
            
            // 验证必要的字段
            if (registerData == null) {
                throw new RuntimeException("注册数据为空");
            }
            
            // 验证用户名
            String username = registerData.get("username");
            if (username == null || username.trim().isEmpty()) {
                throw new RuntimeException("用户名不能为空");
            }
            
            // 验证密码
            String password = registerData.get("password");
            if (password == null || password.trim().isEmpty()) {
                throw new RuntimeException("密码不能为空");
            }
            
            // 验证确认密码
            String confirmPassword = registerData.get("confirmPassword");
            if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
                throw new RuntimeException("确认密码不能为空");
            }
            
            // 验证密码一致性
            if (!password.equals(confirmPassword)) {
                throw new RuntimeException("两次输入的密码不一致");
            }
            
            // 验证姓名
            String name = registerData.get("name");
            if (name == null || name.trim().isEmpty()) {
                throw new RuntimeException("姓名不能为空");
            }
            
            // 验证手机号码
            String phone = registerData.get("phone");
            if (phone == null || phone.trim().isEmpty()) {
                throw new RuntimeException("手机号码不能为空");
            }
            
            // 创建用户对象
            User user = new User();
            user.setUsername(username);
            user.setPassword(password); 
            user.setName(name);
            user.setPhone(phone);
            
            // 处理可选字段：邮箱
            String email = registerData.get("email");
            if (email != null && !email.trim().isEmpty()) {
                user.setEmail(email);
            }
            
            // 处理可选字段：性别
            String gender = registerData.get("gender");
            if (gender != null && !gender.trim().isEmpty()) {
                if (gender.equalsIgnoreCase("male") || 
                    gender.equalsIgnoreCase("female") || 
                    gender.equalsIgnoreCase("unknown")) {
                    user.setGender(gender.toLowerCase());
                } else {
                    throw new RuntimeException("性别值无效，应为male、female或unknown");
                }
            }
            
            // 处理可选字段：生日
            String birthdayStr = registerData.get("birthday");
            if (birthdayStr != null && !birthdayStr.trim().isEmpty()) {
                try {
                    LocalDate birthday = LocalDate.parse(birthdayStr, DateTimeFormatter.ISO_DATE);
                    user.setBirthday(birthday);
                } catch (DateTimeParseException e) {
                    throw new RuntimeException("生日格式错误，应为YYYY-MM-DD格式");
                }
            }
            
            // 设置为普通用户角色
            user.setRole(User.UserRole.CUSTOMER);
            
            System.out.println("创建的用户对象: " + user);
            
            // 调用用户服务进行注册
            User registeredUser = userService.register(user);
            
            System.out.println("注册成功，返回的用户ID: " + registeredUser.getId());
            
            // 按API文档格式构建响应
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("userId", registeredUser.getId().toString());
            resultData.put("username", registeredUser.getUsername());
            
            // 使用API响应工具类返回统一格式
            ApiResponse<Map<String, Object>> response = ApiResponse.success("注册成功", resultData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("===== 用户注册失败 =====");
            System.err.println("异常类型: " + e.getClass().getName());
            System.err.println("异常消息: " + e.getMessage());
            e.printStackTrace();
            
            // 使用API响应工具类返回错误信息
            ApiResponse<?> errorResponse = ApiResponse.fail("注册失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        try {
            System.out.println("检查用户名是否可用: " + username);
            boolean exists = false;
            
            try {
                userService.getUserByUsername(username);
                exists = true;
            } catch (RuntimeException e) {
                exists = false;
            }
            
            Map<String, Boolean> resultData = new HashMap<>();
            resultData.put("available", !exists);
            
            // 使用API响应工具类返回统一格式
            ApiResponse<Map<String, Boolean>> response = ApiResponse.success(resultData);
            
            System.out.println("用户名 " + username + " 可用性: " + !exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 如果是找不到用户的异常，说明用户名可用
            System.out.println("检查用户名发生异常，默认用户名可用: " + e.getMessage());
            
            Map<String, Boolean> resultData = new HashMap<>();
            resultData.put("available", true);
            
            // 使用API响应工具类返回统一格式
            ApiResponse<Map<String, Boolean>> response = ApiResponse.success(resultData);
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
            
            boolean exists = userService.existsByPhone(phone);
            
            Map<String, Boolean> resultData = new HashMap<>();
            resultData.put("available", !exists);
            
            // 使用API响应工具类返回统一格式
            ApiResponse<Map<String, Boolean>> response = ApiResponse.success(resultData);
            
            System.out.println("手机号 " + phone + " 可用性: " + !exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("检查手机号发生异常: " + e.getMessage());
            
            Map<String, Boolean> resultData = new HashMap<>();
            resultData.put("available", true);
            
            // 使用API响应工具类返回统一格式
            ApiResponse<Map<String, Boolean>> response = ApiResponse.success(resultData);
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 用户登录接口 (使用 Spring Security 和 JWT)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("接收到登录请求: " + loginRequest.getUsername());

            // 1. 使用 AuthenticationManager 进行认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // 2. 将认证信息设置到 SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. 从认证信息中获取 UserDetails
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // 4. 生成 JWT
            String token = jwtTokenProvider.generateToken(authentication);

            // 5. 获取用户详细信息 (从 UserDetails 或 userService)
            User user = userService.getUserByUsername(userDetails.getUsername());

            // 6. 构建响应
            LoginResponse response = new LoginResponse(
                    true,
                    "登录成功",
                    user.getId(),
                    user.getUsername(),
                    user.getRole().toString(),
                    token
            );

            System.out.println("用户登录成功, userId: " + user.getId() + ", username: " + user.getUsername());
            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            System.err.println("用户认证失败: " + e.getMessage());
            LoginResponse response = new LoginResponse(false, "用户名或密码错误", null, null, null, null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            System.err.println("登录处理发生错误: " + e.getMessage());
            e.printStackTrace();
            LoginResponse response = new LoginResponse(false, "登录失败: " + e.getMessage(), null, null, null, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            System.out.println("接收到获取用户信息请求, userId: " + id);
            User user = userService.getUserById(id);
            
            // 移除敏感信息
            user.setPassword(null);
            
            // 使用API响应工具类返回统一格式
            ApiResponse<User> response = ApiResponse.success(user);
            
            System.out.println("返回用户信息成功, userId: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("获取用户信息失败: " + e.getMessage());
            e.printStackTrace();
            
            // 使用API响应工具类返回错误信息
            ApiResponse<?> errorResponse = ApiResponse.fail("获取用户信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        try {
            System.out.println("接收到获取用户信息请求, username: " + username);
            User user = userService.getUserByUsername(username);
            
            // 移除敏感信息
            user.setPassword(null);
            
            // 使用API响应工具类返回统一格式
            ApiResponse<User> response = ApiResponse.success(user);
            
            System.out.println("返回用户信息成功, username: " + username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("获取用户信息失败: " + e.getMessage());
            e.printStackTrace();
            
            // 使用API响应工具类返回错误信息
            ApiResponse<?> errorResponse = ApiResponse.fail("获取用户信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userUpdate) {
        try {
            System.out.println("接收到更新用户信息请求, userId: " + id);
            
            // 获取当前用户信息
            User existingUser = userService.getUserById(id);
            
            // 只更新允许的字段
            User user = new User();
            user.setId(id);
            user.setName(userUpdate.getName());
            user.setPhone(userUpdate.getPhone());
            user.setEmail(userUpdate.getEmail());
            user.setGender(userUpdate.getGender());
            user.setBirthday(userUpdate.getBirthday());
            
            // 保持原有的角色和其他字段不变
            user.setRole(existingUser.getRole());
            
            User updatedUser = userService.updateUser(user);
            
            // 移除敏感信息
            updatedUser.setPassword(null);
            
            // 使用API响应工具类返回统一格式
            ApiResponse<User> response = ApiResponse.success("用户信息更新成功", updatedUser);
            
            System.out.println("用户信息更新成功, userId: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("更新用户信息失败: " + e.getMessage());
            e.printStackTrace();
            
            // 使用API响应工具类返回错误信息
            ApiResponse<?> errorResponse = ApiResponse.fail("更新用户信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllActiveStaff() {
        try {
            System.out.println("接收到获取在职员工请求");
            List<User> users = userService.getAllActiveStaff();
            
            // 移除敏感信息
            users.forEach(user -> user.setPassword(null));
            
            // 使用API响应工具类返回统一格式
            ApiResponse<List<User>> response = ApiResponse.success(users);
            
            System.out.println("返回在职员工信息成功, 员工数量: " + users.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("获取在职员工失败: " + e.getMessage());
            e.printStackTrace();
            
            // 使用API响应工具类返回错误信息
            ApiResponse<?> errorResponse = ApiResponse.fail("获取在职员工失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/count/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> countUsersByRole(@PathVariable User.UserRole role) {
        try {
            System.out.println("接收到统计角色用户数量请求, role: " + role);
            
            // 验证角色参数是否有效
            if (role == null) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.fail("无效的角色参数")
                );
            }
            
            Long count = userService.countUsersByRole(role);
            
            // 使用API响应工具类返回统一格式
            ApiResponse<Long> response = ApiResponse.success(count);
            
            System.out.println("返回角色用户数量成功, 角色: " + role + ", 数量: " + count);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            System.err.println("角色参数错误: " + e.getMessage());
            return ResponseEntity.badRequest().body(
                ApiResponse.fail("角色参数错误: " + e.getMessage())
            );
        } catch (Exception e) {
            System.err.println("统计角色用户数量失败: " + e.getMessage());
            e.printStackTrace();
            
            // 使用API响应工具类返回错误信息
            ApiResponse<?> errorResponse = ApiResponse.fail("统计角色用户数量失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}