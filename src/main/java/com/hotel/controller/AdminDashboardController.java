package com.hotel.controller;

import com.hotel.entity.User;
import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import com.hotel.entity.Statistics;
import com.hotel.service.UserService;
import com.hotel.service.RoomService;
import com.hotel.service.ReservationService;
import com.hotel.service.CheckInService;
import com.hotel.service.CleaningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.Year;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {

    private final UserService userService;
    private final RoomService roomService;
    private final ReservationService reservationService;
    private final CheckInService checkInService;
    private final CleaningService cleaningService;

    @Autowired
    public AdminDashboardController(UserService userService,
                                   RoomService roomService,
                                   ReservationService reservationService,
                                   CheckInService checkInService,
                                   CleaningService cleaningService) {
        this.userService = userService;
        this.roomService = roomService;
        this.reservationService = reservationService;
        this.checkInService = checkInService;
        this.cleaningService = cleaningService;
    }

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 房间统计
        statistics.put("totalRooms", roomService.getAllRooms().size());
        statistics.put("availableRooms", roomService.countByStatus(Room.RoomStatus.AVAILABLE));
        statistics.put("occupiedRooms", roomService.countByStatus(Room.RoomStatus.OCCUPIED));
        statistics.put("needsCleaningRooms", roomService.countByStatus(Room.RoomStatus.NEEDS_CLEANING));
        statistics.put("maintenanceRooms", roomService.countByStatus(Room.RoomStatus.MAINTENANCE));
        statistics.put("cleaningRooms", roomService.countByStatus(Room.RoomStatus.CLEANING));
        statistics.put("reservedRooms", roomService.countByStatus(Room.RoomStatus.RESERVED));
        
        // 预订统计
        statistics.put("todayReservations", reservationService.countTodayReservations());
        statistics.put("upcomingReservations", reservationService.countUpcomingReservations());
        
        // 收入统计
        statistics.put("todayRevenue", checkInService.calculateTodayRevenue());
        statistics.put("monthlyRevenue", checkInService.calculateMonthlyRevenue());
        
        // 用户统计
        statistics.put("totalUsers", userService.countAllUsers());
        statistics.put("newUsersThisMonth", userService.countNewUsersThisMonth());
        
        // 清洁任务统计 - 如果项目中有清洁任务服务，则可以获取相应的统计数据
        try {
            // 尝试从CleaningService获取清洁任务统计
            Map<String, Long> cleaningStats = cleaningService.getTasksStatistics();
            statistics.put("cleaningTasksCompleted", cleaningStats.getOrDefault("completed", 0L));
            statistics.put("cleaningTasksInProgress", cleaningStats.getOrDefault("inProgress", 0L));
            statistics.put("cleaningTasksPending", cleaningStats.getOrDefault("pending", 0L));
        } catch (Exception e) {
            // 如果出现异常（可能是因为cleaningService未注入），提供默认值
            statistics.put("cleaningTasksCompleted", 0L);
            statistics.put("cleaningTasksInProgress", 0L);
            statistics.put("cleaningTasksPending", 0L);
            // 记录异常，但不中断整个响应
            System.err.println("无法获取清洁任务统计: " + e.getMessage());
        }
        
        return ResponseEntity.ok(statistics);
    }

    /**
     * 获取所有用户列表
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    /**
     * 添加新用户
     */
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User savedUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "创建用户失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            user.setId(id);
            User updatedUser = userService.updateUser(user);
            if (updatedUser == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "更新用户失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "用户已成功删除");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "删除用户失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * 获取所有房间类型
     */
    @GetMapping("/roomtypes")
    public ResponseEntity<?> getAllRoomTypes() {
        try {
            List<RoomType> roomTypes = roomService.getAllRoomTypes();
            
            // 转换为前端期望的数据格式
            List<Map<String, Object>> formattedRoomTypes = roomTypes.stream()
                    .map(this::convertRoomTypeToMap)
                    .collect(Collectors.toList());
            
            // 构建响应对象
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "获取房型列表成功");
            response.put("data", formattedRoomTypes);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取房型列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 添加房间类型
     */
    @PostMapping("/roomtypes")
    public ResponseEntity<?> createRoomType(@RequestBody RoomType roomType) {
        try {
            RoomType savedRoomType = roomService.addRoomType(roomType);
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "添加房型成功");
            response.put("data", convertRoomTypeToMap(savedRoomType));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 500);
            errorResponse.put("message", "添加房型失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 更新房间类型
     */
    @PutMapping("/roomtypes/{typeId}")
    public ResponseEntity<?> updateRoomType(@PathVariable Long typeId, @RequestBody RoomType roomTypeDetails) {
        try {
            // 基础验证
            if (roomTypeDetails.getName() == null || roomTypeDetails.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("房型名称不能为空");
            }
            if (roomTypeDetails.getBasePrice() == null || roomTypeDetails.getBasePrice().doubleValue() <= 0) {
                throw new IllegalArgumentException("基础价格必须大于0");
            }
            if (roomTypeDetails.getCapacity() == null || roomTypeDetails.getCapacity() <= 0) {
                throw new IllegalArgumentException("可住人数必须大于0");
            }
            
            RoomType updatedRoomType = roomService.updateRoomType(typeId, roomTypeDetails);
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "更新房型成功");
            response.put("data", convertRoomTypeToMap(updatedRoomType));
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) { // 捕获 Service 层抛出的异常 (Not Found, Name Conflict)
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 400);
            errorResponse.put("message", "更新房型失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 500);
            errorResponse.put("message", "更新房型失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // /**
    //  * 更新房间信息 (与 AdminRoomController 冲突，已注释掉)
    //  */
    // @PutMapping("/rooms/{id}")
    // public ResponseEntity<?> updateRoom(@PathVariable Long id, @RequestBody Room room) {
    //     try {
    //         room.setId(id);
    //         Room updatedRoom = roomService.updateRoom(room);
    //         if (updatedRoom == null) {
    //             return ResponseEntity.notFound().build();
    //         }
    //         // 构建响应...
    //         Map<String, Object> response = new HashMap<>();
    //         response.put("success", true);
    //         response.put("code", 200);
    //         response.put("message", "更新成功");
    //         // 假设存在 convertRoomToMap 方法
    //         // response.put("data", convertRoomToMap(updatedRoom)); 
    //         response.put("data", updatedRoom); // 或者直接返回实体
    //         return ResponseEntity.ok(response);
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         Map<String, Object> errorResponse = new HashMap<>();
    //         errorResponse.put("success", false);
    //         errorResponse.put("code", 500);
    //         errorResponse.put("message", "更新房间失败: " + e.getMessage());
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    //     }
    // }

    /**
     * 获取员工列表
     */
    @GetMapping("/staff")
    public ResponseEntity<List<User>> getAllStaff() {
        List<User> staff = userService.getAllStaff();
        return ResponseEntity.ok(staff);
    }

    /**
     * 添加员工
     */
    @PostMapping("/staff")
    public ResponseEntity<?> createStaff(@RequestBody User staff) {
        try {
            staff.setRole(User.UserRole.CLEANER);
            User savedStaff = userService.createUser(staff);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedStaff);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "创建员工失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * 删除房间类型
     */
    @DeleteMapping("/roomtypes/{typeId}")
    public ResponseEntity<?> deleteRoomType(@PathVariable Long typeId) {
        try {
            // 检查房型是否在使用中
            if (roomService.isRoomTypeInUse(typeId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("code", 400);
                errorResponse.put("message", "删除失败：该房型已被一个或多个房间使用");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            // 删除房型
            roomService.deleteRoomType(typeId);
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "删除房型成功");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) { // 捕获 Service 层抛出的异常 (Not Found)
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 404);
            errorResponse.put("message", "删除房型失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 500);
            errorResponse.put("message", "删除房型失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 将 RoomType 转换为 Map
     */
    private Map<String, Object> convertRoomTypeToMap(RoomType roomType) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", roomType.getId());
        map.put("name", roomType.getName());
        map.put("basePrice", roomType.getBasePrice());
        map.put("capacity", roomType.getCapacity());
        map.put("amenities", roomType.getAmenities());
        map.put("description", roomType.getDescription());
        map.put("createTime", roomType.getCreateTime());
        map.put("updateTime", roomType.getUpdateTime());
        return map;
    }

    // 新增API：标记房间清洁完成
    @PostMapping("/rooms/{roomId}/mark-cleaned")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION', 'STAFF')")
    public ResponseEntity<?> markRoomAsCleaned(@PathVariable Long roomId) {
        try {
            roomService.confirmRoomCleaned(roomId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "房间 " + roomId + " 已成功标记为清洁并可用。");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 更具体的异常处理，例如房间未找到等
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "操作失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 获取入住率趋势数据
     * @param range 时间范围：week(周)或month(月)
     */
    @GetMapping("/dashboard/occupancy-trend")
    public ResponseEntity<?> getOccupancyTrend(@RequestParam(defaultValue = "week") String range) {
        try {
            Map<String, Object> result = new HashMap<>();
            List<String> xAxisData = new ArrayList<>();
            List<Double> occupancyData = new ArrayList<>();
            
            if ("week".equals(range)) {
                // 获取过去7天的入住率数据
                LocalDate today = LocalDate.now();
                for (int i = 6; i >= 0; i--) {
                    LocalDate date = today.minusDays(i);
                    String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.CHINA);
                    xAxisData.add(dayOfWeek);
                    
                    // 获取该日期的入住率
                    double occupancy = checkInService.calculateDailyOccupancyRate(date);
                    occupancyData.add(occupancy);
                }
            } else if ("month".equals(range)) {
                // 获取当月每5天的入住率数据
                YearMonth currentMonth = YearMonth.now();
                int daysInMonth = currentMonth.lengthOfMonth();
                
                // 按5天间隔采样，确保最后一天也包含
                for (int day = 1; day <= daysInMonth; day += 5) {
                    LocalDate date = currentMonth.atDay(day);
                    xAxisData.add(day + "日");
                    
                    // 获取该日期的入住率
                    double occupancy = checkInService.calculateDailyOccupancyRate(date);
                    occupancyData.add(occupancy);
                }
                
                // 确保包含月末数据
                if ((daysInMonth % 5) != 0) {
                    LocalDate date = currentMonth.atEndOfMonth();
                    xAxisData.add(daysInMonth + "日");
                    
                    // 获取该日期的入住率
                    double occupancy = checkInService.calculateDailyOccupancyRate(date);
                    occupancyData.add(occupancy);
                }
            }
            
            result.put("xAxis", xAxisData);
            result.put("data", occupancyData);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取入住率趋势数据失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 获取收入统计数据
     * @param range 时间范围：week(周)或month(月)
     */
    @GetMapping("/dashboard/revenue-stats")
    public ResponseEntity<?> getRevenueStats(@RequestParam(defaultValue = "week") String range) {
        try {
            Map<String, Object> result = new HashMap<>();
            List<String> xAxisData = new ArrayList<>();
            List<BigDecimal> revenueData = new ArrayList<>();
            
            if ("week".equals(range)) {
                // 获取过去7天的收入数据
                LocalDate today = LocalDate.now();
                for (int i = 6; i >= 0; i--) {
                    LocalDate date = today.minusDays(i);
                    String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.CHINA);
                    xAxisData.add(dayOfWeek);
                    
                    // 获取该日期的收入
                    BigDecimal revenue = checkInService.calculateDailyRevenue(date);
                    revenueData.add(revenue);
                }
            } else if ("month".equals(range)) {
                // 获取当年每月的收入数据
                int currentYear = Year.now().getValue();
                
                for (Month month : Month.values()) {
                    xAxisData.add((month.getValue()) + "月");
                    
                    // 获取该月的收入
                    YearMonth yearMonth = YearMonth.of(currentYear, month);
                    BigDecimal revenue = checkInService.calculateMonthlyRevenue(yearMonth);
                    revenueData.add(revenue);
                }
            }
            
            result.put("xAxis", xAxisData);
            result.put("data", revenueData);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取收入统计数据失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
} 