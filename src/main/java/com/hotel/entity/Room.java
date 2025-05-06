package com.hotel.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.hotel.entity.RoomType;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "rooms")
@EntityListeners(AuditingEntityListener.class)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_number", unique = true, nullable = false)
    private String roomNumber;

    @ManyToOne
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(nullable = false)
    private Integer floor;

    @Column(nullable = false)
    private RoomStatus status = RoomStatus.AVAILABLE;

    @Column(name = "notes")
    private String notes;

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    @Column(name = "need_cleaning")
    private boolean needCleaning = false;

    @Column
    private String description;

    @OneToMany(mappedBy = "room")
    @JsonIgnoreProperties("room")
    private List<Reservation> reservations;

    public enum RoomStatus {
        AVAILABLE,      // 可用
        OCCUPIED,       // 已入住
        RESERVED,       // 已预订
        MAINTENANCE,    // 维护中
        CLEANING,       // 清洁中
        NEEDS_CLEANING  // 需要清洁
    }
}