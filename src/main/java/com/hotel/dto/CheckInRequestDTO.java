package com.hotel.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CheckInRequestDTO {
    private Long bookingId;
    private Long roomId;
    private String guestName;
    private String guestIdType;
    private String guestIdNumber;
    private String guestMobile;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer guestCount;
    private BigDecimal deposit;
    private String paymentMethod;
    private String specialRequests;
    private String remarks;
} 