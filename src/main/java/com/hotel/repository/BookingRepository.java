package com.hotel.repository;

import com.hotel.entity.Booking;
import com.hotel.entity.Room;
import com.hotel.entity.User;
import com.hotel.entity.Booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);
    
    List<Booking> findByRoom(Room room);
    
    List<Booking> findByStatus(BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.checkInDate = :date")
    List<Booking> findByStatusAndCheckInDate(@Param("status") BookingStatus status, @Param("date") LocalDate date);
    
    @Query("SELECT b FROM Booking b WHERE b.room = :room AND b.status IN ('CONFIRMED', 'PENDING') " +
           "AND ((b.checkInDate BETWEEN :startDate AND :endDate) OR " +
           "(b.checkOutDate BETWEEN :startDate AND :endDate) OR " +
           "(b.checkInDate <= :startDate AND b.checkOutDate >= :endDate))")
    List<Booking> findConflictingBookings(
        @Param("room") Room room,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    List<Booking> findByUserAndStatusOrderByCreateTimeDesc(User user, BookingStatus status);
}