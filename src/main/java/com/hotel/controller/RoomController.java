package com.hotel.controller;

import com.hotel.dto.RoomFilterDTO;
import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import com.hotel.service.ReservationService;
import com.hotel.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.Collections;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private ReservationService reservationService;

    /**
     * 获取所有房间
     * 用于管理员查看和管理所有房间
     */
    @GetMapping("")
    public ResponseEntity<List<Room>> getAllRooms() {
        List<Room> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }

    /**
     * 获取房间列表（带筛选条件）
     */
    @GetMapping("/filter")
    public ResponseEntity<?> getRoomsWithFilters(RoomFilterDTO filter) {
        try {
            Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());
            
            // 获取房间列表，应用筛选条件
            Page<Room> roomsPage = roomService.getRoomsWithFilters(
                filter.getFloor(), filter.getRoomTypeId(), filter.getStatus(), filter.getKeyword(), pageable);
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", roomsPage.getContent());
            result.put("totalElements", roomsPage.getTotalElements());
            result.put("totalPages", roomsPage.getTotalPages());
            result.put("size", roomsPage.getSize());
            result.put("number", roomsPage.getNumber());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "获取房间列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 根据ID获取房间
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getRoomById(@PathVariable Long id) {
        Room room = roomService.getRoomById(id);
        if (room == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "房间不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        return ResponseEntity.ok(room);
    }

    /**
     * 添加新房间
     */
    @PostMapping("")
    public ResponseEntity<?> addRoom(@RequestBody Room room) {
        try {
            // 检查房间号是否已存在
            if (roomService.isRoomNumberExists(room.getRoomNumber())) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "房间号已存在");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            Room savedRoom = roomService.addRoom(room);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRoom);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "添加房间失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 更新房间信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @RequestBody Room room) {
        try {
            Room existingRoom = roomService.getRoomById(id);
            if (existingRoom == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "房间不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            // 如果房间号发生变化，检查新房间号是否已存在
            if (!existingRoom.getRoomNumber().equals(room.getRoomNumber()) 
                    && roomService.isRoomNumberExists(room.getRoomNumber())) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "房间号已存在");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            room.setId(id);
            Room updatedRoom = roomService.updateRoom(room);
            return ResponseEntity.ok(updatedRoom);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "更新房间失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 删除房间
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        try {
            Room room = roomService.getRoomById(id);
            if (room == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "房间不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            // 检查房间是否有关联的预订
            boolean hasReservations = reservationService.hasRoomReservations(id);
            if (hasReservations) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "房间有关联的预订记录，无法删除");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            roomService.deleteRoom(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "房间删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "删除房间失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 修改房间状态
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateRoomStatus(@PathVariable Long id, @RequestBody Map<String, String> statusMap) {
        try {
            if (!statusMap.containsKey("status")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "状态参数缺失");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            Room room = roomService.getRoomById(id);
            if (room == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "房间不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            String statusStr = statusMap.get("status");
            try {
                Room.RoomStatus status = Room.RoomStatus.valueOf(statusStr);
                Room updatedRoom = roomService.updateRoomStatus(id, status);
                return ResponseEntity.ok(updatedRoom);
            } catch (IllegalArgumentException e) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "无效的房间状态");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "更新房间状态失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 标记房间需要清洁
     */
    @PutMapping("/{id}/cleaning")
    public ResponseEntity<?> markRoomNeedCleaning(@PathVariable Long id) {
        try {
            Room room = roomService.getRoomById(id);
            if (room == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "房间不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            roomService.markRoomNeedCleaning(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "房间已标记为需要清洁");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "标记房间清洁状态失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 确认房间清洁完成
     */
    @PutMapping("/{id}/cleaning/complete")
    public ResponseEntity<?> confirmRoomCleaned(@PathVariable Long id) {
        try {
            Room room = roomService.getRoomById(id);
            if (room == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "房间不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            roomService.confirmRoomCleaned(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "房间清洁完成确认");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "确认房间清洁完成失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

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
                roomTypeData.put("basePrice", type.getBasePrice());
                roomTypeData.put("capacity", type.getCapacity());
                roomTypeData.put("bedType", type.getBedType());
                roomTypeData.put("area", type.getArea());
                
                // 处理设施列表
                List<String> amenitiesList = new ArrayList<>();
                if (type.getAmenities() != null && !type.getAmenities().isEmpty()) {
                    amenitiesList = Arrays.asList(type.getAmenities().split(","));
                }
                roomTypeData.put("amenities", amenitiesList);
                
                roomTypeData.put("description", type.getDescription());
                
                // 处理图片列表
                List<String> imagesList = new ArrayList<>();
                if (type.getImages() != null && !type.getImages().isEmpty()) {
                    try {
                        // 假设images字段存储了JSON数组格式的图片URL列表
                        ObjectMapper mapper = new ObjectMapper();
                        imagesList = mapper.readValue(type.getImages(), new TypeReference<List<String>>() {});
                    } catch (Exception e) {
                        // 解析失败时使用空列表
                        imagesList = new ArrayList<>();
                    }
                }
                roomTypeData.put("images", imagesList);
                
                // 获取该类型当前可用房间数量
                long availableCount = roomService.countAvailableRoomsByType(type.getId());
                roomTypeData.put("availableCount", availableCount);
                
                return roomTypeData;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("code", 200);
                put("message", "获取成功");
                put("data", result);
            }});
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, Object>() {{
                put("success", false);
                put("code", 500);
                put("message", "获取房间类型失败: " + e.getMessage());
            }});
        }
    }

    /**
     * 获取单个房间类型详情
     */
    @GetMapping("/types/{typeId}")
    public ResponseEntity<?> getRoomTypeDetail(@PathVariable Long typeId) {
        try {
            RoomType roomType = roomService.getRoomTypeById(typeId);
            if (roomType == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new HashMap<String, Object>() {{
                    put("success", false);
                    put("code", 404);
                    put("message", "房间类型不存在");
                }});
            }
            
            // 转换为前端需要的数据格式
            Map<String, Object> data = new HashMap<>();
            data.put("id", roomType.getId());
            data.put("name", roomType.getName());
            data.put("basePrice", roomType.getBasePrice());
            data.put("weekendPrice", roomType.getWeekendPrice());
            data.put("holidayPrice", roomType.getHolidayPrice());
            data.put("capacity", roomType.getCapacity());
            data.put("maxCapacity", roomType.getMaxCapacity());
            data.put("extraBedPrice", roomType.getExtraBedPrice());
            data.put("bedType", roomType.getBedType());
            data.put("bedSize", roomType.getBedSize());
            data.put("area", roomType.getArea());
            data.put("floor", roomType.getFloor());
            
            // 处理设施列表
            List<String> amenitiesList = new ArrayList<>();
            if (roomType.getAmenities() != null && !roomType.getAmenities().isEmpty()) {
                amenitiesList = Arrays.asList(roomType.getAmenities().split(","));
            }
            data.put("amenities", amenitiesList);
            
            data.put("description", roomType.getDescription());
            data.put("longDescription", roomType.getLongDescription());
            
            // 处理图片信息
            List<Map<String, String>> imagesList = new ArrayList<>();
            if (roomType.getImages() != null && !roomType.getImages().isEmpty()) {
                try {
                    // 假设images字段存储了JSON格式的图片信息数组
                    ObjectMapper mapper = new ObjectMapper();
                    imagesList = mapper.readValue(roomType.getImages(), new TypeReference<List<Map<String, String>>>() {});
                } catch (Exception e) {
                    // 解析失败时使用空列表
                }
            }
            data.put("images", imagesList);
            
            // 处理政策信息
            Map<String, String> policiesMap = new HashMap<>();
            if (roomType.getPolicies() != null && !roomType.getPolicies().isEmpty()) {
                try {
                    // 假设policies字段存储了JSON格式的政策信息
                    ObjectMapper mapper = new ObjectMapper();
                    policiesMap = mapper.readValue(roomType.getPolicies(), new TypeReference<Map<String, String>>() {});
                } catch (Exception e) {
                    // 解析失败时使用空Map
                }
            }
            data.put("policies", policiesMap);
            
            // 获取该类型当前可用房间数量
            long availableCount = roomService.countAvailableRoomsByType(roomType.getId());
            data.put("availableCount", availableCount);
            
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("code", 200);
                put("message", "获取成功");
                put("data", data);
            }});
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, Object>() {{
                put("success", false);
                put("code", 500);
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
            @RequestParam(required = false) String roomType,
            @RequestParam String checkIn,
            @RequestParam String checkOut,
            @RequestParam(required = false, defaultValue = "1") Integer guests) {
        try {
            final LocalDateTime[] checkInOutDates = parseCheckInOutDates(checkIn, checkOut);
            if (checkInOutDates == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HashMap<String, Object>() {{
                    put("success", false);
                    put("code", 400);
                    put("message", "日期格式无效");
                }});
            }
            
            final LocalDateTime checkInDate = checkInOutDates[0];
            final LocalDateTime checkOutDate = checkInOutDates[1];
            
            // 检查日期范围是否合理
            if (checkInDate.isAfter(checkOutDate) || checkInDate.equals(checkOutDate)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HashMap<String, Object>() {{
                    put("success", false);
                    put("code", 400);
                    put("message", "入住日期必须早于离店日期");
                }});
            }
            
            List<Map<String, Object>> availabilityList = new ArrayList<>();
            List<RoomType> roomTypes;
            
            // 如果指定了房型，则只查询该房型
            if (roomType != null && !roomType.isEmpty()) {
                RoomType type = roomService.getRoomTypeById(Long.parseLong(roomType));
                if (type == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new HashMap<String, Object>() {{
                        put("success", false);
                        put("code", 404);
                        put("message", "指定的房间类型不存在");
                    }});
                }
                roomTypes = Collections.singletonList(type);
            } else {
                // 否则查询所有房型
                roomTypes = roomService.getAllRoomTypes();
            }
            
            // 计算日期范围内的所有日期
            List<LocalDate> dateRange = new ArrayList<>();
            LocalDate startDate = checkInDate.toLocalDate();
            LocalDate endDate = checkOutDate.toLocalDate();
            while (!startDate.isEqual(endDate)) {
                dateRange.add(startDate);
                startDate = startDate.plusDays(1);
            }
            
            // 遍历所有房型，计算可用性和价格
            for (RoomType type : roomTypes) {
                Map<String, Object> typeAvailability = new HashMap<>();
                int availableCount = roomService.getAvailableRoomsCountByTypeAndDateRange(
                        type.getId(), checkInDate, checkOutDate);
                
                typeAvailability.put("typeId", type.getId());
                typeAvailability.put("typeName", type.getName());
                typeAvailability.put("basePrice", type.getBasePrice());
                typeAvailability.put("available", availableCount > 0);
                typeAvailability.put("availableCount", availableCount);
                
                // 计算每日价格
                List<Map<String, Object>> dailyPrices = new ArrayList<>();
                BigDecimal totalPrice = BigDecimal.ZERO;
                
                for (LocalDate date : dateRange) {
                    Map<String, Object> dailyPrice = new HashMap<>();
                    dailyPrice.put("date", date.toString());
                    
                    BigDecimal price;
                    // 判断是否为周末 (星期六或星期日)
                    DayOfWeek dayOfWeek = date.getDayOfWeek();
                    boolean isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
                    
                    // 判断是否为节假日 (这里简化处理，实际应有节假日判断逻辑)
                    boolean isHoliday = false; // 假设没有节假日
                    
                    if (isHoliday && type.getHolidayPrice() != null) {
                        price = type.getHolidayPrice();
                    } else if (isWeekend && type.getWeekendPrice() != null) {
                        price = type.getWeekendPrice();
                    } else {
                        price = type.getBasePrice();
                    }
                    
                    dailyPrice.put("price", price);
                    
                    // 检查该日期是否有可用房间
                    boolean dailyAvailable = true; // 简化处理，假设所有日期均可预订
                    dailyPrice.put("available", dailyAvailable);
                    
                    dailyPrices.add(dailyPrice);
                    totalPrice = totalPrice.add(price);
                }
                
                typeAvailability.put("dailyPrices", dailyPrices);
                typeAvailability.put("totalPrice", totalPrice);
                
                availabilityList.add(typeAvailability);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", availabilityList);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, Object>() {{
                put("success", false);
                put("code", 500);
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