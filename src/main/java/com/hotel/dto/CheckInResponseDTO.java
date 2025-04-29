package com.hotel.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CheckInResponseDTO {
    private Long id;
    private String checkInNumber;
    private Long bookingId;
    private Long roomId;
    private String roomNumber;
    private String roomType;
    private String guestName;
    private String guestIdType;
    private String guestIdNumber;
    private String guestMobile;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDateTime actualCheckInTime;
    private LocalDateTime actualCheckOutTime;
    private Integer guestCount;
    private BigDecimal deposit;
    private String paymentMethod;
    private String status;
    private BigDecimal totalAmount;
    private String operatorName;
    private LocalDateTime createTime;
} 