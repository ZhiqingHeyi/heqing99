package com.hotel.repository;

import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByStatus(Room.RoomStatus status);

    List<Room> findByRoomType(RoomType type);

    List<Room> findByNeedCleaningTrue();

    Optional<Room> findByRoomNumber(String roomNumber);

    @Query("SELECT r FROM Room r WHERE r.status = 'AVAILABLE' AND r.roomType = ?1")
    List<Room> findAvailableRoomsByType(RoomType type);

    List<Room> findByRoomTypeAndStatusIn(RoomType roomType, Collection<Room.RoomStatus> statuses);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.status = ?1")
    long countByStatus(Room.RoomStatus status);

    @Query("SELECT r FROM Room r WHERE r.status = 'AVAILABLE' OR r.status = 'CLEANING'")
    List<Room> findAllAvailableAndCleaningRooms();

    boolean existsByRoomNumber(String roomNumber);

    boolean existsByRoomTypeId(Long typeId);

    List<Room> findByRoomNumberIn(List<String> roomNumbers);

    List<Room> findByRoomTypeId(Long roomTypeId);

    long countByNeedCleaning(boolean needCleaning);
}