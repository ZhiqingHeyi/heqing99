package com.hotel.repository;

import com.hotel.entity.Reservation;
import com.hotel.entity.Room;
import com.hotel.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser(User user);

    Page<Reservation> findByUser(User user, Pageable pageable);

    List<Reservation> findByRoom(Room room);

    List<Reservation> findByStatus(Reservation.ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.user = ?1 AND r.status = ?2")
    List<Reservation> findByUserAndStatus(User user, Reservation.ReservationStatus status);

    Page<Reservation> findByUserAndStatus(User user, Reservation.ReservationStatus status, Pageable pageable);

    @Query("SELECT r FROM Reservation r WHERE r.checkInTime <= ?1 AND r.checkOutTime >= ?1")
    List<Reservation> findCurrentReservations(LocalDateTime currentTime);

    @Query("SELECT r FROM Reservation r WHERE r.room = ?1 AND r.status IN ('CONFIRMED', 'CHECKED_IN') " +
           "AND ((r.checkInTime BETWEEN ?2 AND ?3) OR (r.checkOutTime BETWEEN ?2 AND ?3))")
    List<Reservation> findConflictingReservations(Room room, LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.status = ?1")
    long countByStatus(Reservation.ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'CONFIRMED' AND r.checkInTime <= :now")
    List<Reservation> findReservationsToCheckIn(@Param("now") LocalDateTime now);

    List<Reservation> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    // 添加根据入住时间范围计数的方法
    int countByCheckInTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    // 添加根据创建时间范围计数的方法
    long countByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    // 添加根据入住时间之后计数的方法
    int countByCheckInTimeAfter(LocalDateTime time);

    boolean existsByRoomId(Long roomId);

    @Query("SELECT r FROM Reservation r WHERE r.room.id = :roomId " +
           "AND r.status IN :statuses " +
           "AND (:endTime > r.checkInTime AND :startTime < r.checkOutTime)") // Overlap condition
    List<Reservation> findConflictingReservationsForRoom(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("statuses") List<Reservation.ReservationStatus> statuses);

    @Query(value = "SELECT DISTINCT r FROM Reservation r " +
                   "LEFT JOIN FETCH r.user u " +
                   "LEFT JOIN FETCH r.room rm " +
                   "LEFT JOIN FETCH rm.roomType rt " +
                   "WHERE (:status IS NULL OR r.status = :status) " +
                   "AND (:guestName IS NULL OR lower(r.guestName) LIKE lower(concat('%', :guestName, '%'))) " +
                   "AND (:guestPhone IS NULL OR r.guestPhone LIKE concat('%', :guestPhone, '%')) " +
                   "AND (:bookingNo IS NULL OR CAST(r.id as string) LIKE concat('%', :bookingNo, '%')) " +
                   "AND (:startDate IS NULL OR r.checkInTime >= :startDate) " +
                   "AND (:endDate IS NULL OR r.checkOutTime <= :endDate) "
                   ,
           countQuery = "SELECT count(DISTINCT r) FROM Reservation r " +
                      "LEFT JOIN r.user u " +
                      "LEFT JOIN r.room rm " +
                      "WHERE (:status IS NULL OR r.status = :status) " +
                      "AND (:guestName IS NULL OR lower(r.guestName) LIKE lower(concat('%', :guestName, '%'))) " +
                      "AND (:guestPhone IS NULL OR r.guestPhone LIKE concat('%', :guestPhone, '%')) " +
                      "AND (:bookingNo IS NULL OR CAST(r.id as string) LIKE concat('%', :bookingNo, '%')) " +
                      "AND (:startDate IS NULL OR r.checkInTime >= :startDate) " +
                      "AND (:endDate IS NULL OR r.checkOutTime <= :endDate) "
          )
    Page<Reservation> findReservationsFiltered(
            Pageable pageable,
            @Param("status") Reservation.ReservationStatus status,
            @Param("guestName") String guestName,
            @Param("guestPhone") String guestPhone,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("bookingNo") String bookingNo
    );
    
    /**
     * 查询指定创建时间范围内并且支付状态在指定列表中的预订
     * @param start 开始时间
     * @param end 结束时间
     * @param paymentStatuses 支付状态列表
     * @return 符合条件的预订列表
     */
    List<Reservation> findByCreateTimeBetweenAndPaymentStatusIn(
            LocalDateTime start,
            LocalDateTime end,
            List<String> paymentStatuses
    );
}