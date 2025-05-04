package com.hotel.service.impl;

import com.hotel.dto.PointsHistoryDTO;
import com.hotel.entity.ConsumptionRecord;
import com.hotel.entity.PointsExchangeRecord;
import com.hotel.entity.User;
import com.hotel.repository.ConsumptionRecordRepository;
import com.hotel.repository.PointsExchangeRecordRepository;
import com.hotel.service.PointsHistoryService;
import com.hotel.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(PointsHistoryServiceImpl.class);

    @Autowired
    private UserService userService;
    
    @Autowired
    private ConsumptionRecordRepository consumptionRecordRepository;
    
    @Autowired
    private PointsExchangeRecordRepository pointsExchangeRecordRepository;

    @Override
    public Page<PointsHistoryDTO> getUserPointsHistory(Long userId, Pageable pageable) {
        User user = userService.getUserById(userId);
        
        List<ConsumptionRecord> earnRecords = consumptionRecordRepository.findByUser(user);
        log.info("[GET_HISTORY] Found {} consumption records for userId: {}", earnRecords.size(), userId);

        List<PointsExchangeRecord> redeemRecords = pointsExchangeRecordRepository.findByUser(user);
        log.info("[GET_HISTORY] Found {} points exchange records for userId: {}", redeemRecords.size(), userId);
        
        List<PointsHistoryDTO> historyList = new ArrayList<>();
        
        for (ConsumptionRecord record : earnRecords) {
            log.info("[GET_HISTORY] Processing earn record ID: {}, Points Earned from DB: {}", record.getId(), record.getPointsEarned());
            if (record.getPointsEarned() <= 0) {
                 log.info("[GET_HISTORY] Skipping earn record ID: {} because pointsEarned is <= 0", record.getId());
                continue;
            }
            PointsHistoryDTO dto = new PointsHistoryDTO();
            dto.setId(record.getId());
            dto.setUserId(user.getId());
            dto.setPoints(record.getPointsEarned());
            dto.setType("earn");
            dto.setDescription("消费获得积分: " + record.getDescription());
            dto.setOrderNo(record.getReservationId() != null ? "R" + record.getReservationId() : null);
            dto.setCreateTime(record.getConsumptionTime());
            historyList.add(dto);
        }
        
        for (PointsExchangeRecord record : redeemRecords) {
             log.info("[GET_HISTORY] Processing redeem record ID: {}, Points Used: {}", record.getId(), record.getPointsUsed());
            PointsHistoryDTO dto = new PointsHistoryDTO();
            dto.setId(record.getId());
            dto.setUserId(user.getId());
            dto.setPoints(-record.getPointsUsed());
            dto.setType("redeem");
            dto.setDescription("积分兑换: " + record.getDescription());
            dto.setCreateTime(record.getExchangeTime());
            historyList.add(dto);
        }
        
        historyList.sort(Comparator.comparing(PointsHistoryDTO::getCreateTime).reversed());
        
        int balance = user.getPoints();
        log.info("[GET_HISTORY] Starting balance calculation with current user points: {}", balance);
        for (PointsHistoryDTO record : historyList) {
            record.setBalance(balance);
            balance -= record.getPoints();
        }
        log.info("[GET_HISTORY] Calculated initial balance before first record: {}", balance);
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), historyList.size());
        
        if (start >= historyList.size() && historyList.size() > 0) {
             log.warn("[GET_HISTORY] Page start index {} is out of bounds for history size {}. Resetting to page 0.", start, historyList.size());
             start = 0;
             end = Math.min(pageable.getPageSize(), historyList.size());
        } else if (start >= historyList.size()) {
             log.warn("[GET_HISTORY] Page start index {} is out of bounds, history is empty. Returning empty list.", start);
             start = 0;
             end = 0;
        }
        
        List<PointsHistoryDTO> pageContent = (start < end) ? historyList.subList(start, end) : new ArrayList<>();
        log.info("[GET_HISTORY] Returning {} history items for page {} (size {}) for userId: {}. Total items: {}", pageContent.size(), pageable.getPageNumber(), pageable.getPageSize(), userId, historyList.size());
        
        return new PageImpl<>(pageContent, pageable, historyList.size());
    }
} 