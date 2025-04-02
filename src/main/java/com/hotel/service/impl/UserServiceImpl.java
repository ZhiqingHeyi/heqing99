package com.hotel.service.impl;

import com.hotel.entity.User;
import com.hotel.repository.UserRepository;
import com.hotel.service.InvitationCodeService;
import com.hotel.service.UserService;
import com.hotel.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private InvitationCodeService invitationCodeService;

    @Override
    public User register(User user) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }

        // 对于管理员、前台和保洁人员角色，需要验证邀请码
        if (user.getRole() == User.UserRole.admin ||
            user.getRole() == User.UserRole.receptionist ||
            user.getRole() == User.UserRole.cleaner) {
            String invitationCode = user.getInvitationCode();
            if (invitationCode == null || invitationCode.trim().isEmpty()) {
                throw new RuntimeException("该角色需要提供邀请码");
            }
            invitationCodeService.validateInvitationCode(invitationCode, user.getRole());
            invitationCodeService.useInvitationCode(invitationCode);
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public String authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 生成JWT token
        String token = JwtUtil.generateToken(user.getUsername(), user.getRole().toString());
        return token;
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Override
    public User updateUser(User user) {
        User existingUser = getUserById(user.getId());
        
        // 更新用户信息，但不更新密码
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setRole(user.getRole());

        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户不存在");
        }
        userRepository.deleteById(id);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> getUsersByRole(User.UserRole role) {
        return userRepository.findByRole(role);
    }

    @Override
    public void toggleUserStatus(Long userId, boolean enabled) {
        User user = getUserById(userId);
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    @Override
    public List<User> getAllActiveStaff() {
        return userRepository.findAllActiveStaff();
    }

    @Override
    public long countUsersByRole(User.UserRole role) {
        return userRepository.countByRole(role);
    }
}