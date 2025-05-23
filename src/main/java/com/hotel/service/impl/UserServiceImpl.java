package com.hotel.service.impl;

import com.hotel.entity.MemberLevel;
import com.hotel.entity.User;
import com.hotel.dto.UserDTO;
import com.hotel.dto.StaffRegistrationDto;
import com.hotel.repository.UserRepository;
import com.hotel.entity.InvitationCode;
import com.hotel.service.InvitationCodeService;
import com.hotel.service.UserService;
import com.hotel.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

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
    public Long findUserByRealName(String realName) {
        if (realName == null || realName.trim().isEmpty()) {
            throw new RuntimeException("真实姓名不能为空");
        }
        
        return userRepository.findByName(realName)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("未找到用户: " + realName));
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
    public void toggleUserStatus(Long userId) {
        User user = getUserById(userId);
        // 获取当前状态，并计算新状态
        String currentStatus = user.getStatus();
        String newStatus = "ACTIVE".equalsIgnoreCase(currentStatus) ? "DISABLED" : "ACTIVE"; // 假设数据库用 ACTIVE/DISABLED
        // 设置新状态
        user.setStatus(newStatus);
        // 保存用户
        userRepository.save(user);
        System.out.println("用户状态已切换至: " + newStatus + " for userId: " + userId);
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
    public int countNewUsersThisMonth() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDateTime startOfMonth = firstDayOfMonth.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        
        return userRepository.countByCreateTimeBetween(startOfMonth, endOfDay);
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

    @Override
    public Page<UserDTO> findUsersPaginatedAndFiltered(int page, int pageSize, String username, String phone, String status, String role) {
        System.out.println("开始查询用户(分页+过滤): page="+page+", pageSize="+pageSize+", username="+username+", phone="+phone+", status="+status+", role="+role);
        
        // 创建分页请求对象
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));

        // 使用 Specification 构建动态查询条件
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 添加用户名模糊查询条件
            if (username != null && !username.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("username"), "%" + username.trim() + "%"));
            }

            // 添加手机号精确查询条件 (或模糊查询，根据需要)
            if (phone != null && !phone.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("phone"), phone.trim()));
            }

            // 添加状态查询条件
            if (status != null && !status.trim().isEmpty()) {
                System.out.println("添加 status 过滤条件: " + status);
                // 假设 status 是字符串类型且与数据库存储一致
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("status")), status.toLowerCase()));
            }

            // 添加 role 过滤条件
            if (role != null && !role.trim().isEmpty()) {
                System.out.println("添加 role 过滤条件: " + role);
                try {
                    // 尝试将传入的 role 字符串转换为 UserRole 枚举
                    User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase()); 
                    predicates.add(criteriaBuilder.equal(root.get("role"), userRole));
                } catch (IllegalArgumentException e) {
                    // 如果转换失败 (传入的 role 不是有效的枚举值), 可以选择忽略或抛出异常
                    System.err.println("无效的角色值: " + role + ", 忽略该过滤条件。");
                    // 或者可以抛出异常: throw new IllegalArgumentException("无效的角色值: " + role);
                }
            }

            System.out.println("构建的 Predicate 数量: " + predicates.size());
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询并转换
        Page<User> userPage = userRepository.findAll(spec, pageable);
        return userPage.map(UserDTO::fromEntity); // 使用静态工厂方法引用
    }

    @Override
    public Page<UserDTO> findStaffPaginatedAndFiltered(int page, int pageSize, String username, String phone, String status) {
        System.out.println("开始查询员工(分页+过滤): page="+page+", pageSize="+pageSize+", username="+username+", phone="+phone+", status="+status);
        
        // 创建分页请求对象 (使用 createTime 排序)
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));

        // 创建查询规范
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 固定添加角色过滤条件：必须是 RECEPTIONIST 或 CLEANER
            predicates.add(root.get("role").in(User.UserRole.RECEPTIONIST, User.UserRole.CLEANER));

            // 添加其他过滤条件 (与 findUsersPaginatedAndFiltered 类似)
            if (username != null && !username.trim().isEmpty()) {
                System.out.println("添加 username 过滤条件: " + username);
                predicates.add(criteriaBuilder.like(root.get("username"), "%" + username.trim() + "%"));
            }
            if (phone != null && !phone.trim().isEmpty()) {
                System.out.println("添加 phone 过滤条件: " + phone);
                // 注意：用户表中的 phone 可能允许 NULL，但这里假设我们只对有手机号的用户进行精确匹配
                predicates.add(criteriaBuilder.equal(root.get("phone"), phone.trim()));
            }
            if (status != null && !status.trim().isEmpty()) {
                System.out.println("添加 status 过滤条件: " + status);
                // 假设 status 是字符串类型且与数据库存储一致
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("status")), status.toLowerCase()));
            }

            System.out.println("构建的员工查询 Predicate 数量: " + predicates.size());
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<User> staffPage = userRepository.findAll(spec, pageable);
        
        System.out.println("员工查询完成, 返回数量: " + staffPage.getNumberOfElements() + ", 总页数: " + staffPage.getTotalPages());
        
        // 移除敏感信息 (例如密码) - 不再需要，在转换DTO时已排除
        // staffPage.getContent().forEach(user -> user.setPassword(null));

        // return staffPage;
        // return staffPage.map(user -> convertToDto(user)); // 使用 map 转换
        return staffPage.map(UserDTO::fromEntity); // 使用静态工厂方法引用
    }

    // 添加私有辅助方法用于转换
    private UserDTO convertToDto(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setGender(user.getGender());
        dto.setBirthday(user.getBirthday());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setStatus(user.getStatus());
        dto.setMemberLevel(user.getMemberLevel() != null ? user.getMemberLevel().name() : null);
        dto.setPoints(user.getPoints());
        dto.setTotalSpent(user.getTotalSpent());
        dto.setCreateTime(user.getCreateTime());
        dto.setUpdateTime(user.getUpdateTime());
        // 注意：这里没有设置 password
        return dto;
    }

    @Override
    @Transactional
    public User registerStaff(StaffRegistrationDto dto) {
        System.out.println("===== 开始员工注册处理 =====");
        System.out.println("注册 DTO: " + dto);

        // 1. 检查用户名是否已存在
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            System.out.println("用户名已存在: " + dto.getUsername());
            throw new RuntimeException("用户名已存在");
        }
        System.out.println("用户名可用: " + dto.getUsername());

        // 2. 再次验证邀请码
        System.out.println("验证邀请码: " + dto.getInviteCode());
        InvitationCode validCode = invitationCodeService.validateCodeOnly(dto.getInviteCode());
        System.out.println("邀请码验证成功，角色: " + validCode.getRole());

        // 3. 标记邀请码为已使用
        System.out.println("标记邀请码为已使用: " + dto.getInviteCode());
        invitationCodeService.useInvitationCode(dto.getInviteCode());
        System.out.println("邀请码状态更新成功");

        // 4. 创建用户实体
        User newUser = new User();
        System.out.println("创建新 User 实体");

        // 5. 设置用户信息
        newUser.setUsername(dto.getUsername());
        newUser.setName(dto.getRealName()); // 使用 realName 作为 name
        newUser.setPhone(dto.getPhone());
        newUser.setEmail(dto.getEmail());
        System.out.println("设置用户基本信息");

        // 6. 加密密码
        System.out.println("加密密码");
        newUser.setPassword(passwordEncoder.encode(dto.getPassword()));

        // 7. 设置角色 (从验证后的邀请码获取)
        newUser.setRole(validCode.getRole());
        System.out.println("设置用户角色: " + validCode.getRole());

        // 8. 设置邀请码关联 (可选，如果需要记录)
        newUser.setInvitationCode(dto.getInviteCode());
        System.out.println("设置关联邀请码");

        // 9. 设置默认值 (员工不需要会员等级/积分，但需要设置状态为启用)
        newUser.setStatus("ACTIVE"); // 默认启用状态
        // 员工通常不需要会员等级和积分，保持默认或null
        // newUser.setMemberLevel(MemberLevel.REGULAR); 
        // newUser.setPoints(0);
        // newUser.setTotalSpent(BigDecimal.ZERO);
        System.out.println("设置默认状态为 ACTIVE");

        // 10. 保存用户
        System.out.println("保存员工用户到数据库");
        User savedUser = userRepository.save(newUser);
        System.out.println("员工用户保存成功，ID: " + savedUser.getId());

        // 11. 返回用户
        return savedUser;
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            // 用户未认证或为匿名用户
            return null; 
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username)
                    .orElse(null); // 或者抛出异常，例如 new UsernameNotFoundException("User not found in repository for authenticated principal: " + username)
        } else if (principal instanceof String) {
            // 有时 principal 直接是 username 字符串
            String username = (String) principal;
            return userRepository.findByUsername(username)
                    .orElse(null); // 同上
        }
        
        log.warn("Could not determine user from principal of type: {}", principal.getClass().getName());
        return null; // 无法从 principal 确定用户
    }
}