package com.hotel.repository;

import com.hotel.entity.Reservation;
import com.hotel.entity.Room;
import com.hotel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser(User user);

    List<Reservation> findByRoom(Room room);

    List<Reservation> findByStatus(Reservation.ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.user = ?1 AND r.status = ?2")
    List<Reservation> findByUserAndStatus(User user, Reservation.ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.checkInTime <= ?1 AND r.checkOutTime >= ?1")
    List<Reservation> findCurrentReservations(LocalDateTime currentTime);

    @Query("SELECT r FROM Reservation r WHERE r.room = ?1 AND r.status IN ('CONFIRMED', 'CHECKED_IN') " +
           "AND ((r.checkInTime BETWEEN ?2 AND ?3) OR (r.checkOutTime BETWEEN ?2 AND ?3))")
    List<Reservation> findConflictingReservations(Room room, LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.status = ?1")
    long countByStatus(Reservation.ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'CONFIRMED' AND r.checkInTime <= ?1")
    List<Reservation> findReservationsToCheckIn(LocalDateTime currentTime);

    List<Reservation> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    // 添加根据入住时间范围计数的方法
    int countByCheckInTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    // 添加根据入住时间之后计数的方法
    int countByCheckInTimeAfter(LocalDateTime time);
}