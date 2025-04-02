package com.hotel.entity;

import com.hotel.entity.RoomType;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "rooms")
@EntityListeners(AuditingEntityListener.class)
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roomNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(nullable = false)
    private String floor;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoomStatus status;

    @Column(name = "notes")
    private String notes;

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    @Column(name = "need_cleaning", nullable = false)
    private Boolean needCleaning = false;


    public enum RoomStatus {
        AVAILABLE,      // 可用
        OCCUPIED,       // 已入住
        RESERVED,       // 已预订
        MAINTENANCE,    // 维护中
        CLEANING,       // 清洁中
        NEEDS_CLEANING  // 需要清洁
    }
}