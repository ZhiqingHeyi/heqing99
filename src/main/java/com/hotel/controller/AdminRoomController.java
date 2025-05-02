package com.hotel.controller;

import com.hotel.dto.RoomBatchDTO;
import com.hotel.dto.RoomFilterDTO;
import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import com.hotel.exception.BatchAddException;
import com.hotel.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/admin/rooms")
public class AdminRoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private com.hotel.service.ReservationService reservationService;
    
    @Autowired
    private com.hotel.repository.RoomTypeRepository roomTypeRepository;

    /**
     * 管理员获取所有房间列表
     * 支持分页、筛选和搜索功能
     */
    @GetMapping("")
    public ResponseEntity<?> getAllRooms(RoomFilterDTO filter) {
        
        try {
            // 页码从0开始
            Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());
            
            // 获取房间列表，应用筛选条件
            Page<Room> roomsPage = roomService.getRoomsWithFilters(
                    filter.getFloor(), filter.getRoomTypeId(), filter.getStatus(), filter.getKeyword(), pageable);
            
            // 转换为前端所需格式
            List<Map<String, Object>> roomsList = roomsPage.getContent().stream()
                    .map(this::convertRoomToMap)
                    .collect(Collectors.toList());
            
            // 构建分页信息
            Map<String, Object> pageInfo = new HashMap<>();
            pageInfo.put("total", roomsPage.getTotalElements());
            pageInfo.put("pages", roomsPage.getTotalPages());
            pageInfo.put("current", filter.getPage() + 1);
            pageInfo.put("size", filter.getSize());
            
            // 构建响应
            Map<String, Object> data = new HashMap<>();
            data.put("total", roomsPage.getTotalElements());
            data.put("list", roomsList);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", data);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取房间列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 管理员添加新房间
     */
    @PostMapping("")
    public ResponseEntity<?> addRoom(@RequestBody Room room) {
        try {
            // 检查房间号是否已存在
            if (roomService.isRoomNumberExists(room.getRoomNumber())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("code", 400);
                errorResponse.put("message", "房间号已存在");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            // 检查房间类型是否存在
            RoomType roomType = roomService.getRoomTypeById(room.getRoomType().getId());
            if (roomType == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("code", 400);
                errorResponse.put("message", "房间类型不存在");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            // 设置房间类型
            room.setRoomType(roomType);
            
            // 保存房间
            Room savedRoom = roomService.addRoom(room);
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "添加成功");
            response.put("data", convertRoomToMap(savedRoom));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 500);
            errorResponse.put("message", "添加房间失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 管理员更新房间信息
     */
    @PutMapping("/{roomId}")
    public ResponseEntity<?> updateRoom(@PathVariable String roomId, @RequestBody Room roomRequest) {
        try {
            // 1. 检查房间是否存在
            Long roomIdLong = Long.valueOf(roomId);
            Room existingRoom = roomService.getRoomById(roomIdLong);
            if (existingRoom == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("code", 404);
                errorResponse.put("message", "房间不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            // 2. 如果房间号变更，检查是否与现有房间冲突
            if (roomRequest.getRoomNumber() != null && 
                !roomRequest.getRoomNumber().equals(existingRoom.getRoomNumber())) {
                if(roomService.isRoomNumberExists(roomRequest.getRoomNumber())) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("code", 400);
                    errorResponse.put("message", "房间号已存在");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
                }
                // 更新 existingRoom 的房间号
                existingRoom.setRoomNumber(roomRequest.getRoomNumber());
            }
            
            // 3. 如果房型ID变更，检查新房型是否存在并更新
            if (roomRequest.getRoomType() != null && roomRequest.getRoomType().getId() != null && 
                !roomRequest.getRoomType().getId().equals(existingRoom.getRoomType().getId())) {
                RoomType newRoomType = roomService.getRoomTypeById(roomRequest.getRoomType().getId());
                if (newRoomType == null) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("code", 400);
                    errorResponse.put("message", "指定的房间类型不存在");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
                }
                // 更新 existingRoom 的房型
                existingRoom.setRoomType(newRoomType);
            }
            
            // 4. 更新其他允许修改的字段 (楼层, 状态, 清洁状态, 备注)
            if (roomRequest.getFloor() != null) {
                existingRoom.setFloor(roomRequest.getFloor());
            }
            if (roomRequest.getStatus() != null) {
                existingRoom.setStatus(roomRequest.getStatus());
            }
            // 注意：roomRequest 中可能没有 needCleaning 字段，因为它在 RoomForm 中
            // 但 Room 实体中有，直接从 roomRequest 获取
            // 如果前端 roomForm 包含 needCleaning, roomRequest 会有值
             existingRoom.setNeedCleaning(roomRequest.isNeedCleaning());
            
            if (roomRequest.getNotes() != null) { // 允许将备注设置为空字符串
                existingRoom.setNotes(roomRequest.getNotes());
            }
            
            // 5. 保存更新后的 existingRoom 对象
            // 注意：不需要再手动设置 ID，existingRoom 本身就包含正确的 ID
            Room updatedRoom = roomService.updateRoom(existingRoom);
            
            // 6. 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "更新成功");
            response.put("data", convertRoomToMap(updatedRoom));
            
            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            // 处理 roomId 无法转换为 Long 的情况
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 400);
            errorResponse.put("message", "无效的房间ID格式");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 500);
            errorResponse.put("message", "更新房间失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 管理员删除房间
     */
    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable String roomId) {
        try {
            // 1. 检查房间是否存在
            Room room = roomService.getRoomById(Long.valueOf(roomId));
            if (room == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("code", 404);
                errorResponse.put("message", "房间不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            // 2. 检查房间是否有关联的预订
            boolean hasReservations = reservationService.hasRoomReservations(Long.valueOf(roomId));
            if (hasReservations) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("code", 400);
                errorResponse.put("message", "房间有关联的预订记录，无法删除");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            // 3. 删除房间
            roomService.deleteRoom(Long.valueOf(roomId));
            
            // 4. 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "删除成功");
            response.put("data", null);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 500);
            errorResponse.put("message", "删除房间失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 管理员批量添加新房间
     */
    @PostMapping("/batch")
    public ResponseEntity<?> addMultipleRooms(@RequestBody @Valid List<RoomBatchDTO> roomDTOs) {
        try {
            List<Room> savedRooms = roomService.addMultipleRooms(roomDTOs);

            Map<String, Object> data = new HashMap<>();
            data.put("count", savedRooms.size());
            // 可选：返回添加的房间列表
            // data.put("rooms", savedRooms.stream().map(this::convertRoomToMap).collect(Collectors.toList()));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 201); // 201 Created
            response.put("message", "成功添加 " + savedRooms.size() + " 个房间");
            response.put("data", data);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (BatchAddException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 400);
            errorResponse.put("message", "批量添加房间失败");
            errorResponse.put("errors", e.getErrors()); // 返回详细错误列表
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            // 处理其他意外错误
            e.printStackTrace(); // 记录详细错误日志
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 500);
            errorResponse.put("message", "批量添加房间时发生内部错误: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 将Room实体转换为Map
     */
    private Map<String, Object> convertRoomToMap(Room room) {
        Map<String, Object> roomMap = new HashMap<>();
        roomMap.put("id", room.getId());
        roomMap.put("roomNumber", room.getRoomNumber());
        roomMap.put("floor", room.getFloor());
        
        // 房间类型信息
        Map<String, Object> roomTypeMap = new HashMap<>();
        roomTypeMap.put("id", room.getRoomType().getId());
        roomTypeMap.put("name", room.getRoomType().getName());
        roomTypeMap.put("basePrice", room.getRoomType().getBasePrice());
        roomMap.put("roomType", roomTypeMap);
        
        roomMap.put("status", room.getStatus().name());
        roomMap.put("needCleaning", room.isNeedCleaning());
        roomMap.put("lastCleanTime", room.getUpdateTime()); // 假设更新时间作为最后清洁时间
        roomMap.put("notes", room.getNotes());
        roomMap.put("updateTime", room.getUpdateTime());
        
        return roomMap;
    }
} 