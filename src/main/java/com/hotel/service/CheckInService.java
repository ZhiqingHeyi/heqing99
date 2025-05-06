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
    
    // 更新入住记录
    CheckInRecord updateCheckInRecord(CheckInRecord checkInRecord);
    
    // 获取当前所有入住的客人记录
    List<CheckInRecord> getCurrentlyCheckedInRecords();
    
    /**
     * 计算今日收入
     * @return 今日收入总额
     */
    BigDecimal calculateTodayRevenue();
    
    /**
     * 计算当月收入
     * @return 当月收入总额
     */
    BigDecimal calculateMonthlyRevenue();

    /**
     * 获取退房记录列表
     * @param roomNumber 房间号
     * @param guestName 客人姓名
     * @param checkoutDate 退房日期
     * @param page 页码
     * @param pageSize 每页大小
     * @return 退房记录列表
     */
    List<CheckInRecord> getCheckoutRecords(
            String roomNumber,
            String guestName,
            LocalDate checkoutDate,
            int page,
            int pageSize);
    
    /**
     * 计算退房记录总数
     * @param roomNumber 房间号
     * @param guestName 客人姓名
     * @param checkoutDate 退房日期
     * @return 记录总数
     */
    long countCheckoutRecords(String roomNumber, String guestName, LocalDate checkoutDate);
    
    /**
     * 根据退房日期统计退房数量
     * @param checkoutDate 退房日期
     * @return 退房数量
     */
    long countCheckoutsByDate(LocalDate checkoutDate);
    
    /**
     * 统计日期范围内的退房数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 退房数量
     */
    long countCheckoutsBetweenDates(LocalDate startDate, LocalDate endDate);
    
    /**
     * 根据房间号获取当前的入住记录
     * @param roomNumber 房间号
     * @return 入住记录
     */
    CheckInRecord getCurrentCheckInByRoomNumber(String roomNumber);
} 