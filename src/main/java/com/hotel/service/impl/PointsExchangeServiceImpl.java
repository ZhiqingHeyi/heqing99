package com.hotel.service.impl;

import com.hotel.entity.PointsExchangeRecord;
import com.hotel.entity.PointsExchangeRecord.ExchangeStatus;
import com.hotel.entity.PointsExchangeRecord.ExchangeType;
import com.hotel.entity.User;
import com.hotel.service.PointsExchangeService;
import com.hotel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PointsExchangeServiceImpl implements PointsExchangeService {

    @Autowired
    private UserService userService;

    // 临时存储兑换记录的列表，实际项目中应该使用数据库
    private final List<PointsExchangeRecord> exchangeRecords = new ArrayList<>();
    private Long nextId = 1L;

    // 临时存储可兑换商品的Map，实际项目中应该从数据库获取
    private final Map<String, Map<String, Object>> exchangeableItems = new HashMap<>();

    public PointsExchangeServiceImpl() {
        // 初始化一些示例可兑换商品
        Map<String, Object> item1 = new HashMap<>();
        item1.put("code", "ROOMUPGRADE");
        item1.put("name", "房间升级");
        item1.put("description", "使用积分升级到更高级的房间");
        item1.put("points", 500);
        item1.put("type", ExchangeType.ROOM_DISCOUNT);
        item1.put("cashValue", new BigDecimal("50.00"));
        exchangeableItems.put("ROOMUPGRADE", item1);

        Map<String, Object> item2 = new HashMap<>();
        item2.put("code", "FREESTAY");
        item2.put("name", "免费住宿");
        item2.put("description", "兑换一晚免费住宿");
        item2.put("points", 1000);
        item2.put("type", ExchangeType.CASH_VOUCHER);
        item2.put("cashValue", new BigDecimal("200.00"));
        exchangeableItems.put("FREESTAY", item2);

        Map<String, Object> item3 = new HashMap<>();
        item3.put("code", "BREAKFAST");
        item3.put("name", "免费早餐");
        item3.put("description", "兑换一次免费早餐");
        item3.put("points", 200);
        item3.put("type", ExchangeType.FREE_BREAKFAST);
        item3.put("cashValue", new BigDecimal("30.00"));
        exchangeableItems.put("BREAKFAST", item3);
    }

    @Override
    public PointsExchangeRecord exchangePoints(Long userId, String itemCode, Integer points) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        Map<String, Object> item = exchangeableItems.get(itemCode);
        if (item == null) {
            throw new RuntimeException("兑换项目不存在");
        }

        int requiredPoints = (int) item.get("points");
        if (points < requiredPoints) {
            throw new RuntimeException("积分不足");
        }

        // 创建兑换记录
        PointsExchangeRecord record = new PointsExchangeRecord();
        record.setId(nextId++);
        record.setUser(user);
        record.setPointsUsed(points);
        record.setCashValue((BigDecimal) item.get("cashValue"));
        record.setExchangeType((ExchangeType) item.get("type"));
        record.setDescription((String) item.get("name"));
        record.setStatus(ExchangeStatus.COMPLETED);
        record.setExchangeTime(LocalDateTime.now());

        // 扣减用户积分（实际项目中需要调用userService的相关方法）
        // userService.deductPoints(userId, points);

        exchangeRecords.add(record);
        return record;
    }

    @Override
    public PointsExchangeRecord getExchangeRecordById(Long id) {
        return exchangeRecords.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<PointsExchangeRecord> getUserExchangeRecords(Long userId) {
        return exchangeRecords.stream()
                .filter(r -> r.getUser().getId().equals(userId))
                .toList();
    }

    @Override
    public List<Map<String, Object>> getAllExchangeableItems() {
        return new ArrayList<>(exchangeableItems.values());
    }

    @Override
    public boolean isPointsSufficient(Long userId, String itemCode) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return false;
        }

        Map<String, Object> item = exchangeableItems.get(itemCode);
        if (item == null) {
            return false;
        }

        // 假设User类有getPoints方法来获取用户积分
        // int userPoints = user.getPoints();
        int userPoints = 1000; // 默认值，实际项目中应该从用户对象获取
        
        int requiredPoints = (int) item.get("points");
        return userPoints >= requiredPoints;
    }

    @Override
    public List<PointsExchangeRecord> getExchangeRecordsByUserAndTimeRange(Long userId, String startDateStr, String endDateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime startDate = LocalDateTime.parse(startDateStr, formatter);
        LocalDateTime endDate = LocalDateTime.parse(endDateStr, formatter);

        return exchangeRecords.stream()
                .filter(r -> r.getUser().getId().equals(userId))
                .filter(r -> !r.getExchangeTime().isBefore(startDate) && !r.getExchangeTime().isAfter(endDate))
                .toList();
    }

    @Override
    public Map<String, Object> getExchangeStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 计算总兑换次数
        statistics.put("totalExchanges", exchangeRecords.size());
        
        // 计算总兑换积分
        int totalPoints = exchangeRecords.stream()
                .mapToInt(PointsExchangeRecord::getPointsUsed)
                .sum();
        statistics.put("totalPointsExchanged", totalPoints);
        
        // 计算各类商品兑换情况
        Map<String, Integer> typeStats = new HashMap<>();
        for (PointsExchangeRecord record : exchangeRecords) {
            ExchangeType type = record.getExchangeType();
            String typeKey = type.name();
            typeStats.put(typeKey, typeStats.getOrDefault(typeKey, 0) + 1);
        }
        statistics.put("typeStatistics", typeStats);
        
        return statistics;
    }
} 