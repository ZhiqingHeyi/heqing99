package com.hotel.service.impl;

import com.hotel.entity.CheckInRecord;
import com.hotel.entity.Reservation;
import com.hotel.entity.Room;
import com.hotel.entity.User;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.CheckInRecordRepository;
import com.hotel.service.ReservationService;
import com.hotel.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    private CheckInRecordRepository checkInRecordRepository;

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
        // 使用实际的字段名
        LocalDateTime now = LocalDateTime.now();
        reservation.setCheckInTime(now);
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
        // 使用实际的字段名
        LocalDateTime now = LocalDateTime.now();
        reservation.setCheckOutTime(now);
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
            // 跳过取消的预订
            if (existing.getStatus() == Reservation.ReservationStatus.CANCELLED || 
                existing.getStatus() == Reservation.ReservationStatus.COMPLETED) {
                continue;
            }
            
            // 检查时间冲突
            if (!(newReservation.getCheckOutTime().isBefore(existing.getCheckInTime()) ||
                  newReservation.getCheckInTime().isAfter(existing.getCheckOutTime()))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isRoomAvailable(Long roomId, String startDateStr, String endDateStr) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("房间不存在"));
        
        LocalDateTime startTime, endTime;
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        
        try {
            startTime = LocalDateTime.parse(startDateStr, formatter);
            endTime = LocalDateTime.parse(endDateStr, formatter);
        } catch (DateTimeParseException e) {
            try {
                // 尝试使用日期格式
                LocalDate startDate = LocalDate.parse(startDateStr);
                LocalDate endDate = LocalDate.parse(endDateStr);
                startTime = startDate.atStartOfDay();
                endTime = endDate.atTime(23, 59, 59);
            } catch (DateTimeParseException ex) {
                throw new RuntimeException("日期格式无效");
            }
        }

        // 创建临时预订对象以检查冲突
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

    @Override
    public long countTodayReservations() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        
        // 使用现有的查询方法替代countByCheckInDateBetween
        List<Reservation> todayReservations = reservationRepository.findByCreateTimeBetween(start, end);
        return todayReservations.size();
    }
    
    @Override
    public List<Map<String, Object>> getTodayReservationsWithDetails() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        
        // 使用现有的查询方法替代findByCheckInDateBetween
        List<Reservation> todayReservations = reservationRepository.findByCreateTimeBetween(start, end);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Reservation reservation : todayReservations) {
            Map<String, Object> details = new HashMap<>();
            details.put("id", reservation.getId());
            details.put("guestName", reservation.getGuestName());
            details.put("phone", reservation.getGuestPhone());
            details.put("roomNumber", reservation.getRoom().getRoomNumber());
            details.put("roomType", reservation.getRoom().getRoomType().getName());
            details.put("checkInTime", reservation.getCheckInTime());
            details.put("checkOutTime", reservation.getCheckOutTime());
            details.put("status", reservation.getStatus().toString());
            
            result.add(details);
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> getBookingDetailsByNumber(String bookingNo) {
        // 由于没有bookingNo字段，我们用ID替代
        try {
            Long id = Long.parseLong(bookingNo);
            Reservation reservation = getReservationById(id);
            
            Map<String, Object> details = new HashMap<>();
            details.put("id", reservation.getId());
            details.put("guestName", reservation.getGuestName());
            details.put("phone", reservation.getGuestPhone());
            details.put("roomNumber", reservation.getRoom().getRoomNumber());
            details.put("roomType", reservation.getRoom().getRoomType().getName());
            details.put("checkInTime", reservation.getCheckInTime());
            details.put("checkOutTime", reservation.getCheckOutTime());
            details.put("totalPrice", reservation.getTotalPrice());
            details.put("status", reservation.getStatus().toString());
            
            return details;
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    @Transactional
    public CheckInRecord processCheckIn(CheckInRecord checkInRecord) {
        // 设置入住时间
        checkInRecord.setActualCheckIn(LocalDateTime.now());
        
        // 保存记录
        return checkInRecordRepository.save(checkInRecord);
    }
    
    @Override
    @Transactional
    public void processCheckOut(String roomNumber) {
        // 查找房间
        Room room = roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new RuntimeException("房间不存在: " + roomNumber));
        
        // 更新房间状态为待清洁
        room.setStatus(Room.RoomStatus.CLEANING);
        roomRepository.save(room);
        
        // 查找相关的入住预订
        List<Reservation> currentReservations = getCurrentReservations();
        for (Reservation reservation : currentReservations) {
            if (reservation.getRoom().getRoomNumber().equals(roomNumber)) {
                reservation.setStatus(Reservation.ReservationStatus.COMPLETED);
                reservation.setCheckOutTime(LocalDateTime.now());
                reservationRepository.save(reservation);
                break;
            }
        }
    }
    
    @Override
    public List<Reservation> getUserReservations(Long userId) {
        // 创建User对象
        User user = new User();
        user.setId(userId);
        return reservationRepository.findByUser(user);
    }
    
    @Override
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }
}