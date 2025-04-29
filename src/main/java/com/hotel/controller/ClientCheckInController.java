package com.hotel.controller;

import com.hotel.entity.CheckInRecord;
import com.hotel.service.CheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 客户端入住相关API控制器
 */
@RestController
@RequestMapping("/api/check-in")
public class ClientCheckInController {
    
    @Autowired
    private CheckInService checkInService;
    
    /**
     * 查询当前入住客人列表
     * 
     * @param page 页码，默认0
     * @param size 每页条数，默认10
     * @param sortBy 排序字段(checkInTime/roomNumber/guestName等)
     * @param direction 排序方向(asc/desc)，默认desc
     * @return 当前入住客人分页列表
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentCheckIns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String direction) {
        
        try {
            // 创建排序对象
            Sort sort = null;
            if (sortBy != null && !sortBy.isEmpty()) {
                sort = direction.equalsIgnoreCase("asc") ? 
                    Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            } else {
                // 默认按入住时间降序排序
                sort = Sort.by("checkInDate").descending();
            }
            
            // 查询当前入住的客人（状态为CHECKED_IN）
            Page<CheckInRecord> records = checkInService.getCheckInRecords(
                    CheckInRecord.CheckInStatus.CHECKED_IN,
                    null, null, null, null, null, page, size);
            
            // 构建响应对象
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "获取成功");
            
            Map<String, Object> data = new HashMap<>();
            data.put("content", records.getContent());
            
            Map<String, Object> pageableInfo = new HashMap<>();
            pageableInfo.put("pageNumber", records.getNumber());
            pageableInfo.put("pageSize", records.getSize());
            pageableInfo.put("totalPages", records.getTotalPages());
            pageableInfo.put("totalElements", records.getTotalElements());
            
            data.put("pageable", pageableInfo);
            response.put("data", data);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取当前入住客人列表失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 更新入住信息
     * 
     * @param id 入住记录ID
     * @param updateInfo 更新信息
     * @return 更新后的入住记录
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCheckInInfo(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updateInfo) {
        
        try {
            // 获取现有入住记录
            CheckInRecord checkInRecord = checkInService.getCheckInRecordById(id);
            
            if (checkInRecord == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("code", 404);
                errorResponse.put("message", "入住记录不存在");
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            // 如果客人已退房，不允许更新
            if (checkInRecord.getStatus() == CheckInRecord.CheckInStatus.CHECKED_OUT) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("code", 400);
                errorResponse.put("message", "客人已退房，无法更新入住信息");
                
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            // 解析预计退房时间
            if (updateInfo.containsKey("estimatedCheckOutTime")) {
                LocalDate newCheckOutDate = LocalDate.parse((String) updateInfo.get("estimatedCheckOutTime"));
                String remarks = (String) updateInfo.getOrDefault("remarks", "");
                
                // 调用服务延长入住
                checkInRecord = checkInService.extendStay(id, newCheckOutDate, remarks);
            } else {
                // 更新备注
                if (updateInfo.containsKey("remarks")) {
                    checkInRecord.setRemarks((String) updateInfo.get("remarks"));
                }
                
                // TODO: 更新其他可能的字段，例如additionalGuests等
                
                // 保存更新后的记录
                checkInRecord = checkInService.updateCheckInRecord(checkInRecord);
            }
            
            // 构建响应对象
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "更新成功");
            response.put("data", checkInRecord);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 500);
            errorResponse.put("message", "更新入住信息失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
} 