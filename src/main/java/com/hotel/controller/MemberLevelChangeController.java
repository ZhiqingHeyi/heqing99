package com.hotel.controller;

import com.hotel.entity.MemberLevel;
import com.hotel.entity.MemberLevelChangeRecord;
import com.hotel.entity.User;
import com.hotel.service.MemberLevelChangeRecordService;
import com.hotel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会员等级变更记录控制器
 */
@RestController
@RequestMapping("/api/member-level-changes")
public class MemberLevelChangeController {

    @Autowired
    private MemberLevelChangeRecordService memberLevelChangeRecordService;

    @Autowired
    private UserService userService;

    /**
     * 获取用户的等级变更记录列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserLevelChangeRecords(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            User user = userService.getUserById(userId);
            Page<MemberLevelChangeRecord> records = memberLevelChangeRecordService.getChangeRecordsByUser(
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
            response.put("message", "获取等级变更记录失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取等级变更记录详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getLevelChangeRecordDetail(@PathVariable Long id) {
        try {
            MemberLevelChangeRecord record = memberLevelChangeRecordService.getChangeRecordById(id);
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取等级变更记录详情失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 创建等级变更记录（主要用于管理操作）
     */
    @PostMapping
    public ResponseEntity<?> createLevelChangeRecord(
            @RequestParam Long userId,
            @RequestParam MemberLevel oldLevel,
            @RequestParam MemberLevel newLevel,
            @RequestParam String reason) {
        try {
            User user = userService.getUserById(userId);
            MemberLevelChangeRecord record = memberLevelChangeRecordService.createChangeRecord(
                    user, oldLevel, newLevel, reason);
            
            // 更新用户会员等级
            user.setMemberLevel(newLevel);
            userService.updateUser(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "等级变更记录已创建");
            response.put("record", record);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建等级变更记录失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取用户最新的等级变更记录
     */
    @GetMapping("/user/{userId}/latest")
    public ResponseEntity<?> getLatestLevelChangeRecord(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            MemberLevelChangeRecord record = memberLevelChangeRecordService.getLatestChangeRecordByUser(user);
            
            if (record == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "用户还没有等级变更记录");
                return ResponseEntity.ok(response);
            }
            
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取最新等级变更记录失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 按时间范围查询用户等级变更记录
     */
    @GetMapping("/user/{userId}/time-range")
    public ResponseEntity<?> getLevelChangeRecordsByTimeRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            User user = userService.getUserById(userId);
            List<MemberLevelChangeRecord> records = memberLevelChangeRecordService.getChangeRecordsByUserAndTimeRange(user, start, end);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("records", records);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询等级变更记录失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 统计各等级的用户数量
     */
    @GetMapping("/stats/by-level")
    public ResponseEntity<?> getStatsByLevel() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            for (MemberLevel level : MemberLevel.values()) {
                long count = memberLevelChangeRecordService.countChangeRecordsByNewLevel(level);
                stats.put(level.name(), count);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取等级统计失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 