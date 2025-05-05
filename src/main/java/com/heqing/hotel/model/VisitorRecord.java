package com.heqing.hotel.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "visitor_records")
public class VisitorRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String visitorName;

    @Column(nullable = false)
    private String visitorPhone;

    @Column(nullable = false)
    private String idCard;

    @Column(nullable = false)
    private String visitPurpose;

    @Column(nullable = false)
    private String roomNumber;

    @Column(nullable = false)
    private String guestName;

    @Column(nullable = false)
    private LocalDateTime visitTime;

    @Column
    private LocalDateTime leaveTime;

    @Column(nullable = false)
    private String status; // PENDING, APPROVED, CANCELLED, COMPLETED

    @Column
    private String remarks;

    @Column(nullable = false)
    private LocalDateTime createTime;

    @Column
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}