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

import java.util.Collections;
import java.util.Set;
import java.util.Optional; // Import Optional if not already imported

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    // 修改参数名，使其更清晰
    public UserDetails loadUserByUsername(String usernameOrPhone) throws UsernameNotFoundException {
        // 尝试按用户名查找
        Optional<User> userOptional = userRepository.findByUsername(usernameOrPhone);

        // 如果用户名未找到，尝试按手机号查找
        if (!userOptional.isPresent()) {
            userOptional = userRepository.findByPhone(usernameOrPhone);
        }

        // 如果最终找到了用户（无论通过哪种方式），则获取 User 对象；否则抛出异常
        User user = userOptional.orElseThrow(() -> 
                new UsernameNotFoundException("User not found with identifier: " + usernameOrPhone));

        // 创建权限集合 (Spring Security 需要 ROLE_ 前缀，除非特别配置)
        String roleName = "ROLE_" + user.getRole().name(); 
        Set<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(roleName));

        // 返回 Spring Security 的 User 对象
        // 注意：principal name 仍然使用数据库中的实际用户名 user.getUsername()
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(), // 确保密码是加密存储的
                authorities
        );
    }
} 