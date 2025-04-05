package com.hotel.service.impl;

import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.RoomTypeRepository;
import com.hotel.service.RoomService;
import com.hotel.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final ApplicationContext applicationContext;
    
    @Autowired
    public RoomServiceImpl(RoomRepository roomRepository, 
                          RoomTypeRepository roomTypeRepository,
                          ApplicationContext applicationContext) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.applicationContext = applicationContext;
    }
    
    // 懒加载ReservationService以避免循环依赖
    private ReservationService getReservationService() {
        return applicationContext.getBean(ReservationService.class);
    }

    @Override
    public Room addRoom(Room room) {
        return roomRepository.save(room);
    }

    @Override
    public Room updateRoom(Room room) {
        return roomRepository.save(room);
    }

    @Override
    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }

    @Override
    public Room getRoomById(Long id) {
        return roomRepository.findById(id).orElse(null);
    }

    @Override
    public Room getRoomByNumber(String roomNumber) {
        return roomRepository.findByRoomNumber(roomNumber).orElse(null);
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public List<Room> getRoomsByStatus(Room.RoomStatus status) {
        return roomRepository.findByStatus(status);
    }

    @Override
    public List<Room> getRoomsByType(RoomType type) {
        return roomRepository.findByRoomType(type);
    }

    @Override
    public List<Room> getRoomsNeedCleaning() {
        return roomRepository.findByNeedCleaningTrue();
    }

    @Override
    public Room updateRoomStatus(Long roomId, Room.RoomStatus status) {
        Room room = getRoomById(roomId);
        if (room != null) {
            room.setStatus(status);
            return roomRepository.save(room);
        }
        return null;
    }

    @Override
    public void markRoomNeedCleaning(Long roomId) {
        Room room = getRoomById(roomId);
        if (room != null) {
            room.setNeedCleaning(true);
            roomRepository.save(room);
        }
    }

    @Override
    public void confirmRoomCleaned(Long roomId) {
        Room room = getRoomById(roomId);
        if (room != null) {
            room.setNeedCleaning(false);
            room.setStatus(Room.RoomStatus.AVAILABLE);
            roomRepository.save(room);
        }
    }

    @Override
    public List<Room> getAvailableRoomsByType(RoomType type) {
        List<Room.RoomStatus> statuses = new ArrayList<>();
        statuses.add(Room.RoomStatus.AVAILABLE);
        statuses.add(Room.RoomStatus.CLEANING);
        return roomRepository.findByRoomTypeAndStatusIn(type, statuses);
    }

    @Override
    public List<Room> getBookableRooms() {
        return roomRepository.findAllAvailableAndCleaningRooms();
    }

    @Override
    public long countAvailableRooms() {
        return roomRepository.countByStatus(Room.RoomStatus.AVAILABLE);
    }

    @Override
    public boolean isRoomNumberExists(String roomNumber) {
        return roomRepository.existsByRoomNumber(roomNumber);
    }

    @Override
    public long countByStatus(Room.RoomStatus status) {
        return roomRepository.countByStatus(status);
    }

    @Override
    public Room getRoomByTypeId(Long typeId) {
        // 根据房间类型ID查找对应类型
        Optional<RoomType> roomType = roomTypeRepository.findById(typeId);
        if (!roomType.isPresent()) {
            return null;
        }
        
        // 查找该类型的可用房间
        List<Room> availableRooms = getAvailableRoomsByType(roomType.get());
        if (availableRooms.isEmpty()) {
            return null;
        }
        
        // 返回第一个可用的房间
        return availableRooms.get(0);
    }

    @Override
    public List<RoomType> getAllRoomTypes() {
        return roomTypeRepository.findAll();
    }

    @Override
    public RoomType addRoomType(RoomType roomType) {
        return roomTypeRepository.save(roomType);
    }
    
    @Override
    public RoomType getRoomTypeById(Long id) {
        return roomTypeRepository.findById(id).orElse(null);
    }
    
    @Override
    public long countAvailableRoomsByType(Long typeId) {
        Optional<RoomType> roomType = roomTypeRepository.findById(typeId);
        if (!roomType.isPresent()) {
            return 0;
        }
        
        List<Room> availableRooms = getAvailableRoomsByType(roomType.get());
        return availableRooms.size();
    }
    
    @Override
    public int getAvailableRoomsCountByTypeAndDateRange(Long typeId, LocalDateTime checkIn, LocalDateTime checkOut) {
        // 获取指定类型的所有房间
        Optional<RoomType> roomTypeOpt = roomTypeRepository.findById(typeId);
        if (!roomTypeOpt.isPresent()) {
            return 0;
        }
        
        RoomType roomType = roomTypeOpt.get();
        List<Room> roomsOfType = getRoomsByType(roomType);
        
        // 统计可用房间数量
        int availableCount = 0;
        
        for (Room room : roomsOfType) {
            // 首先检查房间当前状态是否可用
            if (room.getStatus() != Room.RoomStatus.AVAILABLE && 
                room.getStatus() != Room.RoomStatus.CLEANING) {
                continue;
            }
            
            // 再检查该房间在指定时间段内是否已被预订
            boolean isAvailable = getReservationService().isRoomAvailable(
                room.getId(), 
                checkIn.toString(),
                checkOut.toString()
            );
            
            if (isAvailable) {
                availableCount++;
            }
        }
        
        return availableCount;
    }
}