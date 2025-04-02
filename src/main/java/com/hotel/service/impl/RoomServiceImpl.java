package com.hotel.service.impl;

import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import com.hotel.repository.RoomRepository;
import com.hotel.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomRepository roomRepository;

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
        return roomRepository.findAvailableRoomsByType(type);
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
}