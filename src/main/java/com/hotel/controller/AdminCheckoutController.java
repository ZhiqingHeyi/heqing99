package com.hotel.controller;

import com.hotel.entity.Room;
import com.hotel.entity.CheckInRecord;
import com.hotel.entity.AdditionalCharge;
import com.hotel.service.RoomService;
import com.hotel.service.CheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/checkout")
public class AdminCheckoutController {

    @Autowired
    private RoomService roomService;
    
    @Autowired
    private CheckInService checkInService;

    /**
     * 获取退房统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getCheckoutStats() {
        try {
            // 获取今日日期
            LocalDate today = LocalDate.now();
            LocalDate firstDayOfMonth = today.withDayOfMonth(1);
            
            // 获取今日退房数
            long todayCheckouts = checkInService.countCheckoutsByDate(today);
            
            // 获取待清洁房间数
            long pendingCleanings = roomService.countByNeedCleaning(true);
            
            // 获取本月退房数
            long monthlyCheckouts = checkInService.countCheckoutsBetweenDates(
                    firstDayOfMonth, today);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("todayCheckouts", todayCheckouts);
            stats.put("pendingCleanings", pendingCleanings);
            stats.put("monthlyCheckouts", monthlyCheckouts);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "获取统计数据成功");
            response.put("data", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取统计数据失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取退房记录列表
     */
    @GetMapping("/records")
    public ResponseEntity<?> getCheckoutRecords(
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) String guestName,
            @RequestParam(required = false) String checkoutDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            LocalDate parsedDate = null;
            if (checkoutDate != null && !checkoutDate.isEmpty()) {
                parsedDate = LocalDate.parse(checkoutDate);
            }
            
            List<CheckInRecord> checkoutRecords = checkInService.getCheckoutRecords(
                    roomNumber, guestName, parsedDate, page, pageSize);
            long total = checkInService.countCheckoutRecords(roomNumber, guestName, parsedDate);
            
            List<Map<String, Object>> records = checkoutRecords.stream()
                    .map(this::convertCheckoutRecordToMap)
                    .collect(Collectors.toList());
            
