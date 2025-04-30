package com.hotel.service.impl;

import com.hotel.entity.AdditionalCharge;
import com.hotel.entity.Booking;
import com.hotel.entity.CheckInRecord;
import com.hotel.entity.Room;
import com.hotel.exception.ResourceNotFoundException;
import com.hotel.repository.AdditionalChargeRepository;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.CheckInRecordRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.service.CheckInService;
import com.hotel.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CheckInServiceImpl implements CheckInService {
    
    @Autowired
    private CheckInRecordRepository checkInRecordRepository;
    
    @Autowired
    private AdditionalChargeRepository additionalChargeRepository;
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private RoomService roomService;
    
    @Override
    public CheckInRecord checkIn(CheckInRecord checkInRecord) {
        // 生成入住单号
        checkInRecord.setCheckInNumber(generateCheckInNumber());
        
        // 设置入住时间
        checkInRecord.setActualCheckInTime(LocalDateTime.now());
        
        // 设置状态为已入住
        checkInRecord.setStatus(CheckInRecord.CheckInStatus.CHECKED_IN);
        
        // 如果有预订ID，关联预订信息
        if (checkInRecord.getBookingId() != null) {
            Booking booking = getBookingById(checkInRecord.getBookingId());
            // 在此可以进行更多与预订关联的操作
        }
        
        // 查找并更新房间信息
        Room room = findAvailableRoom(checkInRecord.getRoomId());
        checkInRecord.setRoomNumber(room.getRoomNumber());
        checkInRecord.setRoomType(room.getRoomType().getName());
        
        // 更新房间状态为已入住
        room.setStatus(Room.RoomStatus.OCCUPIED);
        roomRepository.save(room);
        
        // 保存入住记录
        return checkInRecordRepository.save(checkInRecord);
    }
    
    @Override
    public CheckInRecord getCheckInRecordById(Long id) {
        return checkInRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("入住记录", "ID", id));
    }
    
    @Override
    public Page<CheckInRecord> getCheckInRecords(
            CheckInRecord.CheckInStatus status,
            String roomNumber,
            String guestName,
            String guestMobile,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size) {
        
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        
        return checkInRecordRepository.findWithFilters(
                status, roomNumber, guestName, guestMobile, startDate, endDate, pageable);
    }
    
    @Override
    public String generateCheckInNumber() {
        // 生成格式：CI + yyyyMMdd + 4位序号
        String prefix = "CI" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String maxNumber = checkInRecordRepository.findMaxCheckInNumberWithPrefix(prefix);
        
        int sequence = 1;
        if (maxNumber != null && maxNumber.length() >= prefix.length() + 4) {
            String sequenceStr = maxNumber.substring(prefix.length());
            try {
                sequence = Integer.parseInt(sequenceStr) + 1;
            } catch (NumberFormatException e) {
                sequence = 1;
            }
        }
        
        return prefix + String.format("%04d", sequence);
    }
    
    @Override
    public Room findAvailableRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("房间", "ID", roomId));
        
        if (room.getStatus() != Room.RoomStatus.AVAILABLE) {
            throw new IllegalStateException("房间不可用，当前状态: " + room.getStatus());
        }
        
        return room;
    }
    
    @Override
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("预订", "ID", bookingId));
    }
    
    @Override
    public Room getRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("房间", "ID", roomId));
    }
    
    @Override
    public CheckInRecord checkout(Long checkInId, List<AdditionalCharge> additionalCharges, 
            BigDecimal returnDeposit, String remarks, CheckInRecord.PaymentMethod paymentMethod) {
        
        // 获取入住记录
        CheckInRecord checkInRecord = getCheckInRecordById(checkInId);
        
        // 验证入住状态
        if (checkInRecord.getStatus() != CheckInRecord.CheckInStatus.CHECKED_IN) {
            throw new IllegalStateException("只能对已入住的记录进行退房操作");
        }
        
        // 设置实际退房时间
        LocalDateTime now = LocalDateTime.now();
        checkInRecord.setActualCheckOutTime(now);
        
        // 添加额外消费记录
        BigDecimal totalAdditionalAmount = BigDecimal.ZERO;
        if (additionalCharges != null && !additionalCharges.isEmpty()) {
            for (AdditionalCharge charge : additionalCharges) {
                charge.setCheckInRecord(checkInRecord);
                additionalChargeRepository.save(charge);
                totalAdditionalAmount = totalAdditionalAmount.add(charge.getAmount());
            }
        }
        
        // 计算最终金额
        BigDecimal finalAmount = checkInRecord.getTotalAmount().add(totalAdditionalAmount);
        
        if (returnDeposit != null && returnDeposit.compareTo(BigDecimal.ZERO) > 0) {
            // 验证退还押金不超过原押金
            if (returnDeposit.compareTo(checkInRecord.getDeposit()) > 0) {
                throw new IllegalArgumentException("退还押金不能超过原收取押金");
            }
        }
        
        // 更新入住记录
        checkInRecord.setStatus(CheckInRecord.CheckInStatus.CHECKED_OUT);
        if (remarks != null && !remarks.isEmpty()) {
            checkInRecord.setRemarks(checkInRecord.getRemarks() + "; " + remarks);
        }
        
        // 更新房间状态
        Room room = getRoomById(checkInRecord.getRoomId());
        room.setStatus(Room.RoomStatus.NEEDS_CLEANING);
        room.setNeedCleaning(true);
        roomRepository.save(room);
        
        // 保存更新后的入住记录
        return checkInRecordRepository.save(checkInRecord);
    }
    
    @Override
    public Map<String, Object> getTodayCheckInAndCheckoutStats() {
        LocalDate today = LocalDate.now();
        
        // 获取今日预计入住的记录
        List<CheckInRecord> todayExpectedCheckIns = checkInRecordRepository.findByCheckInDate(today);
        
        // 获取今日预计退房的记录
        List<CheckInRecord> todayExpectedCheckOuts = checkInRecordRepository.findByCheckOutDate(today);
        
        // 获取今日已完成入住的记录
        List<CheckInRecord> todayCompletedCheckIns = checkInRecordRepository.findCompletedCheckInByDate(today);
        
        // 获取今日已完成退房的记录
        List<CheckInRecord> todayCompletedCheckOuts = checkInRecordRepository.findCompletedCheckOutByDate(today);
        
        // 获取入住率相关数据
        long totalRooms = roomRepository.count();
        long occupiedRooms = roomRepository.countByStatus(Room.RoomStatus.OCCUPIED);
        double occupancyRate = (double) occupiedRooms / totalRooms * 100;
        long availableRooms = roomRepository.countByStatus(Room.RoomStatus.AVAILABLE);
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        
        Map<String, Object> todayCheckin = new HashMap<>();
        todayCheckin.put("total", todayExpectedCheckIns.size());
        todayCheckin.put("completed", todayCompletedCheckIns.size());
        todayCheckin.put("pending", todayExpectedCheckIns.size() - todayCompletedCheckIns.size());
        todayCheckin.put("list", todayExpectedCheckIns);
        result.put("todayCheckin", todayCheckin);
        
        Map<String, Object> todayCheckout = new HashMap<>();
        todayCheckout.put("total", todayExpectedCheckOuts.size());
        todayCheckout.put("completed", todayCompletedCheckOuts.size());
        todayCheckout.put("pending", todayExpectedCheckOuts.size() - todayCompletedCheckOuts.size());
        todayCheckout.put("list", todayExpectedCheckOuts);
        result.put("todayCheckout", todayCheckout);
        
        result.put("occupancyRate", occupancyRate);
        result.put("availableRooms", availableRooms);
        
        return result;
    }
    
    @Override
    public AdditionalCharge addAdditionalCharge(Long checkInId, AdditionalCharge additionalCharge) {
        // 获取入住记录
        CheckInRecord checkInRecord = getCheckInRecordById(checkInId);
        
        // 验证入住状态
        if (checkInRecord.getStatus() != CheckInRecord.CheckInStatus.CHECKED_IN) {
            throw new IllegalStateException("只能为在住客人添加额外消费");
        }
        
        // 设置关联的入住记录
        additionalCharge.setCheckInRecord(checkInRecord);
        
        // 保存额外消费记录
        return additionalChargeRepository.save(additionalCharge);
    }
    
    @Override
    public CheckInRecord extendStay(Long checkInId, LocalDate newCheckOutDate, String remarks) {
        // 获取入住记录
        CheckInRecord checkInRecord = getCheckInRecordById(checkInId);
        
        // 验证入住状态
        if (checkInRecord.getStatus() != CheckInRecord.CheckInStatus.CHECKED_IN) {
            throw new IllegalStateException("只能为在住客人延长入住时间");
        }
        
        // 验证新的退房日期是否有效
        LocalDate originalCheckOutDate = checkInRecord.getCheckOutDate();
        if (newCheckOutDate.isBefore(originalCheckOutDate)) {
            throw new IllegalArgumentException("新的退房日期不能早于原退房日期");
        }
        
        // 获取房间信息
        Room room = getRoomById(checkInRecord.getRoomId());
        
        // 检查在新的退房日期范围内，房间是否已被预订
        // 这里可以添加检查逻辑，确保房间在延长的时间段内是空闲的
        
        // 计算延长的天数
        long additionalNights = ChronoUnit.DAYS.between(originalCheckOutDate, newCheckOutDate);
        
        // 计算额外费用 (按照原单价计算)
        BigDecimal dailyRate = checkInRecord.getTotalAmount().divide(
                new BigDecimal(ChronoUnit.DAYS.between(checkInRecord.getCheckInDate(), originalCheckOutDate)));
        BigDecimal additionalAmount = dailyRate.multiply(new BigDecimal(additionalNights));
        
        // 更新入住记录
        BigDecimal newTotalAmount = checkInRecord.getTotalAmount().add(additionalAmount);
        checkInRecord.setCheckOutDate(newCheckOutDate);
        checkInRecord.setTotalAmount(newTotalAmount);
        
        // 添加备注
        if (remarks != null && !remarks.isEmpty()) {
            String newRemarks = "延长入住至" + newCheckOutDate + ": " + remarks;
            if (checkInRecord.getRemarks() != null && !checkInRecord.getRemarks().isEmpty()) {
                checkInRecord.setRemarks(checkInRecord.getRemarks() + "; " + newRemarks);
            } else {
                checkInRecord.setRemarks(newRemarks);
            }
        }
        
        // 保存更新后的入住记录
        return checkInRecordRepository.save(checkInRecord);
    }
    
    @Override
    public CheckInRecord updateCheckInRecord(CheckInRecord checkInRecord) {
        // 验证入住记录
        if (checkInRecord == null) {
            throw new IllegalArgumentException("入住记录不能为空");
        }
        
        // 检查入住记录ID是否存在
        if (checkInRecord.getId() == null) {
            throw new IllegalArgumentException("入住记录ID不能为空");
        }
        
        // 检查入住记录是否存在
        CheckInRecord existingRecord = checkInRecordRepository.findById(checkInRecord.getId())
                .orElseThrow(() -> new ResourceNotFoundException("入住记录不存在，ID: " + checkInRecord.getId()));
        
        // 如果客人已退房，不允许更新
        if (existingRecord.getStatus() == CheckInRecord.CheckInStatus.CHECKED_OUT) {
            throw new IllegalStateException("客人已退房，无法更新入住信息");
        }
        
        // 保存更新后的记录
        return checkInRecordRepository.save(checkInRecord);
    }
    
    @Override
    public List<CheckInRecord> getCurrentlyCheckedInRecords() {
        // 使用repository中已定义的方法查询当前所有入住的客人记录
        return checkInRecordRepository.findCurrentlyCheckedIn();
    }
    
    @Override
    public BigDecimal calculateTodayRevenue() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        
        // 获取今日完成的所有退房记录
        List<CheckInRecord> records = checkInRecordRepository.findByCheckOutTimeBetweenAndStatus(
            startOfDay, endOfDay, CheckInRecord.CheckInStatus.CHECKED_OUT);
        
        // 计算总收入
        return records.stream()
            .map(CheckInRecord::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Override
    public BigDecimal calculateMonthlyRevenue() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDateTime startOfMonth = firstDayOfMonth.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        
        // 获取本月完成的所有退房记录
        List<CheckInRecord> records = checkInRecordRepository.findByCheckOutTimeBetweenAndStatus(
            startOfMonth, endOfDay, CheckInRecord.CheckInStatus.CHECKED_OUT);
        
        // 计算总收入
        return records.stream()
            .map(CheckInRecord::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
} 