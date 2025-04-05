package com.hotel.service.impl;

import com.hotel.entity.PointsExpiryRecord;
import com.hotel.entity.User;
import com.hotel.repository.PointsExpiryRecordRepository;
import com.hotel.service.PointsExpiryService;
import com.hotel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PointsExpiryServiceImpl implements PointsExpiryService {

    @Autowired
    private PointsExpiryRecordRepository pointsExpiryRecordRepository;
    
    @Autowired
    private UserService userService;

    @Override
    public PointsExpiryRecord addExpiryRecord(PointsExpiryRecord record) {
        if (record.getCreateTime() == null) {
            record.setCreateTime(LocalDateTime.now());
        }
        return pointsExpiryRecordRepository.save(record);
    }

    @Override
    public PointsExpiryRecord createExpiryRecord(User user, Integer points, LocalDateTime expiryDate, String remarks) {
        PointsExpiryRecord record = new PointsExpiryRecord();
        record.setUser(user);
        record.setPoints(points);
        record.setExpiryDate(expiryDate);
        record.setRemarks(remarks);
        record.setNotified(false);
        record.setCreateTime(LocalDateTime.now());
        
        return addExpiryRecord(record);
    }

    @Override
    public List<PointsExpiryRecord> getUserExpiryRecords(Long userId) {
        User user = userService.getUserById(userId);
        return pointsExpiryRecordRepository.findByUser(user);
    }

    @Override
    public List<PointsExpiryRecord> getUpcomingExpiryRecords(int daysBeforeExpiry) {
        LocalDateTime reminderDate = LocalDateTime.now().plusDays(daysBeforeExpiry);
        return pointsExpiryRecordRepository.findByNotifiedFalseAndExpiryDateBefore(reminderDate);
    }

    @Override
    public List<PointsExpiryRecord> getUserUpcomingExpiryRecords(Long userId, int daysBeforeExpiry) {
        User user = userService.getUserById(userId);
        LocalDateTime reminderDate = LocalDateTime.now().plusDays(daysBeforeExpiry);
        return pointsExpiryRecordRepository.findByUserAndNotifiedFalseAndExpiryDateBefore(user, reminderDate);
    }

    @Override
    public void markAsNotified(Long recordId) {
        PointsExpiryRecord record = getExpiryRecordById(recordId);
        record.setNotified(true);
        record.setNotificationTime(LocalDateTime.now());
        pointsExpiryRecordRepository.save(record);
    }

    @Override
    public void deleteExpiredRecords() {
        LocalDateTime now = LocalDateTime.now();
        List<PointsExpiryRecord> expiredRecords = pointsExpiryRecordRepository.findByExpiryDateBefore(now);
        pointsExpiryRecordRepository.deleteAll(expiredRecords);
    }

    @Override
    public PointsExpiryRecord getExpiryRecordById(Long id) {
        return pointsExpiryRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("积分到期记录不存在"));
    }

    @Override
    public List<PointsExpiryRecord> getExpiryRecordsByUser(User user) {
        return pointsExpiryRecordRepository.findByUser(user);
    }

    @Override
    public Page<PointsExpiryRecord> getExpiryRecordsByUser(User user, Pageable pageable) {
        return pointsExpiryRecordRepository.findByUser(user, pageable);
    }

    @Override
    public List<PointsExpiryRecord> getExpiryRecordsByUserAndDateRange(User user, LocalDateTime start, LocalDateTime end) {
        return pointsExpiryRecordRepository.findByUserAndExpiryDateBetween(user, start, end);
    }

    @Override
    public Integer getTotalExpiredPointsByUser(User user) {
        Integer sum = pointsExpiryRecordRepository.sumPointsByUser(user);
        return sum != null ? sum : 0;
    }

    @Override
    public Integer getTotalExpiredPointsByUserAndDateRange(User user, LocalDateTime start, LocalDateTime end) {
        Integer sum = pointsExpiryRecordRepository.sumPointsByUserAndExpiryDateBetween(user, start, end);
        return sum != null ? sum : 0;
    }

    @Override
    @Transactional
    public User addPointsWithExpiry(Long userId, Integer points, String source) {
        User user = userService.getUserById(userId);
        
        // 添加积分
        user.setPoints(user.getPoints() + points);
        
        // 计算到期日期
        LocalDateTime expiryDate = user.calculatePointsExpiryDate();
        
        // 创建到期记录
        createExpiryRecord(user, points, expiryDate, source);
        
        // 保存用户
        return userService.updateUser(user);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 0 * * ?") // 每天凌晨执行
    public void processExpiredPoints() {
        // 获取今天已过期的记录
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime today = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0);
        
        List<PointsExpiryRecord> expiredRecords = pointsExpiryRecordRepository.findByExpiryDateBetween(
                today.minusDays(1), today);
        
        // 处理每条过期记录
        for (PointsExpiryRecord record : expiredRecords) {
            User user = record.getUser();
            Integer expiredPoints = record.getPoints();
            
            // 如果用户积分足够扣除
            if (user.getPoints() >= expiredPoints) {
                user.setPoints(user.getPoints() - expiredPoints);
                userService.updateUser(user);
                
                // 添加备注
                record.setRemarks(record.getRemarks() + " (已扣除)");
                pointsExpiryRecordRepository.save(record);
            } else {
                // 如果用户积分不足，则全部扣完
                record.setRemarks(record.getRemarks() + " (部分扣除: " + user.getPoints() + ")");
                user.setPoints(0);
                userService.updateUser(user);
                pointsExpiryRecordRepository.save(record);
            }
        }
    }

    @Override
    @Scheduled(cron = "0 0 10 * * ?") // 每天上午10点执行
    public void sendExpiryReminders(int daysBeforeExpiry) {
        // 获取即将过期的记录
        List<PointsExpiryRecord> upcomingRecords = getUpcomingExpiryRecords(daysBeforeExpiry);
        
        // 按用户分组处理提醒
        upcomingRecords.forEach(record -> {
            // TODO: 实际项目中应该调用邮件或短信服务发送提醒
            System.out.println("发送提醒: 用户" + record.getUser().getUsername() + 
                    "有" + record.getPoints() + "积分将在" + 
                    record.getExpiryDate() + "过期");
            
            // 标记为已通知
            markAsNotified(record.getId());
        });
    }
} 