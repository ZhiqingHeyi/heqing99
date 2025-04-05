package com.hotel.repository;

import com.hotel.entity.PointsExpiryRecord;
import com.hotel.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointsExpiryRecordRepository extends JpaRepository<PointsExpiryRecord, Long> {
    /**
     * 获取用户的所有积分到期记录
     */
    List<PointsExpiryRecord> findByUser(User user);

    /**
     * 分页获取用户的积分到期记录
     */
    Page<PointsExpiryRecord> findByUser(User user, Pageable pageable);
    
    /**
     * 查找即将过期但尚未通知的记录
     */
    List<PointsExpiryRecord> findByNotifiedFalseAndExpiryDateBefore(LocalDateTime date);
    
    /**
     * 查找所有已过期记录
     */
    List<PointsExpiryRecord> findByExpiryDateBefore(LocalDateTime date);
    
    /**
     * 查找用户的即将过期但尚未通知的记录
     */
    List<PointsExpiryRecord> findByUserAndNotifiedFalseAndExpiryDateBefore(User user, LocalDateTime date);
    
    /**
     * 查找指定日期范围内的到期记录
     */
    List<PointsExpiryRecord> findByExpiryDateBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * 按用户查找指定日期范围内的到期记录
     */
    List<PointsExpiryRecord> findByUserAndExpiryDateBetween(User user, LocalDateTime start, LocalDateTime end);
    
    /**
     * 获取总过期积分数量
     */
    @Query("SELECT SUM(p.points) FROM PointsExpiryRecord p WHERE p.user = ?1")
    Integer sumPointsByUser(User user);
    
    /**
     * 统计用户在指定时间范围内过期的积分
     */
    @Query("SELECT SUM(p.points) FROM PointsExpiryRecord p WHERE p.user = ?1 AND p.expiryDate BETWEEN ?2 AND ?3")
    Integer sumPointsByUserAndExpiryDateBetween(User user, LocalDateTime start, LocalDateTime end);
} 