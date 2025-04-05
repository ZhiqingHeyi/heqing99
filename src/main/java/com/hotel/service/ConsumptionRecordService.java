package com.hotel.service;

import com.hotel.entity.ConsumptionRecord;
import com.hotel.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 消费记录服务接口
 */
public interface ConsumptionRecordService {
    /**
     * 创建消费记录
     */
    ConsumptionRecord createConsumptionRecord(ConsumptionRecord record);
    
    /**
     * 获取消费记录详情
     */
    ConsumptionRecord getConsumptionRecordById(Long id);
    
    /**
     * 获取用户的所有消费记录
     */
    List<ConsumptionRecord> getConsumptionRecordsByUser(User user);
    
    /**
     * 分页获取用户的消费记录
     */
    Page<ConsumptionRecord> getConsumptionRecordsByUser(User user, Pageable pageable);
    
    /**
     * 按时间范围查询用户的消费记录
     */
    List<ConsumptionRecord> getConsumptionRecordsByUserAndTimeRange(User user, LocalDateTime start, LocalDateTime end);
    
    /**
     * 按消费类型查询用户的消费记录
     */
    List<ConsumptionRecord> getConsumptionRecordsByUserAndType(User user, String type);
    
    /**
     * 统计用户在一段时间内的消费总额
     */
    BigDecimal getTotalAmountByUserAndTimeRange(User user, LocalDateTime start, LocalDateTime end);
    
    /**
     * 统计用户在一段时间内获得的积分总额
     */
    Integer getTotalPointsEarnedByUserAndTimeRange(User user, LocalDateTime start, LocalDateTime end);
    
    /**
     * 获取消费金额最高的记录
     */
    List<ConsumptionRecord> getTopConsumptionRecordsByUser(User user);
    
    /**
     * 删除消费记录
     */
    void deleteConsumptionRecord(Long id);
    
    /**
     * 记录用户消费并更新会员信息（积分、等级等）
     */
    User recordConsumptionAndUpdateMembership(Long userId, BigDecimal amount, 
                                              String type, 
                                              String description, 
                                              Long reservationId, 
                                              Long roomId);

    /**
     * 添加消费记录
     */
    ConsumptionRecord addConsumptionRecord(ConsumptionRecord record);
    
    /**
     * 为用户添加消费记录
     */
    ConsumptionRecord addConsumptionForUser(User user, BigDecimal amount, String type, String description);
    
    /**
     * 获取用户的所有消费记录
     */
    List<ConsumptionRecord> getUserConsumptionRecords(Long userId);
    
    /**
     * 获取指定时间范围内的消费记录
     */
    List<ConsumptionRecord> getConsumptionRecordsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 获取用户指定时间范围内的消费记录
     */
    List<ConsumptionRecord> getUserConsumptionRecordsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 获取消费统计数据
     */
    List<Object[]> getConsumptionStatistics();
    
    /**
     * 获取指定类型的消费统计
     */
    BigDecimal getTotalAmountByType(String type);
} 