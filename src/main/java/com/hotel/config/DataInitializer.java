package com.hotel.config;

import com.hotel.entity.User;
import com.hotel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 如果没有管理员用户，创建默认的管理员账号
        if (!userRepository.findByUsername("admin").isPresent()) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setName("系统管理员");
            adminUser.setRole(User.UserRole.admin);
            adminUser.setEnabled(true);
            userRepository.save(adminUser);
            
            System.out.println("已创建默认管理员账号: admin/admin123");
        }

        // 如果没有前台用户，创建默认的前台账号
        if (!userRepository.findByUsername("front").isPresent()) {
            User frontUser = new User();
            frontUser.setUsername("front");
            frontUser.setPassword(passwordEncoder.encode("front123"));
            frontUser.setName("前台接待");
            frontUser.setRole(User.UserRole.receptionist);
            frontUser.setEnabled(true);
            userRepository.save(frontUser);
            
            System.out.println("已创建默认前台账号: front/front123");
        }

        // 如果没有清洁人员用户，创建默认的清洁人员账号
        if (!userRepository.findByUsername("cleaner").isPresent()) {
            User cleanerUser = new User();
            cleanerUser.setUsername("cleaner");
            cleanerUser.setPassword(passwordEncoder.encode("cleaner123"));
            cleanerUser.setName("清洁人员");
            cleanerUser.setRole(User.UserRole.cleaner);
            cleanerUser.setEnabled(true);
            userRepository.save(cleanerUser);
            
            System.out.println("已创建默认清洁人员账号: cleaner/cleaner123");
        }
    }
} 