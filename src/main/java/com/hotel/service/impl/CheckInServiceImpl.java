package com.hotel.service.impl;

import com.hotel.entity.AdditionalCharge;
import com.hotel.entity.Booking;
import com.hotel.entity.CheckInRecord;
import com.hotel.entity.Room;
import com.hotel.entity.Reservation;
import com.hotel.exception.ResourceNotFoundException;
import com.hotel.repository.AdditionalChargeRepository;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.CheckInRecordRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.service.CheckInService;
import com.hotel.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

@Service
@Transactional
public class CheckInServiceImpl implements CheckInService {
    
    private static final Logger log = LoggerFactory.getLogger(CheckInServiceImpl.class);
    
    @Autowired
    private CheckInRecordRepository checkInRecordRepository;
    
    @Autowired
    private AdditionalChargeRepository additionalChargeRepository;
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private RoomService roomService;
    
    @Override
    public CheckInRecord checkIn(CheckInRecord checkInRecord) {
        // --- 修正：获取 bookingId 并检查 ---
        Long bookingId = checkInRecord.getBookingId();
        if (bookingId == null) {
            throw new IllegalArgumentException("预订ID (bookingId) 不能为空");
        }
        // --- 修正结束 ---

        // 检查必需的输入字段 (roomId, guestName, guestMobile, deposit)
        if (checkInRecord.getRoomId() == null) { throw new IllegalArgumentException("房间ID不能为空"); }
        if (checkInRecord.getGuestName() == null || checkInRecord.getGuestName().isEmpty()) { throw new IllegalArgumentException("客人姓名不能为空"); }
        if (checkInRecord.getGuestMobile() == null || checkInRecord.getGuestMobile().isEmpty()) { throw new IllegalArgumentException("客人手机号不能为空"); }
        if (checkInRecord.getDeposit() == null) { throw new IllegalArgumentException("押金不能为空"); }
        
        // 解析并设置离店日期 (使用 Transient 字段)
        if (checkInRecord.getExpectedCheckOutTime() == null || checkInRecord.getExpectedCheckOutTime().isEmpty()) {
            throw new IllegalArgumentException("预计离店日期不能为空 (expectedCheckOutTime)");
        }
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(checkInRecord.getExpectedCheckOutTime(), DateTimeFormatter.ISO_DATE_TIME);
            checkInRecord.setCheckOutDate(zdt.toLocalDate());
        } catch (DateTimeParseException e) {
            log.error("解析离店日期失败 (expectedCheckOutTime: {}): {}", checkInRecord.getExpectedCheckOutTime(), e.getMessage());
            throw new IllegalArgumentException("无效的预计离店日期格式，需要 ISO 8601 格式 (例如: 2025-05-15T08:00:00.000Z)", e);
        }

        // --- 修正：获取关联的 Reservation 对象 --- 
        Reservation reservation = reservationRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到 ID 为 " + bookingId + " 的预订记录"));
        // --- 修正结束 ---
        
        // 检查预订状态是否允许入住 (例如，必须是 CONFIRMED)
        if (reservation.getStatus() != Reservation.ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("预订状态为 " + reservation.getStatus() + "，无法办理入住");
        }

        // 生成入住单号
        checkInRecord.setCheckInNumber(generateCheckInNumber());
        
        // 设置实际入住时间
        LocalDateTime actualCheckIn = LocalDateTime.now();
        checkInRecord.setActualCheckInTime(actualCheckIn);
        checkInRecord.setCheckInDate(actualCheckIn.toLocalDate());
        
        // 设置状态为已入住
        checkInRecord.setStatus(CheckInRecord.CheckInStatus.CHECKED_IN);
        
        // 从关联房间获取信息 (如果需要， roomId 已经从前端传入 checkInRecord)
        Room room = roomRepository.findById(checkInRecord.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("未找到 ID 为 " + checkInRecord.getRoomId() + " 的房间"));
        checkInRecord.setRoomNumber(room.getRoomNumber()); // 确保 roomNumber 更新
        checkInRecord.setRoomType(room.getRoomType() != null ? room.getRoomType().getName() : "未知房型");
        
        // --- 修正：使用 Reservation 对象填充信息 ---
        checkInRecord.setGuestIdType("ID_CARD"); // 保持默认值
        checkInRecord.setGuestIdNumber("UNKNOWN"); // 保持默认值
        checkInRecord.setGuestCount(1); // 设置默认入住人数为 1，或根据业务需要从 Reservation 获取
        checkInRecord.setTotalAmount(reservation.getTotalPrice() != null ? reservation.getTotalPrice() : calculateDefaultTotalAmount(room, checkInRecord)); // 从 reservation 获取总价
        checkInRecord.setSpecialRequests(reservation.getSpecialRequests()); // 从 reservation 获取特殊要求
        // --- 修正结束 ---
        
