package com.hotel.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "statistics")
@EntityListeners(AuditingEntityListener.class)
public class Statistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    // 房间总数
    private Integer totalRooms;

    // 已入住房间数
    private Integer occupiedRooms;

    // 已预订房间数
    private Integer reservedRooms;

    // 空闲房间数
    private Integer availableRooms;

    // 入住率
    private Double occupancyRate;

    // 日收入
    private BigDecimal dailyRevenue;

    // 待清洁房间数
    private Integer roomsNeedCleaning;

    // 已完成清洁房间数
    private Integer roomsCleaned;

    // 当日访客数
    private Integer visitorCount;

    // 当日预订数
    private Integer reservationCount;

    @CreatedDate
    private LocalDateTime createTime;

    // 统计类型
    @Enumerated(EnumType.STRING)
    private StatisticsType type;

    public enum StatisticsType {
        DAILY,      // 日统计
        WEEKLY,     // 周统计
        MONTHLY     // 月统计
    }
}