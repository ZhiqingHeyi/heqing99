package com.hotel.service;

import com.hotel.entity.Room;
import com.hotel.entity.RoomType;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomService {
    /**
     * 添加新房间
     */
    Room addRoom(Room room);

    /**
     * 更新房间信息
     */
    Room updateRoom(Room room);

    /**
     * 删除房间
     */
    void deleteRoom(Long id);

    /**
     * 获取房间详情
     */
    Room getRoomById(Long id);

    /**
     * 根据房间号获取房间
     */
    Room getRoomByNumber(String roomNumber);

    /**
     * 获取所有房间列表
     */
    List<Room> getAllRooms();

    /**
     * 根据房间状态获取房间列表
     */
    List<Room> getRoomsByStatus(Room.RoomStatus status);

    /**
     * 根据房间类型获取房间列表
     */
    List<Room> getRoomsByType(RoomType type);

    /**
     * 根据房间类型ID获取可用房间
     */
    Room getRoomByTypeId(Long typeId);

    /**
     * 获取所有需要清洁的房间
     */
    List<Room> getRoomsNeedCleaning();

    /**
     * 更新房间状态
     */
    Room updateRoomStatus(Long roomId, Room.RoomStatus status);

    /**
     * 标记房间需要清洁
     */
    void markRoomNeedCleaning(Long roomId);

    /**
     * 确认房间清洁完成
     */
    void confirmRoomCleaned(Long roomId);

    /**
     * 获取可用房间数量
     */
    long countAvailableRooms();

    /**
     * 检查房间号是否已存在
     */
    boolean isRoomNumberExists(String roomNumber);

    /**
     * 获取所有可预订的房间（状态为可用或清洁中）
     */
    List<Room> getBookableRooms();

    /**
     * 根据房间类型获取可用房间
     */
    List<Room> getAvailableRoomsByType(RoomType type);

    /**
     * 根据房间状态统计数量
     */
    long countByStatus(Room.RoomStatus status);

    /**
     * 获取所有房间类型
     */
    List<RoomType> getAllRoomTypes();

    /**
     * 添加新房间类型
     */
    RoomType addRoomType(RoomType roomType);

    /**
     * 根据ID获取房间类型
     */
    RoomType getRoomTypeById(Long id);

    /**
     * 统计指定类型的可用房间数量
     */
    long countAvailableRoomsByType(Long typeId);

    /**
     * 获取指定日期范围内、指定类型的可用房间数量
     */
    int getAvailableRoomsCountByTypeAndDateRange(Long typeId, LocalDateTime checkIn, LocalDateTime checkOut);
}