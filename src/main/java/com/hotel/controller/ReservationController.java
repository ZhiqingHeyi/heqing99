package com.hotel.controller;

import com.hotel.entity.Reservation;
import com.hotel.entity.Room;
import com.hotel.entity.User;
import com.hotel.service.ReservationService;
import com.hotel.service.RoomService;
import com.hotel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RoomService roomService;
    
    // 创建预订
    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody Map<String, Object> reservationData) {
        try {
            System.out.println("接收到预订请求数据: " + reservationData);
            
            // 解析请求数据
            Long userId = Long.parseLong(reservationData.get("userId").toString());
            Long roomTypeId = Long.parseLong(reservationData.get("roomType").toString());
            String checkInDateStr = reservationData.get("checkIn").toString();
            String checkOutDateStr = reservationData.get("checkOut").toString();
            Integer roomCount = Integer.parseInt(reservationData.get("roomCount").toString());
            String contactName = reservationData.get("contactName").toString();
            String phone = reservationData.get("phone").toString();
            String remarks = reservationData.containsKey("remarks") ? reservationData.get("remarks").toString() : "";
            Double totalAmount = Double.parseDouble(reservationData.get("totalAmount").toString());
            
            // 解析ISO格式日期时间
            LocalDateTime checkInTime;
            LocalDateTime checkOutTime;
            
            try {
                checkInTime = LocalDateTime.parse(checkInDateStr);
                checkOutTime = LocalDateTime.parse(checkOutDateStr);
            } catch (DateTimeParseException e) {
                // 尝试其他常见格式
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    checkInTime = LocalDateTime.parse(checkInDateStr, formatter);
                    checkOutTime = LocalDateTime.parse(checkOutDateStr, formatter);
                } catch (DateTimeParseException e2) {
                    System.err.println("日期解析失败: " + e2.getMessage());
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "日期格式无效，请使用ISO日期时间格式");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            System.out.println("解析后的入住日期: " + checkInTime);
            System.out.println("解析后的退房日期: " + checkOutTime);
            
            // 查找用户和房间
            User user = userService.getUserById(userId);
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户不存在，请先登录");
                return ResponseEntity.badRequest().body(response);
            }
            
            Room room = roomService.getRoomByTypeId(roomTypeId);
            if (room == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "指定类型的房间不存在或已满");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 应用会员折扣
            BigDecimal discount = userService.getDiscountByUserId(userId);
            BigDecimal originalAmount = BigDecimal.valueOf(totalAmount);
            BigDecimal finalAmount = originalAmount.multiply(discount);
            
            // 创建预订
            Reservation reservation = new Reservation();
            reservation.setUser(user);
            reservation.setRoom(room);
            reservation.setCheckInTime(checkInTime);
            reservation.setCheckOutTime(checkOutTime);
            reservation.setGuestName(contactName);
            reservation.setGuestPhone(phone);
            reservation.setSpecialRequests(remarks);
            reservation.setTotalPrice(finalAmount); // 使用折扣后的价格
            reservation.setRoomCount(roomCount);
            reservation.setStatus(Reservation.ReservationStatus.PENDING);
            
            // 保存预订
            Reservation createdReservation = reservationService.createReservation(reservation);
            
            // 记录用户消费并更新会员信息
            userService.addSpending(userId, finalAmount);
            
            // 返回响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "预订创建成功");
            response.put("reservationId", createdReservation.getId());
            response.put("originalAmount", originalAmount);
            response.put("discountRate", discount);
            response.put("finalAmount", finalAmount);
            response.put("memberLevel", user.getMemberLevel());
            
            System.out.println("预订创建成功，ID: " + createdReservation.getId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("创建预订失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建预订失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 获取用户的所有预订
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserReservations(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<Reservation> reservations = reservationService.getReservationsByUser(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", reservations);
            response.put("memberLevel", user.getMemberLevel());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("获取用户预订失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取预订失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 取消预订
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id) {
        try {
            Reservation reservation = reservationService.getReservationById(id);
            if (reservation == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "预订不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            reservationService.cancelReservation(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "预订已取消");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("取消预订失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{
                put("success", false);
                put("message", "取消预订失败: " + e.getMessage());
            }});
        }
    }
    
    // 获取预订详情
    @GetMapping("/{id}")
    public ResponseEntity<?> getReservation(@PathVariable Long id) {
        try {
            Reservation reservation = reservationService.getReservationById(id);
            if (reservation == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "预订不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", reservation);
            // 添加会员信息
            User user = reservation.getUser();
            if (user != null) {
                response.put("memberLevel", user.getMemberLevel());
                response.put("memberDiscount", user.getDiscountRate());
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("获取预订详情失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{
                put("success", false);
                put("message", "获取预订详情失败: " + e.getMessage());
            }});
        }
    }
    
    // 管理员确认预订
    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmReservation(@PathVariable Long id) {
        try {
            Reservation reservation = reservationService.getReservationById(id);
            if (reservation == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "预订不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            Reservation confirmedReservation = reservationService.confirmReservation(id);
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("message", "预订已确认");
                put("data", confirmedReservation);
            }});
        } catch (Exception e) {
            System.err.println("确认预订失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{
                put("success", false);
                put("message", "确认预订失败: " + e.getMessage());
            }});
        }
    }
    
    // 计算订单折扣价格
    @PostMapping("/calculate-discount")
    public ResponseEntity<?> calculateDiscount(
            @RequestParam Long userId,
            @RequestParam BigDecimal amount) {
        try {
            User user = userService.getUserById(userId);
            BigDecimal discount = BigDecimal.valueOf(user.getDiscountRate());
            BigDecimal discountedAmount = amount.multiply(discount).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal savedAmount = amount.subtract(discountedAmount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("originalAmount", amount);
            response.put("discountRate", discount);
            response.put("discountedAmount", discountedAmount);
            response.put("savedAmount", savedAmount);
            response.put("memberLevel", user.getMemberLevel());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("计算折扣失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{
                put("success", false);
                put("message", "计算折扣失败: " + e.getMessage());
            }});
        }
    }
}