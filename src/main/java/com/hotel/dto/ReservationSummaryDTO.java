package com.hotel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSummaryDTO {
    private Long id;
    private String bookingNo; // Typically derived from id
    private String guestName;
    private String guestPhone;
    private String roomNumber;
    private String roomTypeName;
    private BigDecimal roomPrice;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String status;
    private String paymentStatus;
} 