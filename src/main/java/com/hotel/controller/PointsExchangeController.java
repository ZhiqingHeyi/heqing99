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

import java.time.LocalDateTime;
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
            User user = userService.getUserById(userId);
            Page<PointsExchangeRecord> records = pointsExchangeRecordService.getExchangeRecordsByUser(
                    user, 
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime")));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("records", records.getContent());
            response.put("totalPages", records.getTotalPages());
            response.put("totalElements", records.getTotalElements());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
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