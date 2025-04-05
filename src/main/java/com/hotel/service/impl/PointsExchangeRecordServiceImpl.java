package com.hotel.service.impl;

import com.hotel.entity.PointsExchangeRecord;
import com.hotel.entity.User;
import com.hotel.repository.PointsExchangeRecordRepository;
import com.hotel.service.PointsExchangeRecordService;
import com.hotel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PointsExchangeRecordServiceImpl implements PointsExchangeRecordService {

    @Autowired
    private PointsExchangeRecordRepository pointsExchangeRecordRepository;
    
    @Autowired
    private UserService userService;

    @Override
    public PointsExchangeRecord createExchangeRecord(PointsExchangeRecord record) {
        if (record.getCreateTime() == null) {
            record.setCreateTime(LocalDateTime.now());
        }
        return pointsExchangeRecordRepository.save(record);
    }

    @Override
    @Transactional
    public PointsExchangeRecord submitExchangeRequest(Long userId, Integer points, 
                                                     PointsExchangeRecord.ExchangeType type, 
                                                     String description) {
        if (points <= 0) {
            throw new IllegalArgumentException("兑换积分必须为正数");
        }
        
        User user = userService.getUserById(userId);
        
        // 检查用户积分是否足够
        if (user.getPoints() < points) {
            throw new RuntimeException("积分不足，当前积分: " + user.getPoints() + ", 需要积分: " + points);
        }
        
        // 计算兑换等价值
        BigDecimal cashValue = calculateCashValue(points);
        
        // 创建兑换记录
        PointsExchangeRecord record = new PointsExchangeRecord();
        record.setUser(user);
        record.setPointsUsed(points);
        record.setCashValue(cashValue);
        record.setExchangeType(type);
        record.setDescription(description);
        record.setStatus(PointsExchangeRecord.ExchangeStatus.PENDING); // 初始状态为待处理
        record.setCreateTime(LocalDateTime.now());
        
        // 保存兑换记录
        PointsExchangeRecord savedRecord = createExchangeRecord(record);
        
        // 暂时不扣除积分，等到管理员确认后再扣除
        return savedRecord;
    }

    @Override
    @Transactional
    public PointsExchangeRecord processExchangeRequest(Long recordId, 
                                                     PointsExchangeRecord.ExchangeStatus newStatus, 
                                                     Long operatorId) {
        PointsExchangeRecord record = getExchangeRecordById(recordId);
        
        // 只有待处理状态的请求才能被处理
        if (record.getStatus() != PointsExchangeRecord.ExchangeStatus.PENDING) {
            throw new RuntimeException("只有待处理状态的兑换请求才能被处理");
        }
        
        // 更新状态
        record.setStatus(newStatus);
        record.setOperatorId(operatorId);
        
        // 如果状态是已完成，则更新兑换时间并扣除用户积分
        if (newStatus == PointsExchangeRecord.ExchangeStatus.COMPLETED) {
            record.setExchangeTime(LocalDateTime.now());
            
            // 扣除用户积分
            User user = record.getUser();
            userService.redeemPoints(user.getId(), record.getPointsUsed());
        }
        
        // 保存更新
        return pointsExchangeRecordRepository.save(record);
    }

    @Override
    public PointsExchangeRecord getExchangeRecordById(Long id) {
        return pointsExchangeRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("积分兑换记录不存在"));
    }

    @Override
    public List<PointsExchangeRecord> getExchangeRecordsByUser(User user) {
        return pointsExchangeRecordRepository.findByUser(user);
    }

    @Override
    public Page<PointsExchangeRecord> getExchangeRecordsByUser(User user, Pageable pageable) {
        return pointsExchangeRecordRepository.findByUser(user, pageable);
    }

    @Override
    public List<PointsExchangeRecord> getExchangeRecordsByUserAndStatus(User user, PointsExchangeRecord.ExchangeStatus status) {
        return pointsExchangeRecordRepository.findByUserAndStatus(user, status);
    }

    @Override
    public List<PointsExchangeRecord> getExchangeRecordsByUserAndType(User user, PointsExchangeRecord.ExchangeType type) {
        return pointsExchangeRecordRepository.findByUserAndExchangeType(user, type);
    }

    @Override
    public List<PointsExchangeRecord> getExchangeRecordsByUserAndTimeRange(User user, LocalDateTime start, LocalDateTime end) {
        return pointsExchangeRecordRepository.findByUserAndCreateTimeBetween(user, start, end);
    }

    @Override
    public Integer getTotalPointsUsedByUser(User user) {
        Integer sum = pointsExchangeRecordRepository.sumPointsUsedByUser(user);
        return sum != null ? sum : 0;
    }

    @Override
    public BigDecimal calculateCashValue(Integer points) {
        // 积分兑换比例：10积分 = 1元
        return new BigDecimal(points).divide(BigDecimal.valueOf(10), 2, BigDecimal.ROUND_DOWN);
    }

    @Override
    public List<PointsExchangeRecord> getPendingExchangeRequests() {
        return pointsExchangeRecordRepository.findByStatus(PointsExchangeRecord.ExchangeStatus.PENDING);
    }
} 