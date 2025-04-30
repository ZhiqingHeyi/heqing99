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

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库查找用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> 
                        new UsernameNotFoundException("User not found with username: " + username));

        // 创建权限集合 (Spring Security 需要 ROLE_ 前缀，除非特别配置)
        // 假设 User 实体中的 Role 是枚举或字符串，需要转换为 GrantedAuthority
        String roleName = "ROLE_" + user.getRole().name(); // 假设 Role 是枚举
        Set<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(roleName));

        // 返回 Spring Security 的 User 对象
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(), // 确保密码是加密存储的
                authorities
        );
    }
} 