        // --- 添加：设置默认支付方式 --- 
        if (checkInRecord.getPaymentMethod() == null) {
            checkInRecord.setPaymentMethod(CheckInRecord.PaymentMethod.CASH); // 设置默认值为现金
            log.info("Payment method not provided, defaulting to CASH for CheckInRecord with bookingId: {}", bookingId);
        }
        // --- 添加结束 --- 
        
        // 设置操作员信息 (从安全上下文中获取)
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            checkInRecord.setOperatorName(userDetails.getUsername());
            // 假设 UserDetails 实现包含 getId() 方法
            // 如果是自定义的UserDetails，需要确保它有获取ID的方法
            // 暂时注释掉，如果 UserDetails 实现没有 getId() 会报错
            // if (userDetails instanceof CustomUserDetails) { 
            //     checkInRecord.setOperatorId(((CustomUserDetails) userDetails).getId());
            // } else {
            //     log.warn("无法获取操作员ID，UserDetails 类型不匹配: {}", principal.getClass());
            // }
        } else if (principal != null) {
            checkInRecord.setOperatorName(principal.toString());
            log.warn("未知的 Principal 类型: {}", principal.getClass());
        } else {
            log.warn("无法获取操作员信息，Principal 为空");
        }
        
        // 更新房间状态为 OCCUPIED
        room.setStatus(Room.RoomStatus.OCCUPIED);
        roomRepository.save(room);
        
        // 更新预订状态为 COMPLETED
        reservation.setStatus(Reservation.ReservationStatus.COMPLETED);
        reservationRepository.save(reservation);
        
        // 保存入住记录 (此时 checkInRecord 已包含 bookingId)
        CheckInRecord savedRecord = checkInRecordRepository.save(checkInRecord);
        
        log.info("入住登记成功: ID={}, 入住单号={}, 预订ID={}", savedRecord.getId(), savedRecord.getCheckInNumber(), savedRecord.getBookingId());
        return savedRecord;
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
        Reservation reservation = reservationRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("预订", "ID", bookingId));
        // 这里需要返回 Booking 类型，但我们只有 Reservation。这是一个类型不匹配问题。
        // 需要决定是修改接口返回 Reservation，还是进行转换。
        // 暂时返回 null 或抛异常，表示此方法不再适用或需要重构。
        // throw new UnsupportedOperationException("getBookingById is deprecated, use getReservationById from ReservationService");
         // 或者尝试转换，但这很奇怪
         // Booking booking = new Booking(); // 转换逻辑... 不推荐
         return null; // 暂时返回 null
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
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate today = LocalDate.now();
        
        return checkInRecordRepository.sumTotalAmountByCheckOutDateBetween(firstDayOfMonth, today);
    }

    @Override
    public List<CheckInRecord> getCheckoutRecords(
            String roomNumber,
            String guestName,
            LocalDate checkoutDate,
            int page,
            int pageSize) {

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "actualCheckOutTime"));

        LocalDate startDate = null;
        LocalDate endDate = null;

        // 如果前端指定了 checkoutDate，则进行精确日期匹配
        if (checkoutDate != null) {
            // 注意：Repository 的 findCheckoutRecords 可能需要调整以支持精确日期匹配
            // 或者，如果 findCheckoutRecords 已经改为范围查询，这里设置 startDate 和 endDate 为同一天
            startDate = checkoutDate;
            endDate = checkoutDate;
            log.info("Fetching checkout records for specific date: {}", checkoutDate);
        } else {
            // 如果前端没有指定日期，则默认查询本月记录
            LocalDate today = LocalDate.now();
            startDate = today.withDayOfMonth(1);
            endDate = today; // 查询到今天
            // endDate = today.withDayOfMonth(today.lengthOfMonth()); // 或者查询到月底
            log.info("Fetching checkout records for current month: {} to {}", startDate, endDate);
        }

        // --- 调用支持日期范围的 Repository 方法 (假设存在或需要创建) ---
        // 需要确保 Repository 有一个接受日期范围的方法，例如 findCheckoutRecordsBetweenDates
        // 这里暂时还用旧方法名，但逻辑上期望它能处理范围
        // Page<CheckInRecord> results = checkInRecordRepository.findCheckoutRecords( 
        //         roomNumber, guestName, checkoutDate, pageable); // 旧调用

        // 假设新的或修改后的方法签名接受 LocalDateTime 范围
        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        // 对于结束日期，通常包含当天，所以结束时间设为第二天的开始
        LocalDateTime endDateTime = (endDate != null) ? endDate.plusDays(1).atStartOfDay() : null;

        Page<CheckInRecord> results = checkInRecordRepository.findCheckoutRecordsByFilterAndDateRange(
            roomNumber, 
            guestName, 
            startDateTime, // 使用范围开始
            endDateTime,   // 使用范围结束
            pageable
        );
        // --- 调用结束 ---

        return results.getContent();
    }

    @Override
    public long countCheckoutRecords(String roomNumber, String guestName, LocalDate checkoutDate) {
        // 同样，需要根据 checkoutDate 是否为 null 来决定是精确计数还是范围计数
        LocalDate startDate = null;
        LocalDate endDate = null;
        if (checkoutDate != null) {
            startDate = checkoutDate;
            endDate = checkoutDate;
        } else {
            LocalDate today = LocalDate.now();
            startDate = today.withDayOfMonth(1);
            endDate = today;
        }
        
        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.plusDays(1).atStartOfDay() : null;

        // --- 调用支持日期范围的 Repository 计数方法 (假设存在或需要创建) ---
        // return checkInRecordRepository.countCheckoutRecords(roomNumber, guestName, checkoutDate); // 旧调用
        return checkInRecordRepository.countCheckoutRecordsByFilterAndDateRange(
            roomNumber, guestName, startDateTime, endDateTime);
        // --- 调用结束 ---
    }

    @Override
    public long countCheckoutsByDate(LocalDate checkoutDate) {
        // 建议修改：如果业务逻辑统一按 actual_check_out_time 判断，这里也应该调整
        // 但暂时保持不变，因为它可能用于其他精确日期的统计
        return checkInRecordRepository.countByStatusAndCheckOutDate(
                CheckInRecord.CheckInStatus.CHECKED_OUT, checkoutDate);
    }

    @Override
    public long countCheckoutsBetweenDates(LocalDate startDate, LocalDate endDate) {
        // --- 修改：统一使用 actual_check_out_time 进行范围统计 ---
        // return checkInRecordRepository.countByStatusAndCheckOutDateBetween(
        //         CheckInRecord.CheckInStatus.CHECKED_OUT, startDate, endDate); // 旧逻辑
        
        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.plusDays(1).atStartOfDay() : null;
        
        // 调用新的基于 actual_check_out_time 的计数方法
        return checkInRecordRepository.countCheckoutRecordsByFilterAndDateRange(
            null, // roomNumber: null 表示不按房间号过滤
            null, // guestName: null 表示不按客人姓名过滤
            startDateTime, 
            endDateTime);
        // --- 修改结束 ---
    }

    @Override
    public CheckInRecord getCurrentCheckInByRoomNumber(String roomNumber) {
        Room room = roomRepository.findByRoomNumber(roomNumber).orElse(null);
        
        if (room == null) {
            throw new ResourceNotFoundException("房间号为 " + roomNumber + " 的房间不存在");
        }
        
        if (room.getStatus() != Room.RoomStatus.OCCUPIED) {
            return null; // 房间未被入住
        }
        
        // 3. 如果房间是 OCCUPIED，根据房间 ID 和 CheckInStatus.CHECKED_IN 查找 CheckInRecord
        List<CheckInRecord> records = checkInRecordRepository.findByRoomIdAndStatus(
                room.getId(), CheckInRecord.CheckInStatus.CHECKED_IN);
        
        // --- 调试日志：打印找到的记录列表大小 ---
        log.info("findByRoomIdAndStatus found {} records for roomId {} and status CHECKED_IN", records.size(), room.getId());
        // --- 调试日志结束 ---

        return records.stream()
                .findFirst() // 取列表中的第一个（理论上一个 OCCUPIED 房间只有一个 CHECKED_IN 记录）
                .orElse(null); // 如果找不到 CHECKED_IN 记录，返回 null
    }

    // 新增一个计算默认总价的方法 (简化)
    private BigDecimal calculateDefaultTotalAmount(Room room, CheckInRecord record) {
        if (room == null || room.getRoomType() == null || room.getRoomType().getBasePrice() == null || record.getCheckInDate() == null || record.getCheckOutDate() == null) {
             log.warn("无法计算默认总价，缺少信息: Room={}, RoomType={}, BasePrice={}, CheckIn={}, CheckOut={}", 
                 room != null ? room.getId() : "null", 
                 (room != null && room.getRoomType() != null) ? room.getRoomType().getId() : "null",
                 (room != null && room.getRoomType() != null) ? room.getRoomType().getBasePrice() : "null",
                 record.getCheckInDate(), record.getCheckOutDate());
            return BigDecimal.ZERO; 
        }
        long nights = ChronoUnit.DAYS.between(record.getCheckInDate(), record.getCheckOutDate());
        if (nights <= 0) nights = 1; 
        return room.getRoomType().getBasePrice().multiply(BigDecimal.valueOf(nights));
    }
} 