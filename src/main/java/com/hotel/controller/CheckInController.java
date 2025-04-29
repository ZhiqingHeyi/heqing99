package com.hotel.controller;

import com.hotel.entity.AdditionalCharge;
import com.hotel.entity.CheckInRecord;
import com.hotel.exception.ResourceNotFoundException;
import com.hotel.service.CheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/checkin")
public class CheckInController {
    
    @Autowired
    private CheckInService checkInService;
    
    /**
     * 前台办理入住
     */
    @PostMapping
    public ResponseEntity<?> checkIn(@RequestBody CheckInRecord checkInRecord) {
        try {
            CheckInRecord result = checkInService.checkIn(checkInRecord);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "入住登记成功");
            response.put("data", result);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "入住登记失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    /**
     * 获取入住详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCheckInDetail(@PathVariable Long id) {
        try {
            CheckInRecord checkInRecord = checkInService.getCheckInRecordById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", checkInRecord);
            
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取入住详情失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 获取入住记录列表
     */
    @GetMapping
    public ResponseEntity<?> getCheckInRecords(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) String guestName,
            @RequestParam(required = false) String guestMobile,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            CheckInRecord.CheckInStatus checkInStatus = null;
            if (status != null && !status.isEmpty()) {
                checkInStatus = CheckInRecord.CheckInStatus.valueOf(status);
            }
            
            Page<CheckInRecord> records = checkInService.getCheckInRecords(
                    checkInStatus, roomNumber, guestName, guestMobile, startDate, endDate, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            
            Map<String, Object> data = new HashMap<>();
            data.put("content", records.getContent());
            
            Map<String, Object> pageable = new HashMap<>();
            pageable.put("pageNumber", records.getNumber());
            pageable.put("pageSize", records.getSize());
            pageable.put("totalPages", records.getTotalPages());
            pageable.put("totalElements", records.getTotalElements());
            
            data.put("pageable", pageable);
            response.put("data", data);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取入住记录列表失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 办理退房
     */
    @PostMapping("/checkout/{checkInId}")
    public ResponseEntity<?> checkout(
            @PathVariable Long checkInId,
            @RequestBody Map<String, Object> checkoutInfo) {
        
        try {
            // 解析额外消费记录
            List<AdditionalCharge> additionalCharges = null;
            if (checkoutInfo.containsKey("additionalCharges")) {
                // 需要将Map转换为AdditionalCharge对象列表
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> chargesList = (List<Map<String, Object>>) checkoutInfo.get("additionalCharges");
                additionalCharges = AdditionalCharge.fromList(chargesList);
            }
            
            // 解析退还押金
            BigDecimal returnDeposit = null;
            if (checkoutInfo.containsKey("returnDeposit")) {
                returnDeposit = new BigDecimal(checkoutInfo.get("returnDeposit").toString());
            }
            
            // 解析备注
            String remarks = (String) checkoutInfo.get("remarks");
            
            // 解析支付方式
            CheckInRecord.PaymentMethod paymentMethod = null;
            if (checkoutInfo.containsKey("paymentMethod")) {
                paymentMethod = CheckInRecord.PaymentMethod.valueOf((String) checkoutInfo.get("paymentMethod"));
            }
            
            // 调用服务处理退房
            CheckInRecord result = checkInService.checkout(checkInId, additionalCharges, returnDeposit, remarks, paymentMethod);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "退房成功");
            response.put("data", result);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "退房失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    /**
     * 获取今日入住/退房统计
     */
    @GetMapping("/today-stats")
    public ResponseEntity<?> getTodayStats() {
        try {
            Map<String, Object> stats = checkInService.getTodayCheckInAndCheckoutStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取统计数据失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 添加额外消费记录
     */
    @PostMapping("/{checkInId}/charges")
    public ResponseEntity<?> addAdditionalCharge(
            @PathVariable Long checkInId,
            @RequestBody AdditionalCharge additionalCharge) {
        
        try {
            AdditionalCharge result = checkInService.addAdditionalCharge(checkInId, additionalCharge);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "额外消费添加成功");
            response.put("data", result);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "添加额外消费失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    /**
     * 延长入住时间
     */
    @PutMapping("/{checkInId}/extend")
    public ResponseEntity<?> extendStay(
            @PathVariable Long checkInId,
            @RequestBody Map<String, Object> extendInfo) {
        
        try {
            // 解析新的退房日期
            LocalDate newCheckOutDate = LocalDate.parse((String) extendInfo.get("newCheckOutDate"));
            
            // 解析备注
            String remarks = (String) extendInfo.get("remarks");
            
            // 调用服务延长入住
            CheckInRecord result = checkInService.extendStay(checkInId, newCheckOutDate, remarks);
            
            // 构建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("id", result.getId());
            responseData.put("checkInNumber", result.getCheckInNumber());
            responseData.put("originalCheckOutDate", extendInfo.get("originalCheckOutDate"));
            responseData.put("newCheckOutDate", result.getCheckOutDate());
            responseData.put("additionalNights", ChronoUnit.DAYS.between(LocalDate.parse((String) extendInfo.get("originalCheckOutDate")), newCheckOutDate));
            responseData.put("totalAmount", result.getTotalAmount());
            responseData.put("updateTime", result.getUpdateTime());
            responseData.put("operatorId", result.getOperatorId());
            responseData.put("operatorName", result.getOperatorName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "入住时间已延长");
            response.put("data", responseData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "延长入住时间失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
} 