            Map<String, Object> data = new HashMap<>();
            data.put("records", records);
            data.put("total", total);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "获取退房记录成功");
            response.put("data", data);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取退房记录失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 创建新的退房记录
     */
    @PostMapping("/create")
    public ResponseEntity<?> createCheckout(@RequestBody Map<String, Object> checkoutData) {
        try {
            String roomNumber = (String) checkoutData.get("roomNumber");
            String checkoutDate = (String) checkoutData.get("checkoutDate");
            String checkoutTime = (String) checkoutData.get("checkoutTime");
            String remarks = (String) checkoutData.get("remarks");
            
            // 找到房间
            Room room = roomService.getRoomByNumber(roomNumber);
            if (room == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("code", 404);
                errorResponse.put("message", "房间不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            // 确认房间已入住
            if (room.getStatus() != Room.RoomStatus.OCCUPIED) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("code", 400);
                errorResponse.put("message", "该房间未被入住，无法办理退房");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            // 找到入住记录
            CheckInRecord checkInRecord = checkInService.getCurrentCheckInByRoomNumber(roomNumber);
            if (checkInRecord == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("code", 404);
                errorResponse.put("message", "未找到该房间的入住记录");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            // 设置退房时间
            LocalDate parsedDate = LocalDate.parse(checkoutDate);
            LocalTime parsedTime = LocalTime.parse(checkoutTime);
            LocalDateTime checkoutDateTime = LocalDateTime.of(parsedDate, parsedTime);
            
            // 更新入住记录
            checkInRecord.setActualCheckOutTime(checkoutDateTime);
            checkInRecord.setStatus(CheckInRecord.CheckInStatus.CHECKED_OUT);
            checkInRecord.setRemarks(remarks);
            
            // 更新房间状态
            room.setStatus(Room.RoomStatus.NEEDS_CLEANING);
            room.setNeedCleaning(true);
            roomService.updateRoom(room);
            
            // 保存更新后的入住记录
            checkInService.updateCheckInRecord(checkInRecord);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "退房登记成功");
            response.put("data", convertCheckoutRecordToMap(checkInRecord));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 500);
            errorResponse.put("message", "退房登记失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 确认退房
     */
    @PostMapping("/confirm/{id}")
    public ResponseEntity<?> confirmCheckout(@PathVariable Long id) {
        try {
            // 找到入住记录
            CheckInRecord checkInRecord = checkInService.getCheckInRecordById(id);
            if (checkInRecord == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("code", 404);
                errorResponse.put("message", "退房记录不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            // 检查状态
            if (checkInRecord.getStatus() == CheckInRecord.CheckInStatus.CHECKED_OUT) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("code", 400);
                errorResponse.put("message", "该记录已经完成退房");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            // 更新入住记录
            checkInRecord.setActualCheckOutTime(LocalDateTime.now());
            checkInRecord.setStatus(CheckInRecord.CheckInStatus.CHECKED_OUT);
            
            // 找到房间
            Room room = getRoomByCheckInRecord(checkInRecord);
            if (room != null) {
                // 更新房间状态
                room.setStatus(Room.RoomStatus.NEEDS_CLEANING);
                room.setNeedCleaning(true);
                roomService.updateRoom(room);
            }
            
            // 保存更新后的入住记录
            checkInService.updateCheckInRecord(checkInRecord);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "确认退房成功");
            response.put("data", convertCheckoutRecordToMap(checkInRecord));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 500);
            errorResponse.put("message", "确认退房失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 刷卡退房
     */
    @PostMapping("/card-checkout")
    public ResponseEntity<?> cardCheckout(@RequestBody Map<String, String> checkoutData) {
        try {
            String roomNumber = checkoutData.get("roomNumber");
            
            // 找到房间
            Room room = roomService.getRoomByNumber(roomNumber);
            if (room == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("code", 404);
                errorResponse.put("message", "房间不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            // 确认房间已入住
            if (room.getStatus() != Room.RoomStatus.OCCUPIED) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("code", 400);
                errorResponse.put("message", "该房间未被入住，无法办理退房");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            // 找到入住记录
            CheckInRecord checkInRecord = checkInService.getCurrentCheckInByRoomNumber(roomNumber);
            if (checkInRecord == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("code", 404);
                errorResponse.put("message", "未找到该房间的入住记录");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            // 更新入住记录
            LocalDateTime now = LocalDateTime.now();
            checkInRecord.setActualCheckOutTime(now);
            checkInRecord.setStatus(CheckInRecord.CheckInStatus.CHECKED_OUT);
            checkInRecord.setRemarks("通过刷卡方式退房");
            
            // 更新房间状态
            room.setStatus(Room.RoomStatus.NEEDS_CLEANING);
            room.setNeedCleaning(true);
            roomService.updateRoom(room);
            
            // 保存更新后的入住记录
            checkInService.updateCheckInRecord(checkInRecord);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "刷卡退房成功");
            response.put("data", convertCheckoutRecordToMap(checkInRecord));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 500);
            errorResponse.put("message", "刷卡退房失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取退房账单明细
     */
    @GetMapping("/{id}/bill")
    public ResponseEntity<?> getCheckoutBill(@PathVariable Long id) {
        try {
            // 找到入住记录
            CheckInRecord checkInRecord = checkInService.getCheckInRecordById(id);
            if (checkInRecord == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("code", 404);
                errorResponse.put("message", "退房记录不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            // 获取额外消费记录
            List<AdditionalCharge> additionalCharges = checkInRecord.getAdditionalCharges();
            if (additionalCharges == null || additionalCharges.isEmpty()) {
                // 如果没有额外消费记录，只返回住宿费用
                List<Map<String, Object>> billItems = new ArrayList<>();
                
                // 添加住宿费用
                Map<String, Object> roomFee = createRoomFeeItem(checkInRecord);
                billItems.add(roomFee);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("code", 200);
                response.put("message", "获取账单明细成功");
                response.put("data", billItems);
                return ResponseEntity.ok(response);
            }
            
            // 转换额外消费记录
            List<Map<String, Object>> billItems = additionalCharges.stream()
                    .map(this::convertAdditionalChargeToMap)
                    .collect(Collectors.toList());
            
            // 添加住宿费用
            Map<String, Object> roomFee = createRoomFeeItem(checkInRecord);
            billItems.add(0, roomFee);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "获取账单明细成功");
            response.put("data", billItems);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取账单明细失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 创建住宿费用项
     */
    private Map<String, Object> createRoomFeeItem(CheckInRecord checkInRecord) {
        Map<String, Object> roomFee = new HashMap<>();
        roomFee.put("name", "住宿费用");
        
        Room room = getRoomByCheckInRecord(checkInRecord);
        if (room != null && room.getRoomType() != null) {
            long stayDays = getStayDuration(checkInRecord);
            BigDecimal basePrice = room.getRoomType().getBasePrice();
            
            roomFee.put("quantity", stayDays);
            roomFee.put("price", basePrice);
            roomFee.put("amount", basePrice.multiply(BigDecimal.valueOf(stayDays)));
        } else {
            roomFee.put("quantity", 0);
            roomFee.put("price", 0);
            roomFee.put("amount", 0);
        }
        
        return roomFee;
    }

    /**
     * 获取入住天数
     */
    private long getStayDuration(CheckInRecord record) {
        LocalDate checkInDate = record.getCheckInDate();
        LocalDate checkOutDate = record.getCheckOutDate();
        
        if (checkInDate != null && checkOutDate != null) {
            return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        }
        
        return 0;
    }

    /**
     * 获取入住记录关联的房间
     */
    private Room getRoomByCheckInRecord(CheckInRecord record) {
        if (record.getRoomId() != null) {
            return roomService.getRoomById(record.getRoomId());
        } else if (record.getRoomNumber() != null) {
            return roomService.getRoomByNumber(record.getRoomNumber());
        }
        
        return null;
    }

    /**
     * 将入住记录转换为退房记录Map
     */
    private Map<String, Object> convertCheckoutRecordToMap(CheckInRecord record) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        Map<String, Object> map = new HashMap<>();
        map.put("id", record.getId());
        
        // 获取关联的房间
        Room room = getRoomByCheckInRecord(record);
        if (room != null) {
            map.put("roomNumber", room.getRoomNumber());
            
            // 房间类型信息
            if (room.getRoomType() != null) {
                map.put("roomType", room.getRoomType().getName());
            }
        } else {
            map.put("roomNumber", record.getRoomNumber());
        }
        
        map.put("guestName", record.getGuestName());
        
        // 入住日期
        if (record.getCheckInDate() != null) {
            map.put("checkinDate", record.getCheckInDate().format(dateFormatter));
        } else if (record.getActualCheckInTime() != null) {
            map.put("checkinDate", record.getActualCheckInTime().toLocalDate().format(dateFormatter));
        } else {
            map.put("checkinDate", "");
        }
        
        // 退房日期和时间
        if (record.getActualCheckOutTime() != null) {
            map.put("checkoutDate", record.getActualCheckOutTime().toLocalDate().format(dateFormatter));
            map.put("checkoutTime", record.getActualCheckOutTime().toLocalTime().format(timeFormatter));
        } else {
            map.put("checkoutDate", "");
            map.put("checkoutTime", "");
        }
        
        // 入住时长
        long stayDays = getStayDuration(record);
        map.put("stayDuration", stayDays + "晚");
        
        // 消费总额
        double totalAmount = calculateTotalAmount(record);
        map.put("totalAmount", totalAmount);
        
        // 状态
        map.put("status", convertCheckInStatusToUIStatus(record.getStatus()));
        
        map.put("remarks", record.getRemarks());
        
        return map;
    }

    /**
     * 计算总消费金额
     */
    private double calculateTotalAmount(CheckInRecord record) {
        // 住宿费用
        double roomFee = 0;
        Room room = getRoomByCheckInRecord(record);
        if (room != null && room.getRoomType() != null) {
            long stayDays = getStayDuration(record);
            roomFee = stayDays * room.getRoomType().getBasePrice().doubleValue();
        }
        
        // 额外消费
        double additionalFee = 0;
        if (record.getAdditionalCharges() != null) {
            additionalFee = record.getAdditionalCharges().stream()
                    .mapToDouble(charge -> charge.getAmount().doubleValue())
                    .sum();
        }
        
        return roomFee + additionalFee;
    }

    /**
     * 将额外消费转换为账单项Map
     */
    private Map<String, Object> convertAdditionalChargeToMap(AdditionalCharge charge) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", charge.getDescription());
        map.put("quantity", 1);
        map.put("price", charge.getAmount());
        map.put("amount", charge.getAmount());
        return map;
    }
    
    /**
     * 将入住状态转换为UI状态字符串
     */
    private String convertCheckInStatusToUIStatus(CheckInRecord.CheckInStatus status) {
        if (status == null) {
            return "UNKNOWN";
        }
        
        switch (status) {
            case CHECKED_IN:
                return "PENDING";
            case CHECKED_OUT:
                return "COMPLETED";
            default:
                return status.toString();
        }
    }
} 