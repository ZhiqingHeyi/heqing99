package com.hotel.repository;

import com.hotel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
    
    boolean existsByPhone(String phone);
    
    Optional<User> findByPhone(String phone);

    List<User> findByRole(User.UserRole role);

    List<User> findByRoleAndEnabledTrue(User.UserRole role);

    @Query("SELECT u FROM User u WHERE (u.role = 'RECEPTIONIST' OR u.role = 'CLEANER') AND u.enabled = true")
    List<User> findAllActiveStaff();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = ?1")
    long countByRole(User.UserRole role);
}