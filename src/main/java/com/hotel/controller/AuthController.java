package com.hotel.controller;

import com.hotel.service.TokenBlacklistService;
import com.hotel.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    @Autowired(required = false)
    private TokenBlacklistService tokenBlacklistService;

    /**
     * 刷新Token接口
     * 
     * @param refreshTokenRequest 包含刷新令牌的请求体
     * @return 新的访问令牌和过期时间
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> refreshTokenRequest) {
        try {
            String refreshToken = refreshTokenRequest.get("refreshToken");
            
            if (refreshToken == null || refreshToken.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("code", 400);
                response.put("message", "刷新令牌不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 验证刷新令牌
            boolean isValidRefreshToken = JwtUtil.validateRefreshToken(refreshToken);
            
            if (!isValidRefreshToken) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("code", 401);
                response.put("message", "无效的刷新令牌");
                return ResponseEntity.status(401).body(response);
            }

            // 从刷新令牌中获取用户信息
            String username = JwtUtil.getUsernameFromToken(refreshToken);
            String role = JwtUtil.getRoleFromToken(refreshToken);

            // 生成新的访问令牌
            String newToken = JwtUtil.generateToken(username, role);
            long expiresIn = JwtUtil.getExpirationDuration(); // 获取配置的过期时间（秒）

            // 构建响应
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", newToken);
            responseData.put("expiresIn", expiresIn);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "刷新成功");
            response.put("data", responseData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", 500);
            response.put("message", "刷新令牌失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 登出接口
     * 
     * @param request 请求
     * @return 登出结果
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // 检查Authorization头是否存在
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("code", 400);
                response.put("message", "未提供有效的认证令牌");
                return ResponseEntity.badRequest().body(response);
            }

            // 提取token
            String token = authHeader.substring(7);

            // 如果已配置令牌黑名单服务，则将令牌添加到黑名单
            if (tokenBlacklistService != null) {
                // 从令牌中获取过期时间
                Date expiryDate = JwtUtil.getExpirationDateFromToken(token);
                // 计算剩余有效时间（毫秒）
                long remainingTime = expiryDate.getTime() - System.currentTimeMillis();
                
                // 只有当令牌仍然有效时才添加到黑名单
                if (remainingTime > 0) {
                    tokenBlacklistService.addToBlacklist(token, remainingTime / 1000);
                }
            }

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "登出成功");
            response.put("data", null);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", 500);
            response.put("message", "登出失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
} 