package com.hotel.service;

import com.hotel.entity.Booking;
import com.hotel.entity.CheckInRecord;
import com.hotel.entity.Reservation;
import com.hotel.entity.Room;
import com.hotel.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ReservationService {
    /**
     * 创建预订
     */
    Reservation createReservation(Map<String, Object> reservationData) throws RuntimeException;

    /**
     * 获取指定用户的预订列表
     */
    List<Reservation> getUserReservations(Long userId);

    /**
     * 获取预订详情
     */
    Reservation getReservationById(Long id);

    /**
     * 更新预订信息
     */
    Reservation updateReservation(Reservation reservation);

    /**
     * 取消预订
     */
    void cancelReservation(Long id);

    /**
     * 获取所有预订列表
     */
    List<Reservation> getAllReservations();

    /**
     * 检查房间在指定日期是否可预订
     */
    boolean isRoomAvailable(Long roomId, String startDate, String endDate);

    /**
     * 统计今日预订数量
     */
    long countTodayReservations();
    
    /**
     * 统计未来预订数量
     */
    int countUpcomingReservations();
    
    /**
     * 获取今日预订详情列表
     */
    List<Map<String, Object>> getTodayReservationsWithDetails();
    
    /**
     * 根据预订号查询预订详情
     */
    Map<String, Object> getBookingDetailsByNumber(String bookingNo);
    
    /**
     * 处理入住
     */
    CheckInRecord processCheckIn(CheckInRecord checkInRecord);
    
    /**
     * 处理退房
     */
    void processCheckOut(String roomNumber);

    /**
     * 确认预订
     */
    Reservation confirmReservation(Long reservationId);

    /**
     * 办理入住
     */
    Reservation checkIn(Long reservationId);

    /**
     * 办理退房
     */
    Reservation checkOut(Long reservationId);

    /**
     * 获取用户的所有预订
     */
    List<Reservation> getReservationsByUser(User user);

    /**
     * 获取房间的所有预订
     */
    List<Reservation> getReservationsByRoom(Room room);

    /**
     * 获取指定状态的预订
     */
    List<Reservation> getReservationsByStatus(Reservation.ReservationStatus status);

    /**
     * 获取当前所有有效预订
     */
    List<Reservation> getCurrentReservations();

    /**
     * 获取待入住的预订
     */
    List<Reservation> getReservationsToCheckIn();

    /**
     * 统计指定状态的预订数量
     */
    long countReservationsByStatus(Reservation.ReservationStatus status);

    /**
     * 获取用户指定状态的预订
     */
    List<Reservation> getReservationsByUserAndStatus(User user, Reservation.ReservationStatus status);

    /**
     * 检查房间是否有关联的预订记录
     */
    boolean hasRoomReservations(Long roomId);
    
    /**
     * 完成预订
     */
    Reservation completeReservation(Long reservationId);

    /**
     * 获取预订统计数据
     */
    Map<String, Object> getReservationStatistics(String period);

    /**
     * 获取用户的预订列表（分页和可选状态过滤）
     */
    Page<Reservation> getUserReservationsPaginated(Long userId, Reservation.ReservationStatus status, Pageable pageable);
}