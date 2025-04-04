package com.hotel.service.impl;

import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.RoomTypeRepository;
import com.hotel.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

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
}