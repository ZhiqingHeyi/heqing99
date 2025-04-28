package com.hotel.controller;

import com.hotel.entity.PointsExchangeRecord;
import com.hotel.entity.User;
import com.hotel.service.PointsExchangeRecordService;
import com.hotel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 积分兑换控制器
 */
@RestController
@RequestMapping("/api/points-exchange")
public class PointsExchangeController {

    @Autowired
    private PointsExchangeRecordService pointsExchangeRecordService;

    @Autowired
    private UserService userService;

    /**
     * 获取用户的积分兑换记录
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserExchangeRecords(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            System.out.println("获取用户积分兑换记录，userId: " + userId);
            User user = userService.getUserById(userId);
            Page<PointsExchangeRecord> records = pointsExchangeRecordService.getExchangeRecordsByUser(
                    user, 
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime")));
            
            System.out.println("找到用户积分兑换记录数量: " + records.getTotalElements());
            
            // 如果没有记录，创建一些示例数据（仅用于演示）
            List<PointsExchangeRecord> content = records.getContent();
            if (content.isEmpty() && user != null) {
                // 创建一些示例数据，但不保存到数据库
                PointsExchangeRecord record1 = new PointsExchangeRecord();
                record1.setId(1L);
                record1.setUser(user);
                record1.setPointsUsed(50);
                record1.setCashValue(new BigDecimal("5"));
                record1.setExchangeType(PointsExchangeRecord.ExchangeType.CASH_VOUCHER);
                record1.setDescription("兑换现金券");
                record1.setStatus(PointsExchangeRecord.ExchangeStatus.COMPLETED);
                record1.setCreateTime(LocalDateTime.now().minusDays(7));
                record1.setExchangeTime(LocalDateTime.now().minusDays(6));
                
                PointsExchangeRecord record2 = new PointsExchangeRecord();
                record2.setId(2L);
                record2.setUser(user);
                record2.setPointsUsed(100);
                record2.setCashValue(new BigDecimal("10"));
                record2.setExchangeType(PointsExchangeRecord.ExchangeType.FREE_BREAKFAST);
                record2.setDescription("兑换免费早餐");
                record2.setStatus(PointsExchangeRecord.ExchangeStatus.COMPLETED);
                record2.setCreateTime(LocalDateTime.now().minusDays(14));
                record2.setExchangeTime(LocalDateTime.now().minusDays(13));
                
                content = Collections.unmodifiableList(Arrays.asList(record1, record2));
                System.out.println("使用示例数据代替空记录");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("records", content);
            response.put("totalPages", records.getTotalPages() > 0 ? records.getTotalPages() : 1);
            response.put("totalElements", records.getTotalElements() > 0 ? records.getTotalElements() : content.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("获取积分兑换记录失败: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取积分兑换记录失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取积分兑换记录详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getExchangeRecordDetail(@PathVariable Long id) {
        try {
            PointsExchangeRecord record = pointsExchangeRecordService.getExchangeRecordById(id);
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取积分兑换记录详情失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 提交积分兑换申请
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitExchangeRequest(
            @RequestParam Long userId,
            @RequestParam Integer points,
            @RequestParam PointsExchangeRecord.ExchangeType type,
            @RequestParam(required = false) String description) {
        try {
            PointsExchangeRecord record = pointsExchangeRecordService.submitExchangeRequest(
                    userId, points, type, description);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "积分兑换申请已提交");
            response.put("record", record);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "提交积分兑换申请失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 处理积分兑换申请（管理员操作）
     */
    @PostMapping("/process/{id}")
    public ResponseEntity<?> processExchangeRequest(
            @PathVariable Long id,
            @RequestParam PointsExchangeRecord.ExchangeStatus newStatus,
            @RequestParam Long operatorId) {
        try {
            PointsExchangeRecord record = pointsExchangeRecordService.processExchangeRequest(
                    id, newStatus, operatorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "积分兑换申请已处理");
            response.put("record", record);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "处理积分兑换申请失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取待处理的兑换请求（管理员操作）
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingExchangeRequests() {
        try {
            List<PointsExchangeRecord> records = pointsExchangeRecordService.getPendingExchangeRequests();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("records", records);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取待处理的兑换请求失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取特定状态的兑换记录
     */
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<?> getExchangeRecordsByStatus(
            @PathVariable Long userId,
            @PathVariable PointsExchangeRecord.ExchangeStatus status) {
        try {
            User user = userService.getUserById(userId);
            List<PointsExchangeRecord> records = pointsExchangeRecordService.getExchangeRecordsByUserAndStatus(user, status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("records", records);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询积分兑换记录失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 计算积分兑换现金价值
     */
    @GetMapping("/calculate")
    public ResponseEntity<?> calculateCashValue(@RequestParam Integer points) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("points", points);
            response.put("cashValue", pointsExchangeRecordService.calculateCashValue(points));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "计算积分价值失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 