package com.hotel.dto;

import com.hotel.entity.Reservation;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ReservationDTO {

    private Long id;
    private String roomTypeName; // Added field
    private Long roomId;         // Added field
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private BigDecimal totalPrice;
    private String status;       // Changed to String
    private LocalDateTime createTime;
    // Removed fields like user, room, guestName, guestPhone, roomCount etc. if not needed directly in list view

    /**
     * Constructor to convert from Reservation entity.
     * Handles potential null values during conversion.
     */
    public ReservationDTO(Reservation reservation) {
        this.id = reservation.getId();
        this.checkInTime = reservation.getCheckInTime();
        this.checkOutTime = reservation.getCheckOutTime();
        this.totalPrice = reservation.getTotalPrice();
        this.status = reservation.getStatus() != null ? reservation.getStatus().name().toLowerCase() : null;
        this.createTime = reservation.getCreateTime();

        if (reservation.getRoom() != null) {
            this.roomId = reservation.getRoom().getId();
            if (reservation.getRoom().getRoomType() != null) {
                this.roomTypeName = reservation.getRoom().getRoomType().getName();
            } else {
                 this.roomTypeName = "未知房型"; // Fallback
            }
        } else {
            this.roomId = null;
            this.roomTypeName = "未知房型"; // Fallback
        }
    }

     /**
     * Static factory method for conversion (alternative)
     */
    public static ReservationDTO fromEntity(Reservation reservation) {
        if (reservation == null) {
            return null;
        }
        return new ReservationDTO(reservation);
    }
} 