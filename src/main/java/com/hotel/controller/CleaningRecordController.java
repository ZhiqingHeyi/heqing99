package com.hotel.controller;

import com.hotel.entity.CleaningRecord;
import com.hotel.entity.Room;
import com.hotel.entity.User;
import com.hotel.service.CleaningService;
import com.hotel.service.RoomService;
import com.hotel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cleaning")
public class CleaningRecordController {

    private static final Logger logger = LoggerFactory.getLogger(CleaningRecordController.class);

    private final CleaningService cleaningService;
    private final RoomService roomService;
    private final UserService userService;

    @Autowired
    public CleaningRecordController(CleaningService cleaningService, RoomService roomService, UserService userService) {
        this.cleaningService = cleaningService;
        this.roomService = roomService;
        this.userService = userService;
    }

    /**
     * 获取清洁记录列表
     */
    @GetMapping("/records")
    public ResponseEntity<?> getCleaningRecords(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            List<CleaningRecord> records = new ArrayList<>();
            
            // 根据参数筛选记录
            if (status != null && !status.isEmpty()) {
                CleaningRecord.CleaningStatus cleaningStatus = CleaningRecord.CleaningStatus.valueOf(status.toUpperCase());
                records = cleaningService.getCleaningRecordsByStatus(cleaningStatus);
            } else if (startDate != null && endDate != null) {
                LocalDateTime startDateTime = startDate.atStartOfDay();
                LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
                records = cleaningService.getCleaningRecordsByTimeRange(startDateTime, endDateTime);
            } else {
                // 默认获取所有记录
                records = cleaningService.getAllCleaningRecords();
            }
            
            // 转换为前端需要的格式
            List<Map<String, Object>> responseRecords = new ArrayList<>();
            for (CleaningRecord record : records) {
                Map<String, Object> recordMap = new HashMap<>();
                recordMap.put("id", record.getId());
                
                Room room = record.getRoom();
                recordMap.put("roomNumber", room != null ? room.getRoomNumber() : "未知房间");
                
                User staff = userService.getUserById(record.getStaffId());
                recordMap.put("cleaner", staff != null ? staff.getName() : "未知人员");
                
                recordMap.put("status", record.getStatus().toString());
                recordMap.put("startTime", record.getStartTime());
                recordMap.put("endTime", record.getEndTime());
                
                // 计算持续时间
                String duration = "未完成";
                if (record.getStartTime() != null && record.getEndTime() != null) {
                    long hours = java.time.Duration.between(record.getStartTime(), record.getEndTime()).toHours();
                    long minutes = java.time.Duration.between(record.getStartTime(), record.getEndTime()).toMinutes() % 60;
                    duration = hours + "." + (minutes >= 30 ? "5" : "0") + "小时";
                } else if (record.getStartTime() != null) {
                    duration = "进行中";
                }
                recordMap.put("duration", duration);
                
                recordMap.put("remarks", record.getNotes());
                responseRecords.add(recordMap);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", records.size());
            response.put("records", responseRecords);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取清洁记录失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取清洁记录失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取清洁记录详情
     */
    @GetMapping("/records/{id}")
    public ResponseEntity<?> getCleaningRecordById(@PathVariable Long id) {
        try {
            CleaningRecord record = cleaningService.getCleaningRecordById(id);
            if (record == null) {
                Map<String, Object> notFoundResponse = new HashMap<>();
                notFoundResponse.put("success", false);
                notFoundResponse.put("message", "清洁记录不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundResponse);
            }
            
            Map<String, Object> recordMap = new HashMap<>();
            recordMap.put("id", record.getId());
            
            Room room = record.getRoom();
            recordMap.put("roomNumber", room != null ? room.getRoomNumber() : "未知房间");
            
            User staff = userService.getUserById(record.getStaffId());
            recordMap.put("cleaner", staff != null ? staff.getName() : "未知人员");
            
            recordMap.put("status", record.getStatus().toString());
            recordMap.put("startTime", record.getStartTime());
            recordMap.put("endTime", record.getEndTime());
            
            // 计算持续时间
            String duration = "未完成";
            if (record.getStartTime() != null && record.getEndTime() != null) {
                long hours = java.time.Duration.between(record.getStartTime(), record.getEndTime()).toHours();
                long minutes = java.time.Duration.between(record.getStartTime(), record.getEndTime()).toMinutes() % 60;
                duration = hours + "." + (minutes >= 30 ? "5" : "0") + "小时";
            } else if (record.getStartTime() != null) {
                duration = "进行中";
            }
            recordMap.put("duration", duration);
            
            recordMap.put("remarks", record.getNotes());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("record", recordMap);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取清洁记录详情失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取清洁记录详情失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
} 