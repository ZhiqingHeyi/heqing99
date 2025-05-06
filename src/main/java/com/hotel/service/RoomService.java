package com.hotel.service;

import com.hotel.dto.RoomBatchDTO;
import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import com.hotel.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomService {
    /**
     * 根据筛选条件获取房间列表，支持分页
     * @param floor 楼层
     * @param roomTypeId 房间类型ID
     * @param status 房间状态
     * @param keyword 关键字搜索(房间号)
     * @param pageable 分页信息
     * @return 分页的房间列表
     */
    Page<Room> getRoomsWithFilters(Integer floor, String roomTypeId, String status, String keyword, Pageable pageable);

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

    /**
     * 检查指定房型是否被任何房间使用
     * @param typeId 房型ID
     * @return 如果被使用则返回 true，否则返回 false
     */
    boolean isRoomTypeInUse(Long typeId);

    /**
     * 更新房间类型信息
     * @param id 房间类型ID
     * @param roomTypeDetails 包含更新信息的RoomType对象
     * @return 更新后的RoomType对象
     * @throws RuntimeException 如果房型不存在或名称冲突
     */
    RoomType updateRoomType(Long id, RoomType roomTypeDetails);

    /**
     * 删除房间类型
     * @param id 房间类型ID
     * @throws RuntimeException 如果房型不存在
     */
    void deleteRoomType(Long id);

    /**
     * 批量添加新房间
     * @param roomDTOs 包含房间信息的DTO列表
     * @return 成功保存的房间列表
     * @throws com.hotel.exception.BatchAddException 如果校验失败或保存出错
     */
    List<Room> addMultipleRooms(List<RoomBatchDTO> roomDTOs);

    /**
     * 根据房间号查找已入住的客人信息
     * @param roomNumber 房间号
     * @return 如果房间存在且已入住，返回客人 User 对象；否则返回 null
     */
    User findGuestByOccupiedRoomNumber(String roomNumber);

    /**
     * 统计需要清洁的房间数量
     * @param needCleaning 是否需要清洁
     * @return 符合条件的房间数量
     */
    long countByNeedCleaning(boolean needCleaning);
}