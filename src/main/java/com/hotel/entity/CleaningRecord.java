package com.hotel.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cleaning_records")
@EntityListeners(AuditingEntityListener.class)
public class CleaningRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = false)
    private User staff;

    @Column(name = "staff_id", insertable = false, updatable = false)
    private Long staffId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CleaningStatus status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String remarks;

    private String notes;

    private LocalDateTime verifyTime;

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    public enum CleaningStatus {
        PENDING,    // 待清洁
        ASSIGNED,   // 已分配
        IN_PROGRESS,// 清洁中
        COMPLETED,  // 已完成
        VERIFIED    // 已验证
    }
}