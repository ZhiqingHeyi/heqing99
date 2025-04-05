package com.hotel.service.impl;

import com.hotel.entity.MemberLevel;
import com.hotel.entity.User;
import com.hotel.repository.UserRepository;
import com.hotel.service.InvitationCodeService;
import com.hotel.service.UserService;
import com.hotel.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
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
        
        // 设置默认会员级别和积分
        if (user.getMemberLevel() == null) {
            user.setMemberLevel(MemberLevel.REGULAR);
        }
        if (user.getPoints() == null) {
            user.setPoints(0);
        }
        if (user.getTotalSpent() == null) {
            user.setTotalSpent(BigDecimal.ZERO);
        }
        
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
        return userRepository.findByRoleAndEnabledTrue(User.UserRole.cleaner);
    }

    @Override
    public List<User> getAllStaff() {
        List<User> staff = new ArrayList<>();
        staff.addAll(userRepository.findByRole(User.UserRole.cleaner));
        staff.addAll(userRepository.findByRole(User.UserRole.receptionist));
        return staff;
    }

    @Override
    public Long countUsersByRole(User.UserRole role) {
        return userRepository.countByRole(role);
    }

    @Override
    public long countAllUsers() {
        return userRepository.count();
    }

    @Override
    public User createUser(User user) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 设置默认值
        if (user.getRole() == null) {
            user.setRole(User.UserRole.CUSTOMER);
        }
        
        return userRepository.save(user);
    }
    
    // 会员相关方法实现
    
    @Override
    public User updateMemberLevel(Long userId) {
        User user = getUserById(userId);
        
        // 调用User实体类中的方法更新会员等级
        user.updateMemberLevel();
        
        // 保存更新后的用户信息
        return userRepository.save(user);
    }
    
    @Override
    public User addPoints(Long userId, int points) {
        if (points <= 0) {
            throw new IllegalArgumentException("积分必须为正数");
        }
        
        User user = getUserById(userId);
        user.setPoints(user.getPoints() + points);
        return userRepository.save(user);
    }
    
    @Override
    public User addSpending(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("消费金额必须为正数");
        }
        
        User user = getUserById(userId);
        
        // 计算获得的积分
        int pointsToAdd = calculatePointsForSpending(user.getMemberLevel(), amount);
        
        // 更新累计消费
        user.setTotalSpent(user.getTotalSpent().add(amount));
        
        // 添加积分
        user.setPoints(user.getPoints() + pointsToAdd);
        
        // 保存用户
        userRepository.save(user);
        
        // 更新会员等级
        return updateMemberLevel(userId);
    }
    
    @Override
    public User redeemPoints(Long userId, int points) {
        if (points <= 0) {
            throw new IllegalArgumentException("兑换积分必须为正数");
        }
        
        User user = getUserById(userId);
        
        if (user.getPoints() < points) {
            throw new RuntimeException("积分不足");
        }
        
        user.setPoints(user.getPoints() - points);
        return userRepository.save(user);
    }
    
    @Override
    public String getMemberLevelByUserId(Long userId) {
        User user = getUserById(userId);
        return user.getMemberLevel().getDisplayName();
    }
    
    @Override
    public BigDecimal getDiscountByUserId(Long userId) {
        User user = getUserById(userId);
        return BigDecimal.valueOf(user.getDiscountRate());
    }
    
    @Override
    public int getPointsRateByUserId(Long userId) {
        User user = getUserById(userId);
        MemberLevel memberLevel = user.getMemberLevel();
        
        // 根据会员等级返回积分比例
        switch (memberLevel) {
            case REGULAR:
                return 0; // 普通用户不积分
            case BRONZE:
                return 100; // 1元=1积分
            case SILVER:
                return 120; // 1元=1.2积分
            case GOLD:
                return 150; // 1元=1.5积分
            case DIAMOND:
                return 200; // 1元=2积分
            default:
                return 0;
        }
    }
    
    // 辅助方法：计算消费对应的积分
    private int calculatePointsForSpending(MemberLevel memberLevel, BigDecimal amount) {
        int pointsRate = 0;
        
        // 根据会员等级设置积分比例
        switch (memberLevel) {
            case REGULAR:
                pointsRate = 0; // 普通用户不积分
                break;
            case BRONZE:
                pointsRate = 100; // 1元=1积分
                break;
            case SILVER:
                pointsRate = 120; // 1元=1.2积分
                break;
            case GOLD:
                pointsRate = 150; // 1元=1.5积分
                break;
            case DIAMOND:
                pointsRate = 200; // 1元=2积分
                break;
        }
        
        // 计算积分：金额 * 积分比例 / 100
        return amount.multiply(BigDecimal.valueOf(pointsRate))
                .divide(BigDecimal.valueOf(100), BigDecimal.ROUND_DOWN)
                .intValue();
    }
}