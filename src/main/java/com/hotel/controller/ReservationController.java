package com.hotel.controller;

import com.hotel.entity.Reservation;
import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import com.hotel.entity.User;
import com.hotel.service.ReservationService;
import com.hotel.service.RoomService;
import com.hotel.service.UserService;
import com.hotel.util.UserContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            response.put("message", "获取用户预订失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 取消预订
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id) {
        try {
            System.out.println("接收到取消预订请求, id: " + id);
            reservationService.cancelReservation(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "预订取消成功");
            
            System.out.println("预订取消成功, id: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("取消预订失败: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "取消预订失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 获取预订详情
    @GetMapping("/{id}")
    public ResponseEntity<?> getReservation(@PathVariable Long id) {
        try {
            System.out.println("接收到获取预订详情请求, id: " + id);
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
            
            System.out.println("返回预订详情成功, id: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("获取预订详情失败: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取预订详情失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 管理员确认预订
    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmReservation(@PathVariable Long id) {
        try {
            System.out.println("接收到确认预订请求, id: " + id);
            Reservation reservation = reservationService.confirmReservation(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "预订确认成功");
            response.put("data", reservation);
            
            System.out.println("预订确认成功, id: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("确认预订失败: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "确认预订失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 计算订单折扣价格
    @PostMapping("/calculate-discount")
    public ResponseEntity<?> calculateDiscount(
            @RequestParam Long userId,
            @RequestParam BigDecimal amount) {
        try {
            System.out.println("接收到计算折扣请求, userId: " + userId + ", amount: " + amount);
            
            BigDecimal discount = userService.getDiscountByUserId(userId);
            BigDecimal discountedAmount = amount.multiply(discount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("originalAmount", amount);
            response.put("discountRate", discount);
            response.put("discountedAmount", discountedAmount);
            
            System.out.println("计算折扣成功: " + discountedAmount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("计算折扣失败: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "计算折扣失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 获取预订列表
    @GetMapping
    public ResponseEntity<?> getAllReservations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) String guestName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        try {
            System.out.println("接收到获取预订列表请求，参数：" + 
                    "page=" + page + 
                    ", pageSize=" + pageSize + 
                    ", status=" + status + 
                    ", roomType=" + roomType + 
                    ", guestName=" + guestName + 
                    ", startDate=" + startDate + 
                    ", endDate=" + endDate);
            
            // 调用服务层方法获取预订列表
            List<Reservation> reservations = reservationService.getAllReservations();
            
            // 进行过滤处理
            if (status != null && !status.isEmpty()) {
                reservations = reservations.stream()
                        .filter(r -> r.getStatus().toString().equalsIgnoreCase(status))
                        .collect(Collectors.toList());
            }
            
            if (roomType != null && !roomType.isEmpty()) {
                reservations = reservations.stream()
                        .filter(r -> r.getRoom().getRoomType().getName().contains(roomType))
                        .collect(Collectors.toList());
            }
            
            if (guestName != null && !guestName.isEmpty()) {
                reservations = reservations.stream()
                        .filter(r -> r.getGuestName().contains(guestName))
                        .collect(Collectors.toList());
            }
            
            if (startDate != null && !startDate.isEmpty()) {
                LocalDate start = LocalDate.parse(startDate);
                reservations = reservations.stream()
                        .filter(r -> !r.getCheckInTime().toLocalDate().isBefore(start))
                        .collect(Collectors.toList());
            }
            
            if (endDate != null && !endDate.isEmpty()) {
                LocalDate end = LocalDate.parse(endDate);
                reservations = reservations.stream()
                        .filter(r -> !r.getCheckOutTime().toLocalDate().isAfter(end))
                        .collect(Collectors.toList());
            }
            
            // 计算总数
            int total = reservations.size();
            
            // 分页处理
            int fromIndex = (page - 1) * pageSize;
            if (fromIndex >= reservations.size()) {
                reservations = Collections.emptyList();
            } else {
                int toIndex = Math.min(fromIndex + pageSize, reservations.size());
                reservations = reservations.subList(fromIndex, toIndex);
            }
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "获取成功");
            
            Map<String, Object> data = new HashMap<>();
            data.put("total", total);
            data.put("list", reservations);
            
            response.put("data", data);
            
            System.out.println("返回预订列表成功，总数：" + total);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("获取预订列表失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取预订列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 更新预订状态
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReservationStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {
        
        try {
            System.out.println("接收到更新预订状态请求, id: " + id + ", 状态: " + statusUpdate);
            
            String status = statusUpdate.get("status");
            if (status == null || status.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "状态参数不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 获取预订
            Reservation reservation = reservationService.getReservationById(id);
            if (reservation == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "预订不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 设置新状态
            try {
                Reservation.ReservationStatus newStatus = Reservation.ReservationStatus.valueOf(status.toUpperCase());
                reservation.setStatus(newStatus);
            } catch (IllegalArgumentException e) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "无效的状态值: " + status);
                return ResponseEntity.badRequest().body(response);
            }
            
            // 更新预订
            Reservation updatedReservation = reservationService.updateReservation(reservation);
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "状态更新成功");
            response.put("data", updatedReservation);
            
            System.out.println("预订状态更新成功, id: " + id + ", 新状态: " + status);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("更新预订状态失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新预订状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 完成预订
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeReservation(@PathVariable Long id) {
        try {
            System.out.println("接收到完成预订请求, id: " + id);
            Reservation reservation = reservationService.completeReservation(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "预订完成成功");
            response.put("data", null);
            
            System.out.println("预订完成成功, id: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("完成预订失败: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "完成预订失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 获取预订统计数据
    @GetMapping("/statistics")
    public ResponseEntity<?> getReservationStatistics(
            @RequestParam(required = false, defaultValue = "month") String period) {
        try {
            System.out.println("接收到获取预订统计数据请求, period: " + period);
            Map<String, Object> statistics = reservationService.getReservationStatistics(period);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", statistics);
            
            System.out.println("获取预订统计数据成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("获取预订统计数据失败: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取预订统计数据失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 新增：获取当前用户的预订列表（分页和状态过滤）
    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUserReservations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) { // status 作为 String 接收
        try {
            Long userId = UserContextHolder.getCurrentUserId(); // 获取当前用户 ID
            
            // 处理 status 字符串到枚举的转换
            Reservation.ReservationStatus reservationStatus = null;
            if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("all")) {
                try {
                    reservationStatus = Reservation.ReservationStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "无效的状态参数: " + status);
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // 创建 Pageable 对象 (注意 page 是 0-based)
            Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));

            // 调用服务层方法
            Page<Reservation> reservationPage = reservationService.getUserReservationsPaginated(userId, reservationStatus, pageable);

            // 构建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("list", reservationPage.getContent());
            responseData.put("total", reservationPage.getTotalElements());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", responseData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("获取当前用户预订列表失败: " + e.getMessage());
            // 可以根据异常类型返回更具体的错误，例如用户未登录
            if (e instanceof RuntimeException && e.getMessage().contains("用户未登录")) {
                 Map<String, Object> response = new HashMap<>();
                 response.put("success", false);
                 response.put("message", "用户未登录，请先登录");
                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取预订列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 使用 500
        }
    }
}