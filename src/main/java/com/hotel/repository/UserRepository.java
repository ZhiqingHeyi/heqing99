package com.hotel.repository;

import com.hotel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
    
    boolean existsByPhone(String phone);
    
    Optional<User> findByPhone(String phone);
    
    Optional<User> findByName(String name);

    List<User> findByRole(User.UserRole role);

    List<User> findByRoleAndStatus(User.UserRole role, String status);

    @Query("SELECT u FROM User u WHERE (u.role = 'ADMIN' OR u.role = 'RECEPTIONIST' OR u.role = 'CLEANER') AND u.status = 'ACTIVE'")
    List<User> findAllActiveStaff();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = ?1")
    long countByRole(User.UserRole role);
    
    /**
     * 统计指定时间范围内注册的用户数量
     */
    int countByCreateTimeBetween(LocalDateTime start, LocalDateTime end);
}