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
        if (record.getCreateTime() == null) {
            record.setCreateTime(LocalDateTime.now());
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
        return consumptionRecordRepository.findByUserAndCreateTimeBetween(user, start, end);
    }

    @Override
    public List<ConsumptionRecord> getConsumptionRecordsByUserAndType(User user, ConsumptionRecord.ConsumptionType type) {
        return consumptionRecordRepository.findByUserAndType(user, type);
    }

    @Override
    public BigDecimal getTotalAmountByUserAndTimeRange(User user, LocalDateTime start, LocalDateTime end) {
        BigDecimal sum = consumptionRecordRepository.sumAmountByUserAndCreateTimeBetween(user, start, end);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    public Integer getTotalPointsEarnedByUserAndTimeRange(User user, LocalDateTime start, LocalDateTime end) {
        Integer sum = consumptionRecordRepository.sumPointsEarnedByUserAndCreateTimeBetween(user, start, end);
        return sum != null ? sum : 0;
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
    @Transactional
    public User recordConsumptionAndUpdateMembership(Long userId, BigDecimal amount, 
                                                   ConsumptionRecord.ConsumptionType type, 
                                                   String description, 
                                                   Long reservationId, 
                                                   Long roomId) {
        User user = userService.getUserById(userId);
        
        // 创建消费记录
        ConsumptionRecord record = new ConsumptionRecord();
        record.setUser(user);
        record.setAmount(amount);
        
        // 应用折扣
        double discountRate = user.getDiscountRate();
        record.setDiscountRate(discountRate);
        record.setDiscountedAmount(amount.multiply(BigDecimal.valueOf(discountRate)));
        
        // 计算获得的积分
        int pointsRate = userService.getPointsRateByUserId(userId);
        int pointsEarned = amount.multiply(BigDecimal.valueOf(pointsRate))
                .divide(BigDecimal.valueOf(100), BigDecimal.ROUND_DOWN)
                .intValue();
        record.setPointsEarned(pointsEarned);
        
        record.setType(type);
        record.setDescription(description);
        record.setReservationId(reservationId);
        record.setRoomId(roomId);
        record.setCreateTime(LocalDateTime.now());
        
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