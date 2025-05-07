package com.hotel.service;

import com.hotel.entity.User;
import com.hotel.dto.UserDTO;
import com.hotel.dto.StaffRegistrationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface UserService {
    /**
     * 用户注册
     */
    User register(User user);

    /**
     * 用户登录认证
     */
    String authenticate(String username, String password);

    /**
     * 获取用户信息
     */
    User getUserById(Long id);

    /**
     * 通过用户名获取用户信息
     */
    User getUserByUsername(String username);

    /**
     * 通过手机号获取用户信息
     */
    User getUserByPhone(String phone);

    /**
     * 创建用户
     */
    User createUser(User user);

    /**
     * 更新用户信息
     */
    User updateUser(User user);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 修改用户密码
     */
    void changePassword(Long id, String oldPassword, String newPassword);

    /**
     * 获取所有用户列表
     */
    List<User> getAllUsers();

    /**
     * 根据角色获取用户列表
     */
    List<User> getUsersByRole(User.UserRole role);

    /**
     * 启用/禁用用户 (修改签名)
     */
    void toggleUserStatus(Long userId);

    /**
     * 获取所有在职的保洁人员
     */
    List<User> getAllActiveStaff();

    /**
     * 获取所有员工（包括前台和清洁人员）
     */
    List<User> getAllStaff();

    /**
     * 统计特定角色的用户数量
     */
    Long countUsersByRole(User.UserRole role);
    
    /**
     * 统计所有用户数量
     */
    long countAllUsers();
    
    /**
     * 统计当月新注册用户数量
     */
    int countNewUsersThisMonth();

    /**
     * 检查手机号是否已被注册
     */
    boolean existsByPhone(String phone);

    // 会员相关方法
    User updateMemberLevel(Long userId);
    User addPoints(Long userId, int points);
    User addSpending(Long userId, BigDecimal amount);
    User redeemPoints(Long userId, int points);
    String getMemberLevelByUserId(Long userId);
    BigDecimal getDiscountByUserId(Long userId);
    int getPointsRateByUserId(Long userId);

    /**
     * 根据条件分页和过滤用户列表
     */
    Page<UserDTO> findUsersPaginatedAndFiltered(int page, int pageSize, String username, String phone, String status, String role);
    
    /**
     * 根据条件分页和过滤员工列表 (RECEPTIONIST, CLEANER)
     */
    Page<UserDTO> findStaffPaginatedAndFiltered(int page, int pageSize, String username, String phone, String status);

    // 新增员工注册方法声明
    User registerStaff(StaffRegistrationDto dto);

    /**
     * 获取当前登录的用户信息
     * @return 当前登录的User对象，如果用户未登录则可能返回null或抛出异常
     */
    User getCurrentUser();
}