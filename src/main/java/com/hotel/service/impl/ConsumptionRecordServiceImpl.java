package com.hotel.service.impl;

import com.hotel.entity.ConsumptionRecord;
import com.hotel.entity.MemberLevel;
import com.hotel.entity.User;
import com.hotel.repository.ConsumptionRecordRepository;
import com.hotel.service.ConsumptionRecordService;
import com.hotel.service.MemberLevelChangeRecordService;
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
public class ConsumptionRecordServiceImpl implements ConsumptionRecordService {

    @Autowired
    private ConsumptionRecordRepository consumptionRecordRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private MemberLevelChangeRecordService memberLevelChangeRecordService;

    @Override
    public ConsumptionRecord createConsumptionRecord(ConsumptionRecord record) {
        return addConsumptionRecord(record);
    }

    @Override
    public ConsumptionRecord addConsumptionRecord(ConsumptionRecord record) {
        if (record.getConsumptionTime() == null) {
            record.setConsumptionTime(LocalDateTime.now());
        }
        return consumptionRecordRepository.save(record);
    }

    @Override
    public ConsumptionRecord getConsumptionRecordById(Long id) {
        return consumptionRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("消费记录不存在"));
    }

    @Override
    public List<ConsumptionRecord> getConsumptionRecordsByUser(User user) {
        return consumptionRecordRepository.findByUser(user);
    }

    @Override
    public Page<ConsumptionRecord> getConsumptionRecordsByUser(User user, Pageable pageable) {
        return consumptionRecordRepository.findByUser(user, pageable);
    }

    @Override
    public List<ConsumptionRecord> getConsumptionRecordsByUserAndTimeRange(User user, LocalDateTime start, LocalDateTime end) {
        return consumptionRecordRepository.findByUserAndConsumptionTimeBetween(user, start, end);
    }

    @Override
    public List<ConsumptionRecord> getConsumptionRecordsByUserAndType(User user, String type) {
        return consumptionRecordRepository.findByUserAndType(user, type);
    }

    @Override
    public BigDecimal getTotalAmountByUserAndTimeRange(User user, LocalDateTime start, LocalDateTime end) {
        BigDecimal total = consumptionRecordRepository.sumAmountByUserAndConsumptionTimeBetween(user, start, end);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public Integer getTotalPointsEarnedByUserAndTimeRange(User user, LocalDateTime start, LocalDateTime end) {
        Integer total = consumptionRecordRepository.sumPointsEarnedByUserAndConsumptionTimeBetween(user, start, end);
        return total != null ? total : 0;
    }

    @Override
    public List<ConsumptionRecord> getTopConsumptionRecordsByUser(User user) {
        return consumptionRecordRepository.findTop10ByUserOrderByAmountDesc(user);
    }

    @Override
    public void deleteConsumptionRecord(Long id) {
        consumptionRecordRepository.deleteById(id);
    }

    @Override
    public List<ConsumptionRecord> getUserConsumptionRecords(Long userId) {
        User user = userService.getUserById(userId);
        return consumptionRecordRepository.findByUser(user);
    }

    @Override
    public List<ConsumptionRecord> getConsumptionRecordsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return consumptionRecordRepository.findByConsumptionTimeBetween(startDate, endDate);
    }

    @Override
    public List<ConsumptionRecord> getUserConsumptionRecordsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        User user = userService.getUserById(userId);
        return consumptionRecordRepository.findByUserAndConsumptionTimeBetween(user, startDate, endDate);
    }

    @Override
    public List<Object[]> getConsumptionStatistics() {
        // 实现统计逻辑
        return null;
    }

    @Override
    public BigDecimal getTotalAmountByType(String type) {
        // 实现按类型统计金额
        return BigDecimal.ZERO;
    }

    @Override
    public ConsumptionRecord addConsumptionForUser(User user, BigDecimal amount, String type, String description) {
        // 创建消费记录
        ConsumptionRecord record = new ConsumptionRecord();
        record.setUser(user);
        record.setAmount(amount);
        record.setType(type);
        record.setDescription(description);
        record.setConsumptionTime(LocalDateTime.now());
        
        // 设置折扣率和折扣后金额
        double discountRate = user.getDiscountRate();
        record.setDiscountRate(discountRate);
        record.setDiscountedAmount(amount.multiply(BigDecimal.valueOf(discountRate)));
        
        // 计算获得的积分
        int pointsRate = 0;
        switch (user.getMemberLevel()) {
            case BRONZE:
                pointsRate = 100;
                break;
            case SILVER:
                pointsRate = 120;
                break;
            case GOLD:
                pointsRate = 150;
                break;
            case DIAMOND:
                pointsRate = 200;
                break;
            default:
                pointsRate = 0;
        }
        
        int pointsEarned = amount.multiply(BigDecimal.valueOf(pointsRate))
                .divide(BigDecimal.valueOf(100), BigDecimal.ROUND_DOWN)
                .intValue();
        record.setPointsEarned(pointsEarned);
        
        // 保存消费记录
        addConsumptionRecord(record);
        
        // 记录旧会员等级
        MemberLevel oldLevel = user.getMemberLevel();
        
        // 更新用户累计消费和积分
        user.setTotalSpent(user.getTotalSpent().add(amount));
        user.setPoints(user.getPoints() + pointsEarned);
        
        // 更新会员等级
        user.updateMemberLevel();
        
        // 如果会员等级有变化，创建变更记录
        if (!oldLevel.equals(user.getMemberLevel())) {
            memberLevelChangeRecordService.createChangeRecord(
                user, 
                oldLevel, 
                user.getMemberLevel(), 
                "消费升级: " + amount + "元");
        }
        
        // 保存用户更新
        userService.updateUser(user);
        
        return record;
    }

    @Override
    @Transactional
    public User recordConsumptionAndUpdateMembership(Long userId, BigDecimal amount, String type, String description, Long reservationId, Long roomId) {
        User user = userService.getUserById(userId);
        
        // 创建消费记录
        ConsumptionRecord record = new ConsumptionRecord();
        record.setUser(user);
        record.setAmount(amount);
        record.setType(type);
        record.setDescription(description);
        record.setReservationId(reservationId);
        record.setRoomId(roomId);
        record.setConsumptionTime(LocalDateTime.now());
        
        // 应用折扣
        double discountRate = user.getDiscountRate();
        record.setDiscountRate(discountRate);
        record.setDiscountedAmount(amount.multiply(BigDecimal.valueOf(discountRate)));
        
        // 计算获得的积分
        int pointsRate = 0;
        switch (user.getMemberLevel()) {
            case BRONZE:
                pointsRate = 100;
                break;
            case SILVER:
                pointsRate = 120;
                break;
            case GOLD:
                pointsRate = 150;
                break;
            case DIAMOND:
                pointsRate = 200;
                break;
            default:
                pointsRate = 0;
        }
        
        int pointsEarned = amount.multiply(BigDecimal.valueOf(pointsRate))
                .divide(BigDecimal.valueOf(100), BigDecimal.ROUND_DOWN)
                .intValue();
        record.setPointsEarned(pointsEarned);
        
        // 保存消费记录
        createConsumptionRecord(record);
        
        // 记录旧会员等级
        MemberLevel oldLevel = user.getMemberLevel();
        
        // 更新用户累计消费和积分
        user.setTotalSpent(user.getTotalSpent().add(amount));
        user.setPoints(user.getPoints() + pointsEarned);
        
        // 更新会员等级
        user.updateMemberLevel();
        
        // 如果会员等级有变化，创建变更记录
        if (!oldLevel.equals(user.getMemberLevel())) {
            memberLevelChangeRecordService.createChangeRecord(
                user, 
                oldLevel, 
                user.getMemberLevel(), 
                "消费升级: " + amount + "元");
        }
        
        // 保存用户更新
        return userService.updateUser(user);
    }
} 