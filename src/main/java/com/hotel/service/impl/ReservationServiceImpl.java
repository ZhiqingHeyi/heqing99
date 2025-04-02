package com.hotel.service.impl;

import com.hotel.entity.Reservation;
import com.hotel.entity.Room;
import com.hotel.entity.User;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.service.ReservationService;
import com.hotel.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomService roomService;

    @Override
    public Reservation createReservation(Reservation reservation) {
        // 检查房间是否可用
        Room room = roomRepository.findById(reservation.getRoom().getId())
                .orElseThrow(() -> new RuntimeException("房间不存在"));

        if (room.getStatus() != Room.RoomStatus.AVAILABLE) {
            throw new RuntimeException("房间当前不可预订");
        }

        // 检查预订时间段是否有冲突
        if (hasTimeConflict(reservation)) {
            throw new RuntimeException("该时间段房间已被预订");
        }

        // 设置预订状态为待确认
        reservation.setStatus(Reservation.ReservationStatus.PENDING);
        reservation.setCreateTime(LocalDateTime.now());

        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation updateReservation(Reservation reservation) {
        Reservation existingReservation = getReservationById(reservation.getId());

        // 只允许更新待确认状态的预订
        if (existingReservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            throw new RuntimeException("当前状态不允许修改预订信息");
        }

        // 检查时间冲突
        if (hasTimeConflict(reservation)) {
            throw new RuntimeException("该时间段房间已被预订");
        }

        return reservationRepository.save(reservation);
    }

    @Override
    public void cancelReservation(Long reservationId) {
        Reservation reservation = getReservationById(reservationId);

        // 只允许取消待确认或已确认状态的预订
        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING &&
            reservation.getStatus() != Reservation.ReservationStatus.CONFIRMED) {
            throw new RuntimeException("当前状态不允许取消预订");
        }

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    @Override
    public Reservation confirmReservation(Long reservationId) {
        Reservation reservation = getReservationById(reservationId);

        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            throw new RuntimeException("只能确认待确认状态的预订");
        }

        reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation checkIn(Long reservationId) {
        Reservation reservation = getReservationById(reservationId);

        if (reservation.getStatus() != Reservation.ReservationStatus.CONFIRMED) {
            throw new RuntimeException("只能为已确认的预订办理入住");
        }

        // 更新房间状态为已入住
        roomService.updateRoomStatus(reservation.getRoom().getId(), Room.RoomStatus.OCCUPIED);

        reservation.setStatus(Reservation.ReservationStatus.CHECKED_IN);
        reservation.setCheckInTime(LocalDateTime.now());
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation checkOut(Long reservationId) {
        Reservation reservation = getReservationById(reservationId);

        if (reservation.getStatus() != Reservation.ReservationStatus.CHECKED_IN) {
            throw new RuntimeException("只能为已入住的预订办理退房");
        }

        // 更新房间状态为需要清洁
        roomService.markRoomNeedCleaning(reservation.getRoom().getId());

        reservation.setStatus(Reservation.ReservationStatus.COMPLETED);
        reservation.setCheckOutTime(LocalDateTime.now());
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("预订记录不存在"));
    }

    @Override
    public List<Reservation> getReservationsByUser(User user) {
        return reservationRepository.findByUser(user);
    }

    @Override
    public List<Reservation> getReservationsByRoom(Room room) {
        return reservationRepository.findByRoom(room);
    }

    @Override
    public List<Reservation> getReservationsByStatus(Reservation.ReservationStatus status) {
        return reservationRepository.findByStatus(status);
    }

    @Override
    public List<Reservation> getReservationsToCheckIn() {
        return reservationRepository.findReservationsToCheckIn(LocalDateTime.now());
    }

    @Override
    public List<Reservation> getReservationsByUserAndStatus(User user, Reservation.ReservationStatus status) {
        return reservationRepository.findByUserAndStatus(user, status);
    }

    @Override
    public List<Reservation> getCurrentReservations() {
        return reservationRepository.findByStatus(Reservation.ReservationStatus.CHECKED_IN);
    }

    private boolean hasTimeConflict(Reservation newReservation) {
        List<Reservation> existingReservations = reservationRepository.findByRoom(newReservation.getRoom());

        for (Reservation existing : existingReservations) {
            if (!(newReservation.getCheckOutTime().isBefore(existing.getCheckInTime()) ||
                  newReservation.getCheckInTime().isAfter(existing.getCheckOutTime()))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isRoomAvailable(Room room, LocalDateTime startTime, LocalDateTime endTime) {
        Reservation tempReservation = new Reservation();
        tempReservation.setRoom(room);
        tempReservation.setCheckInTime(startTime);
        tempReservation.setCheckOutTime(endTime);
        return !hasTimeConflict(tempReservation);
    }

    @Override
    public long countReservationsByStatus(Reservation.ReservationStatus status) {
        return reservationRepository.countByStatus(status);
    }
}