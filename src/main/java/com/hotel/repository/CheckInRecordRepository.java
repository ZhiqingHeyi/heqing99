package com.hotel.repository;

import com.hotel.entity.CheckInRecord;
import com.hotel.entity.CheckInRecord.CheckInStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckInRecordRepository extends JpaRepository<CheckInRecord, Long> {
    
    // 根据入住状态查询
    List<CheckInRecord> findByStatus(CheckInStatus status);
    
    // 根据房间号查询
    List<CheckInRecord> findByRoomNumber(String roomNumber);
    
    // 根据客人姓名模糊查询
    List<CheckInRecord> findByGuestNameContaining(String guestName);
    
    // 根据客人手机号查询
    List<CheckInRecord> findByGuestMobile(String guestMobile);
    
    // 根据入住日期范围查询
    List<CheckInRecord> findByCheckInDateBetween(LocalDate startDate, LocalDate endDate);
    
    // 分页查询所有入住记录
    Page<CheckInRecord> findAll(Pageable pageable);
    
    // 根据状态分页查询
    Page<CheckInRecord> findByStatus(CheckInStatus status, Pageable pageable);
    
    // 自定义查询(处理nullable条件)
    @Query("SELECT c FROM CheckInRecord c WHERE " +
            "(:status IS NULL OR c.status = :status) AND " +
            "(:roomNumber IS NULL OR c.roomNumber = :roomNumber) AND " +
            "(:guestName IS NULL OR c.guestName LIKE %:guestName%) AND " +
            "(:guestMobile IS NULL OR c.guestMobile = :guestMobile) AND " +
            "(:startDate IS NULL OR c.checkInDate >= :startDate) AND " +
            "(:endDate IS NULL OR c.checkInDate <= :endDate)")
    Page<CheckInRecord> findWithFilters(
            @Param("status") CheckInStatus status,
            @Param("roomNumber") String roomNumber,
            @Param("guestName") String guestName,
            @Param("guestMobile") String guestMobile,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
    
    // 生成入住单号的查询
    @Query("SELECT MAX(c.checkInNumber) FROM CheckInRecord c WHERE c.checkInNumber LIKE :prefix%")
    String findMaxCheckInNumberWithPrefix(@Param("prefix") String prefix);
    
    // 根据预订ID查询 (修正方法名以匹配实体属性 bookingId)
    Optional<CheckInRecord> findByBookingId(Long bookingId);
    
    // 查询当前仍在住宿中的记录
    @Query("SELECT c FROM CheckInRecord c WHERE c.status = 'CHECKED_IN' AND c.actualCheckOutTime IS NULL")
    List<CheckInRecord> findCurrentlyCheckedIn();
    
    // 查询今日预计入住的记录
    List<CheckInRecord> findByCheckInDate(LocalDate date);
    
    // 查询今日预计退房的记录
    List<CheckInRecord> findByCheckOutDate(LocalDate date);
    
    // 查询已入住的记录数
    long countByStatus(CheckInStatus status);
    
    // 查询今日已完成入住的记录
    @Query("SELECT c FROM CheckInRecord c WHERE c.checkInDate = :date AND c.status = 'CHECKED_IN'")
    List<CheckInRecord> findCompletedCheckInByDate(@Param("date") LocalDate date);
    
    // 查询今日已完成退房的记录
    @Query("SELECT c FROM CheckInRecord c WHERE c.checkOutDate = :date AND c.status = 'CHECKED_OUT'")
    List<CheckInRecord> findCompletedCheckOutByDate(@Param("date") LocalDate date);
    
    /**
     * 查询指定时间范围内退房的记录，用于统计收入
     */
    @Query("SELECT c FROM CheckInRecord c WHERE c.actualCheckOutTime BETWEEN :start AND :end AND c.status = :status")
    List<CheckInRecord> findByCheckOutTimeBetweenAndStatus(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("status") CheckInStatus status);

    /**
     * 查询指定房间当前有效的入住记录 (按入住日期降序，取第一条)
     * @param roomNumber 房间号
     * @param status 入住状态 (通常是 CHECKED_IN)
     * @return 最新的有效入住记录
     */
    Optional<CheckInRecord> findTopByRoomNumberAndStatusOrderByCheckInDateDesc(String roomNumber, CheckInStatus status);

    /**
     * 查询指定日期范围内的退房记录总收入
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总收入
     */
    @Query("SELECT SUM(c.totalAmount) FROM CheckInRecord c WHERE c.checkOutDate BETWEEN :startDate AND :endDate AND c.status = 'CHECKED_OUT'")
    BigDecimal sumTotalAmountByCheckOutDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 查询退房记录列表
     * @param roomNumber 房间号（可选）
     * @param guestName 客人姓名（可选）
     * @param checkoutDate 退房日期（可选）
     * @param pageable 分页参数
     * @return 符合条件的退房记录页
     */
    @Query("SELECT c FROM CheckInRecord c WHERE c.status = 'CHECKED_OUT' AND " +
            "(:roomNumber IS NULL OR c.roomNumber = :roomNumber) AND " +
            "(:guestName IS NULL OR c.guestName LIKE %:guestName%) AND " +
            "(:checkoutDate IS NULL OR c.checkOutDate = :checkoutDate)")
    Page<CheckInRecord> findCheckoutRecords(
            @Param("roomNumber") String roomNumber,
            @Param("guestName") String guestName,
            @Param("checkoutDate") LocalDate checkoutDate,
            Pageable pageable);

    /**
     * 计算退房记录总数
     * @param roomNumber 房间号（可选）
     * @param guestName 客人姓名（可选）
     * @param checkoutDate 退房日期（可选）
     * @return 符合条件的记录总数
     */
    @Query("SELECT COUNT(c) FROM CheckInRecord c WHERE c.status = 'CHECKED_OUT' AND " +
            "(:roomNumber IS NULL OR c.roomNumber = :roomNumber) AND " +
            "(:guestName IS NULL OR c.guestName LIKE %:guestName%) AND " +
            "(:checkoutDate IS NULL OR c.checkOutDate = :checkoutDate)")
    long countCheckoutRecords(
            @Param("roomNumber") String roomNumber,
            @Param("guestName") String guestName,
            @Param("checkoutDate") LocalDate checkoutDate);

    /**
     * 根据状态和退房日期统计数量
     * @param status 状态
     * @param checkOutDate 退房日期
     * @return 记录数量
     */
    long countByStatusAndCheckOutDate(CheckInStatus status, LocalDate checkOutDate);

    /**
     * 根据状态和退房日期范围统计数量
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 记录数量
     */
    long countByStatusAndCheckOutDateBetween(CheckInStatus status, LocalDate startDate, LocalDate endDate);

    /**
     * 查找指定房间ID和状态的入住记录
     * @param roomId 房间ID
     * @param status 入住状态
     * @return 入住记录列表
     */
    List<CheckInRecord> findByRoomIdAndStatus(Long roomId, CheckInStatus status);

    /**
     * 根据多种条件和 actual_check_out_time 范围查询退房记录并分页
     * @param roomNumber 房间号（可选）
     * @param guestName 客人姓名（可选）
     * @param startDateTime 开始时间（可选，基于 actual_check_out_time）
     * @param endDateTime 结束时间（可选，基于 actual_check_out_time）
     * @param pageable 分页参数
     * @return 符合条件的退房记录页
     */
    @Query("SELECT c FROM CheckInRecord c WHERE c.status = 'CHECKED_OUT' AND " +
           "(:roomNumber IS NULL OR c.roomNumber = :roomNumber) AND " +
           "(:guestName IS NULL OR c.guestName LIKE %:guestName%) AND " +
           "(:startDateTime IS NULL OR c.actualCheckOutTime >= :startDateTime) AND " +
           "(:endDateTime IS NULL OR c.actualCheckOutTime < :endDateTime)")
    Page<CheckInRecord> findCheckoutRecordsByFilterAndDateRange(
            @Param("roomNumber") String roomNumber,
            @Param("guestName") String guestName,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            Pageable pageable);

    /**
     * 根据多种条件和 actual_check_out_time 范围计算退房记录总数
     * @param roomNumber 房间号（可选）
     * @param guestName 客人姓名（可选）
     * @param startDateTime 开始时间（可选，基于 actual_check_out_time）
     * @param endDateTime 结束时间（可选，基于 actual_check_out_time）
     * @return 符合条件的记录总数
     */
    @Query("SELECT COUNT(c) FROM CheckInRecord c WHERE c.status = 'CHECKED_OUT' AND " +
           "(:roomNumber IS NULL OR c.roomNumber = :roomNumber) AND " +
           "(:guestName IS NULL OR c.guestName LIKE %:guestName%) AND " +
           "(:startDateTime IS NULL OR c.actualCheckOutTime >= :startDateTime) AND " +
           "(:endDateTime IS NULL OR c.actualCheckOutTime < :endDateTime)")
    long countCheckoutRecordsByFilterAndDateRange(
            @Param("roomNumber") String roomNumber,
            @Param("guestName") String guestName,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);
}