package com.hotel.service;

import com.hotel.entity.Reservation;
import com.hotel.entity.Room;
import com.hotel.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationService {
    /**
     * 创建预订
     */
    Reservation createReservation(Reservation reservation);

    /**
     * 更新预订信息
     */
    Reservation updateReservation(Reservation reservation);

    /**
     * 取消预订
     */
    void cancelReservation(Long reservationId);

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
     * 获取预订详情
     */
    Reservation getReservationById(Long id);

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
     * 检查房间在指定时间段是否可预订
     */
    boolean isRoomAvailable(Room room, LocalDateTime startTime, LocalDateTime endTime);

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
}