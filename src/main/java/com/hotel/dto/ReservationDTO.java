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
    private String roomTypeName; // 房间类型名称
    private Long roomId;         // 房间ID
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private BigDecimal totalPrice;
    private String status;       // 状态 (String)
    private LocalDateTime createTime;
    private String specialRequests; // 特殊要求
    private Integer roomCount;      // 房间数量
    private String contactName;   // 联系人姓名 (映射自 guestName)
    private String contactPhone;  // 联系人电话 (映射自 guestPhone)

    /**
     * Constructor to convert from Reservation entity.
     * Handles potential null values during conversion.
     */
    public ReservationDTO(Reservation reservation) {
        this.id = reservation.getId();
        this.checkInTime = reservation.getCheckInTime();
        this.checkOutTime = reservation.getCheckOutTime();
        this.totalPrice = reservation.getTotalPrice();
        this.status = reservation.getStatus() != null ? reservation.getStatus().name() : null;
        this.createTime = reservation.getCreateTime();
        this.specialRequests = reservation.getSpecialRequests();
        this.roomCount = reservation.getRoomCount();
        this.contactName = reservation.getGuestName();
        this.contactPhone = reservation.getGuestPhone();

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