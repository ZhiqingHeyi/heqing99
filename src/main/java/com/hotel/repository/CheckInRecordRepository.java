package com.hotel.repository;

import com.hotel.entity.Booking;
import com.hotel.entity.CheckInRecord;
import com.hotel.entity.CheckInRecord.DepositStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckInRecordRepository extends JpaRepository<CheckInRecord, Long> {
    Optional<CheckInRecord> findByBooking(Booking booking);
    
    List<CheckInRecord> findByDepositStatus(DepositStatus depositStatus);
    
    @Query("SELECT c FROM CheckInRecord c WHERE c.actualCheckIn BETWEEN :startTime AND :endTime")
    List<CheckInRecord> findByCheckInTimeBetween(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    @Query("SELECT c FROM CheckInRecord c WHERE c.actualCheckOut IS NULL AND c.actualCheckIn <= :time")
    List<CheckInRecord> findCurrentlyCheckedIn(@Param("time") LocalDateTime time);
    
    @Query("SELECT c FROM CheckInRecord c WHERE c.depositStatus = 'PAID' AND c.actualCheckOut IS NOT NULL")
    List<CheckInRecord> findCompletedWithDeposit();
}