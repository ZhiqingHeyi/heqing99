package com.hotel.util;

import com.hotel.security.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 用户上下文工具类
 */
public class UserContextHolder {
    
    private static final Logger logger = LoggerFactory.getLogger(UserContextHolder.class);
    
    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 检查认证信息是否存在且有效
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未登录或认证无效");
        }
        
        // 获取 Principal
        Object principal = authentication.getPrincipal();
        
        // 检查 Principal 是否为 CustomUserDetails 类型
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getId(); // 返回 ID
        } else if (principal instanceof UserDetails) {
             // 如果 Principal 是标准的 UserDetails 但不是 CustomUserDetails
             // 尝试从 getName() 获取，但这可能不是 ID (保留作为后备或记录警告)
             // throw new RuntimeException("无法从 UserDetails 获取用户ID，类型不匹配: " + principal.getClass());
             // 或者记录警告，并尝试按原方式解析 name (不推荐，因为已知 name 是 username)
             logger.warn("Principal 不是 CustomUserDetails 类型，尝试从 getName() 解析ID，这可能不准确: {}", principal.getClass());
             try {
                 return Long.parseLong(((UserDetails) principal).getUsername()); // 使用 getUsername() 而不是 getName()
             } catch (NumberFormatException e) {
                 throw new RuntimeException("无法将 Principal 的 username 解析为用户ID: " + ((UserDetails) principal).getUsername());
             }
        } else {
            // Principal 不是 UserDetails 类型 (可能是匿名用户或其他情况)
            throw new RuntimeException("无法获取当前用户信息，无效的 Principal 类型: " + principal.getClass());
        }
    }
} 