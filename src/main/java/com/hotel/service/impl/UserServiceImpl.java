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
import java.util.Optional;

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
        try {
            System.out.println("===== 开始用户注册处理 =====");
            System.out.println("用户角色: " + user.getRole());
            
            // 检查用户名是否已存在
            if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                System.out.println("用户名已存在: " + user.getUsername());
                throw new RuntimeException("用户名已存在");
            }

            // 对于管理员、前台和保洁人员角色，需要验证邀请码
            if (user.getRole() == User.UserRole.ADMIN ||
                user.getRole() == User.UserRole.RECEPTIONIST ||
                user.getRole() == User.UserRole.CLEANER) {
                
                // 确保invitationCodeService已经被注入
                if (invitationCodeService == null) {
                    System.out.println("严重错误: invitationCodeService未注入");
                    throw new RuntimeException("系统错误：邀请码服务未配置");
                }
                
                String invitationCode = user.getInvitationCode();
                if (invitationCode == null || invitationCode.trim().isEmpty()) {
                    System.out.println("缺少邀请码");
                    throw new RuntimeException("该角色需要提供邀请码");
                }
                
                System.out.println("验证邀请码: " + invitationCode);
                invitationCodeService.validateInvitationCode(invitationCode, user.getRole());
                
                System.out.println("使用邀请码: " + invitationCode);
                invitationCodeService.useInvitationCode(invitationCode);
            }

            // 加密密码
            System.out.println("加密密码");
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            
            // 设置默认会员级别和积分
            System.out.println("设置默认会员信息");
            if (user.getMemberLevel() == null) {
                System.out.println("设置默认会员等级: " + MemberLevel.REGULAR);
                user.setMemberLevel(MemberLevel.REGULAR);
            }
            if (user.getPoints() == null) {
                System.out.println("设置默认积分: 0");
                user.setPoints(0);
            }
            if (user.getTotalSpent() == null) {
                System.out.println("设置默认消费金额: 0");
                user.setTotalSpent(BigDecimal.ZERO);
            }
            
            System.out.println("保存用户到数据库");
            User savedUser = userRepository.save(user);
            System.out.println("用户保存成功，ID: " + savedUser.getId());
            
            return savedUser;
        } catch (Exception e) {
            System.err.println("===== 用户注册过程中发生错误 =====");
            System.err.println("错误类型: " + e.getClass().getName());
            System.err.println("错误消息: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public String authenticate(String usernameOrPhone, String password) {
        // 保存日志
        System.out.println("尝试验证用户: " + usernameOrPhone);
        
        // 尝试通过用户名查找用户
        Optional<User> userByUsername = userRepository.findByUsername(usernameOrPhone);
        
        // 如果通过用户名未找到用户，尝试通过手机号查找
        User user;
        if (userByUsername.isPresent()) {
            user = userByUsername.get();
            System.out.println("通过用户名找到用户: " + user.getUsername());
        } else {
            // 尝试通过手机号查找
            Optional<User> userByPhone = userRepository.findByPhone(usernameOrPhone);
            if (userByPhone.isPresent()) {
                user = userByPhone.get();
                System.out.println("通过手机号找到用户: " + user.getUsername());
            } else {
                System.out.println("未找到用户: " + usernameOrPhone);
                throw new RuntimeException("用户不存在");
            }
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            System.out.println("密码验证失败");
            throw new RuntimeException("密码错误");
        }

        System.out.println("用户验证成功，生成token");
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

    // 添加通过手机号获取用户的方法
    @Override
    public User getUserByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Override
    public User updateUser(User user) {
        User existingUser = getUserById(user.getId());
        
        // 更新用户信息，但不更新密码
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setGender(user.getGender());
        existingUser.setBirthday(user.getBirthday());
        
        // 管理员才能修改角色
        if (user.getRole() != null) {
            existingUser.setRole(user.getRole());
        }

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
        try {
            System.out.println("获取所有活跃员工...");
            List<User> activeStaff = userRepository.findAllActiveStaff();
            System.out.println("成功获取活跃员工，数量: " + activeStaff.size());
            return activeStaff;
        } catch (Exception e) {
            System.err.println("获取活跃员工失败: " + e.getMessage());
            throw new RuntimeException("获取活跃员工时发生错误: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> getAllStaff() {
        List<User> staff = new ArrayList<>();
        staff.addAll(userRepository.findByRole(User.UserRole.CLEANER));
        staff.addAll(userRepository.findByRole(User.UserRole.RECEPTIONIST));
        return staff;
    }

    @Override
    public Long countUsersByRole(User.UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("角色参数不能为空");
        }
        
        try {
            System.out.println("统计角色为 " + role + " 的用户数量...");
            long count = userRepository.countByRole(role);
            System.out.println("成功获取角色 " + role + " 的用户数量: " + count);
            return count;
        } catch (Exception e) {
            System.err.println("统计角色用户数量失败: " + e.getMessage());
            throw new RuntimeException("统计角色用户数量时发生错误: " + e.getMessage(), e);
        }
    }

    @Override
    public long countAllUsers() {
        return userRepository.count();
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
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