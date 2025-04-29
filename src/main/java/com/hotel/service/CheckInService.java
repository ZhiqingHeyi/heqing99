package com.hotel.service;

import com.hotel.entity.Booking;
import com.hotel.entity.CheckInRecord;
import com.hotel.entity.Room;
import com.hotel.entity.AdditionalCharge;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CheckInService {
    // 办理入住
    CheckInRecord checkIn(CheckInRecord checkInRecord);
    
    // 获取入住详情
    CheckInRecord getCheckInRecordById(Long id);
    
    // 获取入住记录列表
    Page<CheckInRecord> getCheckInRecords(
            CheckInRecord.CheckInStatus status,
            String roomNumber,
            String guestName,
            String guestMobile,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size);
    
    // 生成入住单号
    String generateCheckInNumber();
    
    // 查找可用房间
    Room findAvailableRoom(Long roomId);
    
    // 获取入住关联的预订信息
    Booking getBookingById(Long bookingId);
    
    // 获取入住关联的房间信息
    Room getRoomById(Long roomId);
    
    // 办理退房
    CheckInRecord checkout(Long checkInId, List<AdditionalCharge> additionalCharges, 
        BigDecimal returnDeposit, String remarks, CheckInRecord.PaymentMethod paymentMethod);

    // 获取今日入住/退房统计
    Map<String, Object> getTodayCheckInAndCheckoutStats();

    // 添加额外消费记录
    AdditionalCharge addAdditionalCharge(Long checkInId, AdditionalCharge additionalCharge);

    // 延长入住时间
    CheckInRecord extendStay(Long checkInId, LocalDate newCheckOutDate, String remarks);
} 