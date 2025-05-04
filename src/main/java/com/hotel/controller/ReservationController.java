package com.hotel.controller;

import com.hotel.common.ApiResponse;
import com.hotel.dto.ReservationDTO;
import com.hotel.entity.Reservation;
import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import com.hotel.entity.User;
import com.hotel.service.ReservationService;
import com.hotel.service.RoomService;
import com.hotel.service.UserService;
import com.hotel.util.UserContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageImpl;
import org.springframework.format.annotation.DateTimeFormat;
import com.hotel.dto.ReservationSummaryDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

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
            log.info("接收到预订请求数据 Map:", reservationData);

            // --- Detailed Log BEFORE calling service --- Start
            log.info("--- Debugging reservationData before service call ---");
            log.info("Contains 'paymentStatus' key? {}", reservationData.containsKey("paymentStatus"));
            if (reservationData.containsKey("paymentStatus")) {
                Object paymentStatusValue = reservationData.get("paymentStatus");
                log.info("Value of 'paymentStatus': {}", paymentStatusValue);
                log.info("Type of 'paymentStatus' value: {}", (paymentStatusValue != null ? paymentStatusValue.getClass().getName() : "null"));
            }
            log.info("--- End Debugging reservationData ---");
            // --- Detailed Log BEFORE calling service --- End

            // 将解析和业务逻辑委托给 Service 层
            Reservation createdReservation = reservationService.createReservation(reservationData);
            
            // --- 响应构建部分保持不变 ---
            // 需要从 createdReservation 或其关联对象获取信息来构建响应
            // 注意：如果需要原始金额和折扣率，可能需要在Service层返回更复杂的结果，或者在这里重新计算
            // 暂时假设 Service 成功后返回了包含必要信息的 Reservation 对象
             User user = createdReservation.getUser(); // 获取关联的 User
             BigDecimal finalAmount = createdReservation.getTotalPrice();
             // 如何获取 originalAmount 和 discount? 这需要调整 Service 返回值或 Controller 逻辑
             // 假设我们可以在这里通过 finalAmount 和 discount 反推 originalAmount
             BigDecimal discount = userService.getDiscountByUserId(user.getId()); // 重新获取折扣
             BigDecimal originalAmount = finalAmount.divide(discount, 2, BigDecimal.ROUND_HALF_UP);
            
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("reservationId", createdReservation.getId());
            responseBody.put("originalAmount", originalAmount); // 注意: 此值可能不精确，取决于 finalAmount 和 discount
            responseBody.put("discountRate", discount);
            responseBody.put("finalAmount", finalAmount);
            responseBody.put("memberLevel", user.getMemberLevel()); // 获取用户的会员等级
            
            System.out.println("预订创建成功，Controller 返回 ID: " + createdReservation.getId());
            return ResponseEntity.ok(ApiResponse.success("预订创建成功", responseBody));
            
        } catch (RuntimeException e) { // Catch specific RuntimeExceptions from service
            System.err.println("创建预订失败: " + e.getMessage());
             // 根据 Service 抛出的异常信息返回具体的错误响应
             // 使用 ApiResponse.fail 来包装错误信息
            return ResponseEntity.badRequest().body(ApiResponse.fail("创建预订失败: " + e.getMessage()));
        } catch (Exception e) { // Catch any other unexpected errors
            System.err.println("创建预订时发生意外错误: " + e.getMessage());
            e.printStackTrace();
            // 返回通用服务器错误
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("创建预订时发生意外错误"));
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
            return ResponseEntity.ok(ApiResponse.success("预订取消成功"));
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
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("预订不存在"));
            }
            
            // Convert to DTO before returning
            ReservationDTO reservationDTO = ReservationDTO.fromEntity(reservation);

            System.out.println("返回预订详情成功, id: " + id);
            return ResponseEntity.ok(ApiResponse.success(reservationDTO));
        } catch (Exception e) {
            System.err.println("获取预订详情失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("获取预订详情失败: " + e.getMessage()));
        }
    }
    
    // 管理员确认预订
    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmReservation(@PathVariable Long id) {
        try {
            System.out.println("接收到确认预订请求, id: " + id);
            Reservation reservation = reservationService.confirmReservation(id);
            
            // Convert to DTO before returning
            ReservationDTO reservationDTO = ReservationDTO.fromEntity(reservation);
            
            System.out.println("预订确认成功, id: " + id);
            return ResponseEntity.ok(ApiResponse.success("预订确认成功", reservationDTO));
        } catch (Exception e) {
            System.err.println("确认预订失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.fail("确认预订失败: " + e.getMessage()));
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
    
    // 获取预订列表 (使用DTO和Service层分页过滤)
    @GetMapping
    public ResponseEntity<?> getAllReservations(
            Pageable pageable, // Spring automatically injects Pageable
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String guestName,
            @RequestParam(required = false) String guestPhone,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String bookingNo) {
        
        try {
            // Log the actual Pageable parameters received
            log.info("Received request for reservations - Page Index: {}, Page Size: {}, Status: {}, GuestName: {}, GuestPhone: {}, StartDate: {}, EndDate: {}, BookingNo: {}",
                     pageable.getPageNumber(), // Log 0-based index
                     pageable.getPageSize(),      // Log actual size used
                     status, guestName, guestPhone, startDate, endDate, bookingNo);

            // Verify Pageable parameters before calling service
            log.debug("Pageable details before service call: PageNumber={}, PageSize={}", pageable.getPageNumber(), pageable.getPageSize());

            // Delegate to service
            Page<ReservationSummaryDTO> reservations = reservationService.getAllReservations(pageable, status, guestName, guestPhone, startDate, endDate, bookingNo);
            
            return ResponseEntity.ok(ApiResponse.success(reservations));

        } catch (Exception e) {
            log.error("Error fetching reservations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("获取预订列表失败: " + e.getMessage()));
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

    // 获取当前登录用户的预订 (分页)
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<Page<ReservationDTO>>> getCurrentUserReservations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int pageSize,
            @RequestParam(required = false) String status
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.getUserByUsername(username);
            Long userId = currentUser.getId();

            System.out.println("获取当前用户预订列表请求, userId: " + userId + ", page: " + page + ", pageSize: " + pageSize + ", status: " + status);

            // Spring Pageable is 0-indexed
            Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("createTime").descending());
            
            // Call service with String status
            Page<Reservation> userReservationsPage = reservationService.getUserReservationsPaginated(userId, status, pageable);
            
            // Convert to DTO Page
            Page<ReservationDTO> dtoPage = userReservationsPage.map(ReservationDTO::fromEntity);
            
            System.out.println("返回当前用户预订列表 DTO 成功");
            return ResponseEntity.ok(ApiResponse.success(dtoPage));
        } catch (Exception e) {
            System.err.println("获取当前用户预订列表失败: " + e.getMessage());
            log.error("Error fetching current user reservations", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(400, "获取预订记录失败: " + e.getMessage()));
        }
    }
}