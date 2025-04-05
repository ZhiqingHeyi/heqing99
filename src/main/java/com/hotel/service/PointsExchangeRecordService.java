package com.hotel.service;

import com.hotel.entity.PointsExchangeRecord;
import com.hotel.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 积分兑换记录服务接口
 */
public interface PointsExchangeRecordService {
    /**
     * 创建积分兑换记录
     */
    PointsExchangeRecord createExchangeRecord(PointsExchangeRecord record);
    
    /**
     * 提交积分兑换申请
     */
    PointsExchangeRecord submitExchangeRequest(Long userId, Integer points, 
                                              PointsExchangeRecord.ExchangeType type, 
                                              String description);
    
    /**
     * 处理积分兑换申请
     */
    PointsExchangeRecord processExchangeRequest(Long recordId, 
                                               PointsExchangeRecord.ExchangeStatus newStatus, 
                                               Long operatorId);
    
    /**
     * 获取兑换记录详情
     */
    PointsExchangeRecord getExchangeRecordById(Long id);
    
    /**
     * 获取用户的所有兑换记录
     */
    List<PointsExchangeRecord> getExchangeRecordsByUser(User user);
    
    /**
     * 分页获取用户的兑换记录
     */
    Page<PointsExchangeRecord> getExchangeRecordsByUser(User user, Pageable pageable);
    
    /**
     * 获取特定状态的兑换记录
     */
    List<PointsExchangeRecord> getExchangeRecordsByUserAndStatus(User user, PointsExchangeRecord.ExchangeStatus status);
    
    /**
     * 按兑换类型查询记录
     */
    List<PointsExchangeRecord> getExchangeRecordsByUserAndType(User user, PointsExchangeRecord.ExchangeType type);
    
    /**
     * 按时间范围查询记录
     */
    List<PointsExchangeRecord> getExchangeRecordsByUserAndTimeRange(User user, LocalDateTime start, LocalDateTime end);
    
    /**
     * 统计用户兑换使用的总积分
     */
    Integer getTotalPointsUsedByUser(User user);
    
    /**
     * 计算积分兑换现金价值
     */
    BigDecimal calculateCashValue(Integer points);
    
    /**
     * 查询待处理的兑换请求
     */
    List<PointsExchangeRecord> getPendingExchangeRequests();
} 