package com.hotel.service;

import com.hotel.entity.MemberLevel;
import com.hotel.entity.MemberLevelChangeRecord;
import com.hotel.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会员等级变更记录服务接口
 */
public interface MemberLevelChangeRecordService {
    /**
     * 创建等级变更记录
     */
    MemberLevelChangeRecord createChangeRecord(MemberLevelChangeRecord record);
    
    /**
     * 创建等级变更记录（便捷方法）
     */
    MemberLevelChangeRecord createChangeRecord(User user, MemberLevel oldLevel, MemberLevel newLevel, String reason);
    
    /**
     * 获取等级变更记录详情
     */
    MemberLevelChangeRecord getChangeRecordById(Long id);
    
    /**
     * 获取用户的所有等级变更记录
     */
    List<MemberLevelChangeRecord> getChangeRecordsByUser(User user);
    
    /**
     * 分页获取用户的等级变更记录
     */
    Page<MemberLevelChangeRecord> getChangeRecordsByUser(User user, Pageable pageable);
    
    /**
     * 获取用户最新的等级变更记录
     */
    MemberLevelChangeRecord getLatestChangeRecordByUser(User user);
    
    /**
     * 按时间范围查询用户的等级变更记录
     */
    List<MemberLevelChangeRecord> getChangeRecordsByUserAndTimeRange(User user, LocalDateTime start, LocalDateTime end);
    
    /**
     * 按变更后等级查询记录
     */
    List<MemberLevelChangeRecord> getChangeRecordsByNewLevel(MemberLevel level);
    
    /**
     * 统计特定等级的变更记录数量
     */
    long countChangeRecordsByNewLevel(MemberLevel level);
} 