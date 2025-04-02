package com.hotel.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "visitor_records")
@EntityListeners(AuditingEntityListener.class)
public class VisitorRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String visitorName;

    private String phone;

    private String idCard;

    @Column(nullable = false)
    private String purpose;

    @ManyToOne
    @JoinColumn(name = "visited_user_id")
    private User visitedUser;

    @Column(nullable = false)
    private LocalDateTime visitTime;

    private LocalDateTime leaveTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VisitStatus status;

    private String remarks;

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    public enum VisitStatus {
        VISITING,   // 访问中
        COMPLETED,  // 已结束
        CANCELLED   // 已取消
    }
}