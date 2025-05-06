package com.hotel.service;

import com.hotel.entity.User;
import com.hotel.repository.UserRepository; // 假设 UserRepository 存在
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.hotel.security.CustomUserDetails; // 引入 CustomUserDetails
import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory

import java.util.Collections;
import java.util.Set;
import java.util.Optional; // Import Optional if not already imported

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class); // Add logger

    @Autowired
    private UserRepository userRepository;

    @Override
    // 修改参数名，使其更清晰
    public UserDetails loadUserByUsername(String usernameOrPhone) throws UsernameNotFoundException {
        logger.info("Attempting to load user by identifier: {}", usernameOrPhone); // Log identifier
        // 尝试按用户名查找
        Optional<User> userOptional = userRepository.findByUsername(usernameOrPhone);

        // 如果用户名未找到，尝试按手机号查找
        if (!userOptional.isPresent()) {
             logger.info("User not found by username, trying phone: {}", usernameOrPhone);
            userOptional = userRepository.findByPhone(usernameOrPhone);
        }

        // 如果最终找到了用户（无论通过哪种方式），则获取 User 对象；否则抛出异常
        User user = userOptional.orElseThrow(() -> 
                new UsernameNotFoundException("User not found with identifier: " + usernameOrPhone));
        logger.info("User found: {}, Role: {}", user.getUsername(), user.getRole().name()); // Log found user and role

        // 创建权限集合 (Spring Security 需要 ROLE_ 前缀，除非特别配置)
        String roleName = "ROLE_" + user.getRole().name(); 
        Set<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(roleName));
        logger.info("Assigned authorities: {}", authorities); // Log assigned authorities

        // 返回 CustomUserDetails 对象，包含 ID
        return new CustomUserDetails(
                user.getId(), // <-- 添加 user ID
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
} 