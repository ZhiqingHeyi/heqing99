package com.hotel.service.impl;

import com.hotel.dto.PointsHistoryDTO;
import com.hotel.entity.ConsumptionRecord;
import com.hotel.entity.PointsExchangeRecord;
import com.hotel.entity.User;
import com.hotel.repository.ConsumptionRecordRepository;
import com.hotel.repository.PointsExchangeRecordRepository;
import com.hotel.service.PointsHistoryService;
import com.hotel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PointsHistoryServiceImpl implements PointsHistoryService {

    @Autowired
    private UserService userService;
    
    @Autowired
    private ConsumptionRecordRepository consumptionRecordRepository;
    
    @Autowired
    private PointsExchangeRecordRepository pointsExchangeRecordRepository;

    @Override
    public Page<PointsHistoryDTO> getUserPointsHistory(Long userId, Pageable pageable) {
        User user = userService.getUserById(userId);
        
        // 获取所有获得积分的记录
        List<ConsumptionRecord> earnRecords = consumptionRecordRepository.findByUser(user);
        
        // 获取所有使用积分的记录
        List<PointsExchangeRecord> redeemRecords = pointsExchangeRecordRepository.findByUser(user);
        
        // 合并两种记录为积分历史DTO
        List<PointsHistoryDTO> historyList = new ArrayList<>();
        
        // 添加消费获得积分记录
        for (ConsumptionRecord record : earnRecords) {
            if (record.getPointsEarned() <= 0) continue;
            
            PointsHistoryDTO dto = new PointsHistoryDTO();
            dto.setId(record.getId());
            dto.setUserId(user.getId());
            dto.setPoints(record.getPointsEarned());
            dto.setType("earn");
            dto.setDescription("消费获得积分: " + record.getDescription());
            dto.setOrderNo(record.getReservationId() != null ? "R" + record.getReservationId() : null);
            // 这里没有实时余额，需要稍后计算
            dto.setCreateTime(record.getConsumptionTime());
            
            historyList.add(dto);
        }
        
        // 添加积分兑换使用记录
        for (PointsExchangeRecord record : redeemRecords) {
            PointsHistoryDTO dto = new PointsHistoryDTO();
            dto.setId(record.getId());
            dto.setUserId(user.getId());
            dto.setPoints(-record.getPointsUsed()); // 使用积分为负数
            dto.setType("redeem");
            dto.setDescription("积分兑换: " + record.getDescription());
            // 这里没有订单号和实时余额
            dto.setCreateTime(record.getExchangeTime());
            
            historyList.add(dto);
        }
        
        // 按时间排序
        historyList.sort(Comparator.comparing(PointsHistoryDTO::getCreateTime).reversed());
        
        // 计算每条记录后的积分余额
        int balance = user.getPoints();
        // 从最新的记录开始，向前计算每一步的余额
        for (PointsHistoryDTO record : historyList) {
            record.setBalance(balance);
            balance -= record.getPoints(); // 减去变动值得到之前的余额
        }
        
        // 分页处理
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), historyList.size());
        
        // 安全检查
        if (start > historyList.size()) {
            start = 0;
            end = Math.min(pageable.getPageSize(), historyList.size());
        }
        
        List<PointsHistoryDTO> pageContent = historyList.subList(start, end);
        return new PageImpl<>(pageContent, pageable, historyList.size());
    }
} 