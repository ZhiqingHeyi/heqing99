package com.hotel.service.impl;

import com.hotel.entity.CheckInRecord;
import com.hotel.entity.Reservation;
import com.hotel.entity.Room;
import com.hotel.entity.User;
import com.hotel.entity.RoomType;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.CheckInRecordRepository;
import com.hotel.repository.RoomTypeRepository;
import com.hotel.service.ReservationService;
import com.hotel.service.RoomService;
import com.hotel.service.UserService;
import com.hotel.service.ConsumptionRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.Objects;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.Arrays;
import com.hotel.dto.ReservationSummaryDTO;
import com.hotel.entity.Reservation.ReservationStatus;

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private CheckInRecordRepository checkInRecordRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserService userService;

    @Autowired
    private ConsumptionRecordService consumptionRecordService;

    @Override
    public Reservation createReservation(Map<String, Object> reservationData) throws RuntimeException {
        // 1. Parse reservation data from Map
        Long userId = Long.parseLong(reservationData.get("userId").toString());
        Long roomTypeId = Long.parseLong(reservationData.get("roomType").toString());
        String checkInDateStr = reservationData.get("checkIn").toString();
        String checkOutDateStr = reservationData.get("checkOut").toString();
        Integer roomCount = Integer.parseInt(reservationData.get("roomCount").toString());
        String contactName = reservationData.get("contactName").toString();
        String phone = reservationData.get("phone").toString();
        String remarks = reservationData.containsKey("remarks") ? reservationData.get("remarks").toString() : "";
        Double totalAmount = Double.parseDouble(reservationData.get("totalAmount").toString());
        
        // Parse dates (reuse logic from controller if complex, simplify if possible)
        LocalDateTime checkInTime;
        LocalDateTime checkOutTime;
        try {
            // Assume ISO format first
            checkInTime = LocalDateTime.parse(checkInDateStr);
            checkOutTime = LocalDateTime.parse(checkOutDateStr);
        } catch (DateTimeParseException e) {
            try {
                // Fallback to ZonedDateTime format
                 DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME; // More robust standard format
                 checkInTime = LocalDateTime.parse(checkInDateStr, formatter);
                 checkOutTime = LocalDateTime.parse(checkOutDateStr, formatter);
            } catch (DateTimeParseException e2) {
                 System.err.println("日期解析失败: " + checkInDateStr + ", " + checkOutDateStr + ", Error: " + e2.getMessage());
                 throw new RuntimeException("日期格式无效，请使用ISO日期时间格式 (e.g., YYYY-MM-DDTHH:mm:ssZ)");
            }
        }

        // 2. Get User and RoomType
        User user = userService.getUserById(userId); // Assumes userService throws exception if not found
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new RuntimeException("指定的房间类型不存在"));

        // 3. Find an available room of the specified type for the given time range
        // Define statuses considered available for booking
        List<Room.RoomStatus> availableStatuses = Arrays.asList(Room.RoomStatus.AVAILABLE, Room.RoomStatus.CLEANING);
        // Fetch rooms that match the type AND are in an available status
        List<Room> roomsOfType = roomRepository.findByRoomTypeAndStatusIn(roomType, availableStatuses);
        if (roomsOfType.isEmpty()) {
            throw new RuntimeException("系统中没有该类型或该类型当前无可用状态（AVAILABLE/CLEANING）的房间"); // Updated message for clarity
        }

        Room availableRoom = null;
        List<Reservation.ReservationStatus> conflictingStatuses = Arrays.asList(
                Reservation.ReservationStatus.PENDING,
                Reservation.ReservationStatus.CONFIRMED,
                Reservation.ReservationStatus.CHECKED_IN
        );

        for (Room room : roomsOfType) {
            List<Reservation> conflicts = reservationRepository.findConflictingReservationsForRoom(
                    room.getId(),
                    checkInTime,
                    checkOutTime,
                    conflictingStatuses
            );
            if (conflicts.isEmpty()) {
                availableRoom = room; // Found an available room
                System.out.println("为预订找到了可用房间: Room ID " + availableRoom.getId() + ", Number " + availableRoom.getRoomNumber());
                break;
            }
        }

        // 4. Check if a room was found
        if (availableRoom == null) {
             System.out.println("房型ID " + roomTypeId + " 在时间段 " + checkInTime + " - " + checkOutTime + " 内已预订满 (或无合适状态的房间)"); // Updated log
            throw new RuntimeException("该房型在指定时间段内已预订满"); // Keep existing user-facing message
        }
        
        // 5. Apply discount and create Reservation entity
        BigDecimal discount = userService.getDiscountByUserId(userId);
        BigDecimal originalAmount = BigDecimal.valueOf(totalAmount);
        BigDecimal finalAmount = originalAmount.multiply(discount).setScale(2, BigDecimal.ROUND_HALF_UP); // Scale for currency

        // --- Determine Payment Status --- Start
        String determinedPaymentStatusString = "UNPAID"; // Default as String
        if (reservationData.containsKey("paymentStatus")) {
            Object paymentStatusObj = reservationData.get("paymentStatus");
            if (paymentStatusObj != null) {
                String paymentStatusStr = paymentStatusObj.toString().toUpperCase();
                // Basic check if the string matches known values (optional, but good practice)
                if (Arrays.asList("UNPAID", "DEPOSIT_PAID", "PAID_FULL").contains(paymentStatusStr)) {
                     determinedPaymentStatusString = paymentStatusStr;
                     log.info("Using paymentStatus from request: {}", determinedPaymentStatusString);
                } else {
                    log.warn("Invalid paymentStatus string received: '{}'. Using default UNPAID.", paymentStatusObj);
                    // Keep default UNPAID string
                }
            } else {
                log.warn("Received null for paymentStatus. Using default UNPAID.");
            }
        } else {
            log.info("No paymentStatus provided in request. Using default UNPAID.");
        }
        // --- Determine Payment Status --- End

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setRoom(availableRoom); // Set the found available room
        reservation.setCheckInTime(checkInTime);
        reservation.setCheckOutTime(checkOutTime);
        reservation.setGuestName(contactName);
        reservation.setGuestPhone(phone);
        reservation.setSpecialRequests(remarks);
        reservation.setTotalPrice(finalAmount);
        reservation.setRoomCount(roomCount);
        reservation.setStatus(Reservation.ReservationStatus.PENDING);
        reservation.setPaymentStatus(determinedPaymentStatusString); // Set String
        reservation.setCreateTime(LocalDateTime.now());

        // 6. Save reservation
        Reservation savedReservation = reservationRepository.save(reservation);

        System.out.println("预订创建成功，Reservation ID: " + savedReservation.getId() + ", Room ID: " + (savedReservation.getRoom() != null ? savedReservation.getRoom().getId() : "N/A"));

        try {
            User userForPoints = savedReservation.getUser(); // 从保存后的预订获取用户
            BigDecimal amountForPoints = savedReservation.getTotalPrice(); 
            String type = "ROOM_RESERVATION"; // 类型改为预订相关
            String description = "Reservation created: ID " + savedReservation.getId();
            Long resId = savedReservation.getId();
            Long roomId = (savedReservation.getRoom() != null) ? savedReservation.getRoom().getId() : null;

            if (userForPoints != null && amountForPoints != null && amountForPoints.compareTo(BigDecimal.ZERO) > 0) {
                log.info("[POINTS_ON_CREATE] Triggering consumption record for Reservation ID: {}", resId); // 添加日志区分
                consumptionRecordService.recordConsumptionAndUpdateMembership(
                    userForPoints.getId(), 
                    amountForPoints, 
                    type, 
                    description,
                    resId,
                    roomId
                );
                 log.info("[POINTS_ON_CREATE] Consumption record processing finished for Reservation ID: {}", resId);
            } else {
                log.warn("[POINTS_ON_CREATE] Skipping consumption record for reservation ID: {} due to missing user or zero/null amount.", resId);
            }
        } catch (Exception e) {
            log.error("[POINTS_ON_CREATE] Error processing consumption record after reservation creation for ID: " + savedReservation.getId(), e);
        }

        return savedReservation;
    }

    @Override
    public Reservation updateReservation(Reservation reservation) {
        Reservation existingReservation = getReservationById(reservation.getId());

        // 检查是否只更新了状态
        boolean onlyStatusChanged = existingReservation.getCheckInTime().equals(reservation.getCheckInTime()) 
                && existingReservation.getCheckOutTime().equals(reservation.getCheckOutTime())
                && existingReservation.getRoom().equals(reservation.getRoom())
                && existingReservation.getGuestName().equals(reservation.getGuestName())
                && existingReservation.getGuestPhone().equals(reservation.getGuestPhone())
                && existingReservation.getRoomCount().equals(reservation.getRoomCount())
                && existingReservation.getTotalPrice().equals(reservation.getTotalPrice())
                && existingReservation.getStatus() != reservation.getStatus();

        // 如果只是状态更新，直接保存
        if (onlyStatusChanged) {
            // 记录状态变更
            System.out.println("预订状态从 " + existingReservation.getStatus() + " 变更为 " + reservation.getStatus());
            return reservationRepository.save(reservation);
        } 
        // 如果是其他信息更新，需要检查是否允许
        else {
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
        // 实际的退房时间，如果需要精确记录
        // reservation.setActualCheckOutTime(LocalDateTime.now()); // 如果有此字段则取消注释
        Reservation savedReservation = reservationRepository.save(reservation);

        return savedReservation; // 返回更新后的预订信息
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

    @Override
    public boolean hasRoomReservations(Long roomId) {
        Optional<Room> roomOpt = roomRepository.findById(roomId);
        if (!roomOpt.isPresent()) {
            return false;
        }
        Room room = roomOpt.get();
        List<Reservation> reservations = reservationRepository.findByRoom(room);
        return !reservations.isEmpty();
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
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        return reservationRepository.countByCheckInTimeBetween(startOfDay, endOfDay);
    }

    @Override
    public int countUpcomingReservations() {
        LocalDateTime now = LocalDateTime.now();
        return reservationRepository.countByCheckInTimeAfter(now);
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
        checkInRecord.setActualCheckInTime(LocalDateTime.now());
        
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
    public Page<ReservationSummaryDTO> getAllReservations(Pageable pageable, String statusStr, String guestName, String guestPhone, LocalDateTime startDate, LocalDateTime endDate, String bookingNo) {
        ReservationStatus statusEnum = null;
        if (StringUtils.hasText(statusStr)) {
            try {
                statusEnum = ReservationStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                 log.warn("Invalid reservation status string provided for getAllReservations: {}", statusStr);
            }
        }

        Page<Reservation> reservationPage = reservationRepository.findAllWithFilters(
            pageable,
            statusEnum // Pass the enum here
        );

        log.debug("Repository returned Page: PageNumber={}, Size={}, TotalElements={}, TotalPages={}",
                  reservationPage.getNumber(),      // 0-based index
                  reservationPage.getSize(),        // Size of the current page
                  reservationPage.getTotalElements(), // Total elements across all pages
                  reservationPage.getTotalPages());   // Total number of pages

        return reservationPage.map(this::convertToSummaryDTO);
    }

    private ReservationSummaryDTO convertToSummaryDTO(Reservation reservation) {
        ReservationSummaryDTO dto = new ReservationSummaryDTO();
        dto.setId(reservation.getId());
        // 简单的将ID转为字符串作为预订号，或使用更复杂的生成逻辑
        dto.setBookingNo(String.valueOf(reservation.getId())); 
        dto.setGuestName(reservation.getGuestName());
        dto.setGuestPhone(reservation.getGuestPhone());
        
        // 安全地获取房间和房型信息
        if (reservation.getRoom() != null) {
            dto.setRoomNumber(reservation.getRoom().getRoomNumber());
            if (reservation.getRoom().getRoomType() != null) {
                dto.setRoomTypeName(reservation.getRoom().getRoomType().getName());
                dto.setRoomTypeId(reservation.getRoom().getRoomType().getId()); // 添加 roomTypeId
                // 尝试获取房间价格，如果房间或房型价格为空，则使用默认值或标记
                BigDecimal price = reservation.getRoom().getRoomType().getBasePrice();
                dto.setRoomPrice(price != null ? price : BigDecimal.ZERO); // 如果价格为空，则设置为0
            } else {
                dto.setRoomTypeName("未知房型");
                dto.setRoomTypeId(null); // 房型未知时 roomTypeId 为 null
                dto.setRoomPrice(BigDecimal.ZERO); // 房型未知时价格为0
            }
        } else {
            dto.setRoomNumber("未分配");
            dto.setRoomTypeName("未知房型");
            dto.setRoomTypeId(null);
            dto.setRoomPrice(BigDecimal.ZERO);
        }
        
        // 设置时间、状态和支付状态
        dto.setCheckInTime(reservation.getCheckInTime());
        dto.setCheckOutTime(reservation.getCheckOutTime());
        dto.setStatus(reservation.getStatus() != null ? reservation.getStatus().name() : null);
        dto.setPaymentStatus(reservation.getPaymentStatus()); // 直接使用 String 类型的支付状态
        
        return dto;
    }

    @Override
    public Reservation completeReservation(Long reservationId) {
        Reservation reservation = getReservationById(reservationId);
        
        // 验证状态是否允许完成（待确认或已确认状态）
        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING &&
            reservation.getStatus() != Reservation.ReservationStatus.CONFIRMED) {
            throw new RuntimeException("当前状态不允许完成预订");
        }
        
        // 更新状态
        reservation.setStatus(Reservation.ReservationStatus.COMPLETED);
        reservation.setUpdateTime(LocalDateTime.now());
        
        return reservationRepository.save(reservation);
    }

    @Override
    public Map<String, Object> getReservationStatistics(String period) {
        LocalDateTime startDate;
        LocalDateTime endDate = LocalDateTime.now();
        
        // 确定统计周期
        if ("quarter".equals(period)) {
            startDate = endDate.minusMonths(3);
        } else if ("year".equals(period)) {
            startDate = endDate.minusYears(1);
        } else {
            // 默认为月
            startDate = endDate.minusMonths(1);
        }
        
        // 统计各状态预订数
        long totalPending = reservationRepository.countByStatus(Reservation.ReservationStatus.PENDING);
        long totalConfirmed = reservationRepository.countByStatus(Reservation.ReservationStatus.CONFIRMED);
        long totalCanceled = reservationRepository.countByStatus(Reservation.ReservationStatus.CANCELLED);
        long totalCompleted = reservationRepository.countByStatus(Reservation.ReservationStatus.COMPLETED);
        long totalCheckedIn = reservationRepository.countByStatus(Reservation.ReservationStatus.CHECKED_IN);
        
        long totalReservations = totalPending + totalConfirmed + totalCanceled + totalCompleted + totalCheckedIn;
        
        // 构建结果
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalReservations", totalReservations);
        statistics.put("totalConfirmed", totalConfirmed);
        statistics.put("totalCanceled", totalCanceled);
        statistics.put("totalCompleted", totalCompleted);
        
        // 按状态统计
        List<Map<String, Object>> byStatus = new ArrayList<>();
        byStatus.add(createStatusCount("待确认", totalPending));
        byStatus.add(createStatusCount("已确认", totalConfirmed));
        byStatus.add(createStatusCount("已取消", totalCanceled));
        byStatus.add(createStatusCount("已完成", totalCompleted));
        byStatus.add(createStatusCount("已入住", totalCheckedIn));
        statistics.put("byStatus", byStatus);
        
        // 按房间类型统计
        statistics.put("byRoomType", getRoomTypeStatistics());
        
        // 按日期统计
        statistics.put("byDate", getDateStatistics(startDate, endDate, period));
        
        return statistics;
    }

    private Map<String, Object> createStatusCount(String status, long count) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", status);
        result.put("count", count);
        return result;
    }

    private List<Map<String, Object>> getRoomTypeStatistics() {
        // 获取所有预订
        List<Reservation> allReservations = reservationRepository.findAll();
        
        // 按房间类型分组统计
        Map<String, Long> roomTypeCounts = allReservations.stream()
            .collect(Collectors.groupingBy(
                r -> r.getRoom().getRoomType().getName(),
                Collectors.counting()
            ));
        
        // 转换为所需格式
        return roomTypeCounts.entrySet().stream()
            .map(entry -> {
                Map<String, Object> item = new HashMap<>();
                item.put("roomType", entry.getKey());
                item.put("count", entry.getValue());
                return item;
            })
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getDateStatistics(LocalDateTime startDate, LocalDateTime endDate, String period) {
        // 获取日期范围内的所有预订
        List<Reservation> reservations = reservationRepository.findByCreateTimeBetween(startDate, endDate);
        
        // 按日期格式化并分组
        DateTimeFormatter formatter;
        if ("year".equals(period)) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        } else {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        }
        
        Map<String, Long> dateCounts = reservations.stream()
            .collect(Collectors.groupingBy(
                r -> r.getCreateTime().format(formatter),
                Collectors.counting()
            ));
        
        // 转换为所需格式
        return dateCounts.entrySet().stream()
            .map(entry -> {
                Map<String, Object> item = new HashMap<>();
                item.put("date", entry.getKey());
                item.put("count", entry.getValue());
                return item;
            })
            .sorted((a, b) -> ((String)a.get("date")).compareTo((String)b.get("date")))
            .collect(Collectors.toList());
    }

    @Override
    public Page<Reservation> getUserReservationsPaginated(Long userId, String statusStr, Pageable pageable) {
        User user = userService.getUserById(userId);
        Page<Reservation> resultPage;

        // --- TEMPORARY MODIFICATION: Ignore status filter ---
        log.warn("getUserReservationsPaginated TEMPORARILY ignoring status filter '{}' and calling findByUser only.", statusStr);
        resultPage = reservationRepository.findByUser(user, pageable);
        // --- END TEMPORARY MODIFICATION ---

        return resultPage;
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
}