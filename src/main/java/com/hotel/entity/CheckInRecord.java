package com.hotel.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "check_in_records")
@EntityListeners(AuditingEntityListener.class)
public class CheckInRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "actual_check_in", nullable = false)
    private LocalDateTime actualCheckIn;

    @Column(name = "actual_check_out")
    private LocalDateTime actualCheckOut;

    @Column(name = "deposit_amount")
    private BigDecimal depositAmount;

    @Column(name = "deposit_status")
    @Enumerated(EnumType.STRING)
    private DepositStatus depositStatus;

    private String notes;

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    public enum DepositStatus {
        PAID,      // 已支付
        REFUNDED   // 已退还
    }
}