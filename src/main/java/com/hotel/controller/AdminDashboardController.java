package com.hotel.controller;

import com.hotel.entity.User;
import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import com.hotel.entity.Statistics;
import com.hotel.service.UserService;
import com.hotel.service.RoomService;
import com.hotel.service.ReservationService;
import com.hotel.service.CheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {

    private final UserService userService;
    private final RoomService roomService;
    private final ReservationService reservationService;
    private final CheckInService checkInService;

    @Autowired
    public AdminDashboardController(UserService userService,
                                   RoomService roomService,
                                   ReservationService reservationService,
                                   CheckInService checkInService) {
        this.userService = userService;
        this.roomService = roomService;
        this.reservationService = reservationService;
        this.checkInService = checkInService;
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
        
        // 预订统计
        statistics.put("todayReservations", reservationService.countTodayReservations());
        statistics.put("upcomingReservations", reservationService.countUpcomingReservations());
        
        // 收入统计
        statistics.put("todayRevenue", checkInService.calculateTodayRevenue());
        statistics.put("monthlyRevenue", checkInService.calculateMonthlyRevenue());
        
        // 用户统计
        statistics.put("totalUsers", userService.countAllUsers());
        statistics.put("newUsersThisMonth", userService.countNewUsersThisMonth());
        
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
} 