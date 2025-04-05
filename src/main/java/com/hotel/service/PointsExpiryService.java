package com.hotel.service;

import com.hotel.entity.PointsExpiryRecord;
import com.hotel.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 积分到期服务接口
 */
public interface PointsExpiryService {
    /**
     * 添加积分过期记录
     */
    PointsExpiryRecord addExpiryRecord(PointsExpiryRecord record);
    
    /**
     * 为用户创建积分过期记录
     */
    PointsExpiryRecord createExpiryRecord(User user, Integer points, LocalDateTime expiryDate, String remarks);
    
    /**
     * 获取用户的所有积分过期记录
     */
    List<PointsExpiryRecord> getUserExpiryRecords(Long userId);
    
    /**
     * 获取即将过期的积分记录（n天内）
     */
    List<PointsExpiryRecord> getUpcomingExpiryRecords(int daysBeforeExpiry);
    
    /**
     * 获取用户即将过期的积分记录（n天内）
     */
    List<PointsExpiryRecord> getUserUpcomingExpiryRecords(Long userId, int daysBeforeExpiry);
    
    /**
     * 标记已通知
     */
    void markAsNotified(Long recordId);
    
    /**
     * 删除过期的积分记录
     */
    void deleteExpiredRecords();
    
    /**
     * 获取到期记录详情
     */
    PointsExpiryRecord getExpiryRecordById(Long id);
    
    /**
     * 获取用户的所有到期记录
     */
    List<PointsExpiryRecord> getExpiryRecordsByUser(User user);
    
    /**
     * 分页获取用户的到期记录
     */
    Page<PointsExpiryRecord> getExpiryRecordsByUser(User user, Pageable pageable);
    
    /**
     * 获取用户在指定时间范围内即将到期的积分
     */
    List<PointsExpiryRecord> getExpiryRecordsByUserAndDateRange(User user, LocalDateTime start, LocalDateTime end);
    
    /**
     * 统计用户总过期积分
     */
    Integer getTotalExpiredPointsByUser(User user);
    
    /**
     * 统计用户在指定时间范围内过期的积分
     */
    Integer getTotalExpiredPointsByUserAndDateRange(User user, LocalDateTime start, LocalDateTime end);
    
    /**
     * 给用户添加积分并创建相应的到期记录
     * @param userId 用户ID
     * @param points 积分数量
     * @param source 积分来源描述
     * @return 用户对象
     */
    User addPointsWithExpiry(Long userId, Integer points, String source);
    
    /**
     * 处理已过期的积分（从用户账户中扣除）
     */
    void processExpiredPoints();
    
    /**
     * 发送即将过期积分提醒
     */
    void sendExpiryReminders(int daysBeforeExpiry);
} 