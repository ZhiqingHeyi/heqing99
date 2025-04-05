package com.hotel.controller;

import com.hotel.entity.ConsumptionRecord;
import com.hotel.entity.User;
import com.hotel.service.ConsumptionRecordService;
import com.hotel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消费记录控制器
 */
@RestController
@RequestMapping("/api/consumption-records")
public class ConsumptionRecordController {

    @Autowired
    private ConsumptionRecordService consumptionRecordService;

    @Autowired
    private UserService userService;

    /**
     * 获取用户的消费记录列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserConsumptionRecords(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            User user = userService.getUserById(userId);
            Page<ConsumptionRecord> records = consumptionRecordService.getConsumptionRecordsByUser(
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
            response.put("message", "获取消费记录失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取用户消费记录详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getConsumptionRecordDetail(@PathVariable Long id) {
        try {
            ConsumptionRecord record = consumptionRecordService.getConsumptionRecordById(id);
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取消费记录详情失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 创建消费记录
     */
    @PostMapping
    public ResponseEntity<?> createConsumptionRecord(
            @RequestParam Long userId,
            @RequestParam BigDecimal amount,
            @RequestParam ConsumptionRecord.ConsumptionType type,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long reservationId,
            @RequestParam(required = false) Long roomId) {
        try {
            User user = consumptionRecordService.recordConsumptionAndUpdateMembership(
                    userId, amount, type, description, reservationId, roomId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "消费记录已创建");
            response.put("user", user);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建消费记录失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 按时间范围查询用户消费记录
     */
    @GetMapping("/user/{userId}/time-range")
    public ResponseEntity<?> getConsumptionRecordsByTimeRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            User user = userService.getUserById(userId);
            List<ConsumptionRecord> records = consumptionRecordService.getConsumptionRecordsByUserAndTimeRange(user, start, end);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("records", records);
            response.put("totalAmount", consumptionRecordService.getTotalAmountByUserAndTimeRange(user, start, end));
            response.put("totalPoints", consumptionRecordService.getTotalPointsEarnedByUserAndTimeRange(user, start, end));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询消费记录失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 按消费类型查询用户消费记录
     */
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<?> getConsumptionRecordsByType(
            @PathVariable Long userId,
            @PathVariable ConsumptionRecord.ConsumptionType type) {
        try {
            User user = userService.getUserById(userId);
            List<ConsumptionRecord> records = consumptionRecordService.getConsumptionRecordsByUserAndType(user, type);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("records", records);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询消费记录失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取用户消费金额最高的记录
     */
    @GetMapping("/user/{userId}/top")
    public ResponseEntity<?> getTopConsumptionRecords(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            List<ConsumptionRecord> records = consumptionRecordService.getTopConsumptionRecordsByUser(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("records", records);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询消费记录失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除消费记录
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteConsumptionRecord(@PathVariable Long id) {
        try {
            consumptionRecordService.deleteConsumptionRecord(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "消费记录已删除");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除消费记录失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 