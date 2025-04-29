package com.hotel.controller;

import com.hotel.entity.CheckInRecord;
import com.hotel.entity.Room;
import com.hotel.service.CheckInService;
import com.hotel.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客户端房间相关API控制器
 */
@RestController
@RequestMapping("/api/rooms")
public class ClientRoomController {
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private CheckInService checkInService;
    
    /**
     * 查询客房入住状态
     * 
     * @param floor 楼层过滤
     * @param building 楼栋过滤
     * @param roomType 房型过滤
     * @param status 状态过滤(空闲/已预订/已入住/清扫中/维修中)
     * @return 客房状态列表
     */
    @GetMapping("/status")
    public ResponseEntity<?> getRoomsStatus(
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) String building,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) String status) {
        
        try {
            // 获取所有客房列表
            List<Room> allRooms = roomService.getAllRooms();
            
            // 应用过滤条件
            List<Room> filteredRooms = allRooms.stream()
                    .filter(room -> floor == null || room.getFloor().equals(floor))
                    .filter(room -> building == null || (room.getDescription() != null && room.getDescription().contains(building)))
                    .filter(room -> roomType == null || (room.getRoomType() != null && room.getRoomType().getName().equals(roomType)))
                    .filter(room -> {
                        if (status == null) return true;
                        try {
                            Room.RoomStatus roomStatus = Room.RoomStatus.valueOf(status.toUpperCase());
                            return room.getStatus() == roomStatus;
                        } catch (IllegalArgumentException e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
            
            // 获取当前所有入住记录（状态为CHECKED_IN）
            List<CheckInRecord> currentCheckIns = checkInService.getCurrentlyCheckedInRecords();
            
            // 创建房间ID到入住记录的映射，便于查找
            Map<Long, CheckInRecord> roomToCheckInMap = currentCheckIns.stream()
                    .collect(Collectors.toMap(CheckInRecord::getRoomId, record -> record));
            
            // 计算统计数据
            long totalRooms = filteredRooms.size();
            long occupiedRooms = filteredRooms.stream()
                    .filter(room -> room.getStatus() == Room.RoomStatus.OCCUPIED)
                    .count();
            long availableRooms = filteredRooms.stream()
                    .filter(room -> room.getStatus() == Room.RoomStatus.AVAILABLE)
                    .count();
            long cleaningRooms = filteredRooms.stream()
                    .filter(room -> room.getStatus() == Room.RoomStatus.CLEANING)
                    .count();
            long maintenanceRooms = filteredRooms.stream()
                    .filter(room -> room.getStatus() == Room.RoomStatus.MAINTENANCE)
                    .count();
            
            double occupancyRate = totalRooms > 0 ? ((double) occupiedRooms / totalRooms) * 100 : 0;
            
            // 日期时间格式化器
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            
            // 构建简化的房间信息
            List<Map<String, Object>> roomsInfo = filteredRooms.stream()
                    .map(room -> {
                        Map<String, Object> roomInfo = new HashMap<>();
                        roomInfo.put("roomId", room.getId());
                        roomInfo.put("roomNumber", room.getRoomNumber());
                        roomInfo.put("roomType", room.getRoomType() != null ? room.getRoomType().getName() : null);
                        roomInfo.put("floor", room.getFloor());
                        roomInfo.put("building", room.getDescription()); // 假设description存储了building信息
                        roomInfo.put("status", room.getStatus().name());
                        
                        // 如果房间有入住记录，添加客人信息
                        CheckInRecord checkInRecord = roomToCheckInMap.get(room.getId());
                        if (checkInRecord != null) {
                            roomInfo.put("guestName", checkInRecord.getGuestName());
                            roomInfo.put("checkInTime", checkInRecord.getActualCheckInTime().format(dateTimeFormatter));
                            roomInfo.put("checkOutTime", checkInRecord.getCheckOutDate().toString());
                        } else {
                            roomInfo.put("guestName", null);
                            roomInfo.put("checkInTime", null);
                            roomInfo.put("checkOutTime", null);
                        }
                        
                        return roomInfo;
                    })
                    .collect(Collectors.toList());
            
            // 构建响应对象
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("totalRooms", totalRooms);
            responseData.put("occupiedRooms", occupiedRooms);
            responseData.put("availableRooms", availableRooms);
            responseData.put("cleaningRooms", cleaningRooms);
            responseData.put("maintenanceRooms", maintenanceRooms);
            responseData.put("occupancyRate", Math.round(occupancyRate * 10) / 10.0); // 保留一位小数
            responseData.put("rooms", roomsInfo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", responseData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取房间状态失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
} 