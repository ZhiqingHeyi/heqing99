package com.hotel.controller;

import com.hotel.entity.Reservation;
import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
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
import java.util.Arrays;
import java.util.Collections;
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
            System.out.println("接收到获取用户预订请求, userId: " + userId);
            User user = userService.getUserById(userId);
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<Reservation> reservations = reservationService.getReservationsByUser(user);
            System.out.println("找到用户预订数量: " + reservations.size());
            
            // 如果没有记录，创建一些示例数据（仅用于演示）
            if (reservations.isEmpty()) {
                // 创建示例房间类型
                RoomType roomType1 = new RoomType();
                roomType1.setId(1L);
                roomType1.setName("豪华大床房");
                roomType1.setBasePrice(new BigDecimal("500"));
                roomType1.setCapacity(2);
                
                RoomType roomType2 = new RoomType();
                roomType2.setId(2L);
                roomType2.setName("行政套房");
                roomType2.setBasePrice(new BigDecimal("800"));
                roomType2.setCapacity(2);
                
                RoomType roomType3 = new RoomType();
                roomType3.setId(3L);
                roomType3.setName("总统套房");
                roomType3.setBasePrice(new BigDecimal("1200"));
                roomType3.setCapacity(4);
                
                // 创建示例房间
                Room room1 = new Room();
                room1.setId(1L);
                room1.setRoomNumber("101");
                room1.setRoomType(roomType1);
                room1.setFloor(1);
                room1.setStatus(Room.RoomStatus.AVAILABLE);
                
                Room room2 = new Room();
                room2.setId(2L);
                room2.setRoomNumber("202");
                room2.setRoomType(roomType2);
                room2.setFloor(2);
                room2.setStatus(Room.RoomStatus.AVAILABLE);
                
                Room room3 = new Room();
                room3.setId(3L);
                room3.setRoomNumber("303");
                room3.setRoomType(roomType3);
                room3.setFloor(3);
                room3.setStatus(Room.RoomStatus.AVAILABLE);
                
                // 创建示例预订
                Reservation reservation1 = new Reservation();
                reservation1.setId(1L);
                reservation1.setUser(user);
                reservation1.setRoom(room1);
                reservation1.setCheckInTime(LocalDateTime.now().plusDays(5));
                reservation1.setCheckOutTime(LocalDateTime.now().plusDays(7));
                reservation1.setGuestName(user.getName());
                reservation1.setGuestPhone(user.getPhone());
                reservation1.setTotalPrice(new BigDecimal("1000"));
                reservation1.setRoomCount(1);
                reservation1.setStatus(Reservation.ReservationStatus.PENDING);
                
                Reservation reservation2 = new Reservation();
                reservation2.setId(2L);
                reservation2.setUser(user);
                reservation2.setRoom(room2);
                reservation2.setCheckInTime(LocalDateTime.now().plusDays(15));
                reservation2.setCheckOutTime(LocalDateTime.now().plusDays(17));
                reservation2.setGuestName(user.getName());
                reservation2.setGuestPhone(user.getPhone());
                reservation2.setTotalPrice(new BigDecimal("1500"));
                reservation2.setRoomCount(1);
                reservation2.setStatus(Reservation.ReservationStatus.CONFIRMED);
                
                Reservation reservation3 = new Reservation();
                reservation3.setId(3L);
                reservation3.setUser(user);
                reservation3.setRoom(room3);
                reservation3.setCheckInTime(LocalDateTime.now().minusDays(20));
                reservation3.setCheckOutTime(LocalDateTime.now().minusDays(18));
                reservation3.setGuestName(user.getName());
                reservation3.setGuestPhone(user.getPhone());
                reservation3.setTotalPrice(new BigDecimal("2000"));
                reservation3.setRoomCount(1);
                reservation3.setStatus(Reservation.ReservationStatus.COMPLETED);
                
                reservations = Collections.unmodifiableList(Arrays.asList(reservation1, reservation2, reservation3));
                System.out.println("使用示例数据代替空记录");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", reservations);
            response.put("memberLevel", user.getMemberLevel());
            
            System.out.println("返回用户预订数据成功");
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