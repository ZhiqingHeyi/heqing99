package com.hotel.service;

import com.hotel.entity.PointsExchangeRecord;
import java.util.List;
import java.util.Map;

public interface PointsExchangeService {
    /**
     * 兑换积分
     */
    PointsExchangeRecord exchangePoints(Long userId, String itemCode, Integer points);
    
    /**
     * 获取兑换记录详情
     */
    PointsExchangeRecord getExchangeRecordById(Long id);
    
    /**
     * 获取用户的所有兑换记录
     */
    List<PointsExchangeRecord> getUserExchangeRecords(Long userId);
    
    /**
     * 获取所有可兑换商品/服务
     */
    List<Map<String, Object>> getAllExchangeableItems();
    
    /**
     * 检查用户积分是否足够兑换指定商品
     */
    boolean isPointsSufficient(Long userId, String itemCode);
    
    /**
     * 根据用户ID和时间范围获取兑换记录
     */
    List<PointsExchangeRecord> getExchangeRecordsByUserAndTimeRange(Long userId, String startDate, String endDate);
    
    /**
     * 获取积分兑换统计数据
     */
    Map<String, Object> getExchangeStatistics();
} 