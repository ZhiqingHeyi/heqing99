package com.hotel.service.impl;

import com.hotel.entity.CheckInRecord;
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
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.LocalDate;

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

    // 统计今日预订数量
    @Override
    public long countTodayReservations() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        return reservationRepository.countByCheckInDateBetween(start, end);
    }
    
    // 获取今日预订详情列表
    @Override
    public List<Map<String, Object>> getTodayReservationsWithDetails() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        
        List<Reservation> todayReservations = reservationRepository.findByCheckInDateBetween(start, end);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Reservation reservation : todayReservations) {
            Map<String, Object> details = new HashMap<>();
            details.put("id", reservation.getId());
            details.put("bookingNo", reservation.getBookingNo());
            details.put("guestName", reservation.getGuestName());
            details.put("phone", reservation.getPhone());
            details.put("roomNumber", reservation.getRoom().getRoomNumber());
            details.put("roomType", reservation.getRoom().getRoomType().getName());
            details.put("checkInDate", reservation.getCheckInDate());
            details.put("checkOutDate", reservation.getCheckOutDate());
            
            result.add(details);
        }
        
        return result;
    }
    
    // 根据预订号查询预订详情
    @Override
    public Map<String, Object> getBookingDetailsByNumber(String bookingNo) {
        Optional<Reservation> reservation = reservationRepository.findByBookingNo(bookingNo);
        if (!reservation.isPresent()) {
            return null;
        }
        
        Reservation res = reservation.get();
        Map<String, Object> details = new HashMap<>();
        details.put("id", res.getId());
        details.put("bookingNo", res.getBookingNo());
        details.put("guestName", res.getGuestName());
        details.put("phone", res.getPhone());
        details.put("idCard", res.getIdCard());
        details.put("roomNumber", res.getRoom().getRoomNumber());
        details.put("roomType", res.getRoom().getRoomType().getName());
        details.put("checkInDate", res.getCheckInDate());
        details.put("checkOutDate", res.getCheckOutDate());
        details.put("price", res.getPrice());
        details.put("status", res.getStatus());
        
        return details;
    }
    
    // 处理入住
    @Override
    @Transactional
    public CheckInRecord processCheckIn(CheckInRecord checkInRecord) {
        // 如果有预订号，则关联预订
        if (checkInRecord.getBookingNo() != null && !checkInRecord.getBookingNo().isEmpty()) {
            Optional<Reservation> reservation = reservationRepository.findByBookingNo(checkInRecord.getBookingNo());
            if (reservation.isPresent()) {
                // 更新预订状态
                Reservation res = reservation.get();
                res.setStatus(Reservation.ReservationStatus.CHECKED_IN);
                reservationRepository.save(res);
                
                // 关联预订信息
                checkInRecord.setReservation(res);
            } else {
                throw new RuntimeException("找不到对应的预订记录");
            }
        }
        
        // 更新房间状态
        Room room = roomService.getRoomByNumber(checkInRecord.getRoomNumber());
        if (room == null) {
            throw new RuntimeException("找不到对应的房间");
        }
        
        // 检查房间状态是否可入住
        if (room.getStatus() != Room.RoomStatus.AVAILABLE) {
            throw new RuntimeException("该房间当前不可入住");
        }
        
        // 更新房间状态为已入住
        roomService.updateRoomStatus(room.getId(), Room.RoomStatus.OCCUPIED);
        
        // 保存入住记录
        checkInRecord.setCheckInTime(LocalDateTime.now());
        return checkInRecordRepository.save(checkInRecord);
    }
    
    // 处理退房
    @Override
    @Transactional
    public void processCheckOut(String roomNumber) {
        // 查找房间
        Room room = roomService.getRoomByNumber(roomNumber);
        if (room == null) {
            throw new RuntimeException("找不到对应的房间");
        }
        
        // 检查房间状态
        if (room.getStatus() != Room.RoomStatus.OCCUPIED) {
            throw new RuntimeException("该房间当前不是入住状态");
        }
        
        // 查找最近的入住记录
        CheckInRecord checkInRecord = checkInRecordRepository.findTopByRoomNumberOrderByCheckInTimeDesc(roomNumber);
        if (checkInRecord == null) {
            throw new RuntimeException("找不到该房间的入住记录");
        }
        
        // 更新入住记录
        checkInRecord.setCheckOutTime(LocalDateTime.now());
        checkInRecordRepository.save(checkInRecord);
        
        // 更新关联的预订记录
        if (checkInRecord.getReservation() != null) {
            Reservation reservation = checkInRecord.getReservation();
            reservation.setStatus(Reservation.ReservationStatus.COMPLETED);
            reservationRepository.save(reservation);
        }
        
        // 将房间标记为待清洁状态
        roomService.updateRoomStatus(room.getId(), Room.RoomStatus.CLEANING);
        roomService.markRoomNeedCleaning(room.getId());
    }

    // 检查房间在指定日期是否可预订
    @Override
    public boolean isRoomAvailable(Long roomId, String startDate, String endDate) {
        // 解析日期
        LocalDate start;
        LocalDate end;
        
        try {
            start = LocalDate.parse(startDate);
            end = LocalDate.parse(endDate);
        } catch (Exception e) {
            throw new RuntimeException("日期格式不正确，请使用yyyy-MM-dd格式");
        }
        
        Room room = roomService.getRoomById(roomId);
        if (room == null) {
            throw new RuntimeException("找不到对应的房间");
        }
        
        // 检查该时间段内是否有其他预订
        List<Reservation> overlappingReservations = reservationRepository.findOverlappingReservations(
            roomId, 
            start.atStartOfDay(), 
            end.atStartOfDay()
        );
        
        return overlappingReservations.isEmpty();
    }
    
    // 获取指定用户的预订列表
    @Override
    public List<Reservation> getUserReservations(Long userId) {
        return reservationRepository.findByUserId(userId);
    }
    
    // 获取所有预订列表
    @Override
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }
}