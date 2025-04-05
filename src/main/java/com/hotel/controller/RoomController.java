package com.hotel.controller;

import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import com.hotel.service.ReservationService;
import com.hotel.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private ReservationService reservationService;

    /**
     * 获取所有房间类型
     * 前端通过该接口获取所有可预订的房间类型及其详情
     */
    @GetMapping("/types")
    public ResponseEntity<?> getAllRoomTypes() {
        try {
            List<RoomType> roomTypes = roomService.getAllRoomTypes();
            
            // 转换为前端需要的数据格式
            List<Map<String, Object>> result = roomTypes.stream().map(type -> {
                Map<String, Object> roomTypeData = new HashMap<>();
                roomTypeData.put("id", type.getId());
                roomTypeData.put("name", type.getName());
                roomTypeData.put("price", type.getBasePrice());
                roomTypeData.put("capacity", type.getCapacity());
                roomTypeData.put("size", 0); // 假设没有size字段，默认为0
                roomTypeData.put("description", type.getDescription());
                roomTypeData.put("image", ""); // 假设没有image字段，使用空字符串
                
                // 获取该类型当前可用房间数量
                long availableCount = roomService.countAvailableRoomsByType(type.getId());
                roomTypeData.put("availableCount", availableCount);
                
                // 添加房间特性列表
                String[] features = type.getAmenities() != null ? type.getAmenities().split(",") : new String[0];
                roomTypeData.put("features", features);
                
                return roomTypeData;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("data", result);
            }});
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{
                put("success", false);
                put("message", "获取房间类型失败: " + e.getMessage());
            }});
        }
    }

    /**
     * 获取单个房间类型详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getRoomDetail(@PathVariable Long id) {
        try {
            RoomType roomType = roomService.getRoomTypeById(id);
            if (roomType == null) {
                return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{
                    put("success", false);
                    put("message", "房间类型不存在");
                }});
            }
            
            // 转换为前端需要的数据格式
            Map<String, Object> roomTypeData = new HashMap<>();
            roomTypeData.put("id", roomType.getId());
            roomTypeData.put("name", roomType.getName());
            roomTypeData.put("price", roomType.getBasePrice());
            roomTypeData.put("capacity", roomType.getCapacity());
            roomTypeData.put("size", 0); // 假设没有size字段，默认为0
            roomTypeData.put("description", roomType.getDescription());
            roomTypeData.put("image", ""); // 假设没有image字段，使用空字符串
            
            // 获取该类型当前可用房间数量
            long availableCount = roomService.countAvailableRoomsByType(roomType.getId());
            roomTypeData.put("availableCount", availableCount);
            
            // 添加房间特性列表
            String[] features = roomType.getAmenities() != null ? roomType.getAmenities().split(",") : new String[0];
            roomTypeData.put("features", features);
            
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("data", roomTypeData);
            }});
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{
                put("success", false);
                put("message", "获取房间类型详情失败: " + e.getMessage());
            }});
        }
    }

    /**
     * 检查房间可用性
     * 前端通过该接口检查指定日期范围内房间是否可预订
     */
    @GetMapping("/availability")
    public ResponseEntity<?> checkRoomAvailability(
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam String checkIn,
            @RequestParam String checkOut) {
        try {
            final LocalDateTime[] checkInOutDates = parseCheckInOutDates(checkIn, checkOut);
            if (checkInOutDates == null) {
                return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{
                    put("success", false);
                    put("message", "日期格式无效");
                }});
            }
            
            final LocalDateTime checkInDate = checkInOutDates[0];
            final LocalDateTime checkOutDate = checkInOutDates[1];
            
            Map<String, Object> result = new HashMap<>();
            
            if (roomTypeId != null) {
                // 检查特定房型可用性
                RoomType roomType = roomService.getRoomTypeById(roomTypeId);
                if (roomType == null) {
                    return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{
                        put("success", false);
                        put("message", "房间类型不存在");
                    }});
                }
                
                // 获取该类型可用房间数量
                int availableCount = roomService.getAvailableRoomsCountByTypeAndDateRange(
                        roomTypeId, checkInDate, checkOutDate);
                
                Map<String, Object> typeAvailability = new HashMap<>();
                typeAvailability.put("id", roomType.getId());
                typeAvailability.put("name", roomType.getName());
                typeAvailability.put("availableCount", availableCount);
                typeAvailability.put("isAvailable", availableCount > 0);
                
                result.put("roomType", typeAvailability);
            } else {
                // 检查所有房型可用性
                List<RoomType> allRoomTypes = roomService.getAllRoomTypes();
                List<Map<String, Object>> availabilityList = allRoomTypes.stream().map(type -> {
                    int availableCount = roomService.getAvailableRoomsCountByTypeAndDateRange(
                            type.getId(), checkInDate, checkOutDate);
                    
                    Map<String, Object> typeAvailability = new HashMap<>();
                    typeAvailability.put("id", type.getId());
                    typeAvailability.put("name", type.getName());
                    typeAvailability.put("price", type.getBasePrice());
                    typeAvailability.put("availableCount", availableCount);
                    typeAvailability.put("isAvailable", availableCount > 0);
                    
                    return typeAvailability;
                }).collect(Collectors.toList());
                
                result.put("roomTypes", availabilityList);
            }
            
            result.put("checkIn", checkIn);
            result.put("checkOut", checkOut);
            
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("data", result);
            }});
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{
                put("success", false);
                put("message", "检查房间可用性失败: " + e.getMessage());
            }});
        }
    }
    
    /**
     * 解析入住和退房日期
     * @return 返回一个包含两个元素的数组：[0]入住日期,[1]退房日期，解析失败返回null
     */
    private LocalDateTime[] parseCheckInOutDates(String checkIn, String checkOut) {
        try {
            // 尝试解析ISO日期时间格式
            LocalDateTime checkInDate = LocalDateTime.parse(checkIn);
            LocalDateTime checkOutDate = LocalDateTime.parse(checkOut);
            return new LocalDateTime[] { checkInDate, checkOutDate };
        } catch (DateTimeParseException e) {
            try {
                // 尝试解析日期格式
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDateTime checkInDate = LocalDate.parse(checkIn, formatter).atStartOfDay();
                LocalDateTime checkOutDate = LocalDate.parse(checkOut, formatter).atStartOfDay();
                return new LocalDateTime[] { checkInDate, checkOutDate };
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }
} 