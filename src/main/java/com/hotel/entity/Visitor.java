package com.hotel.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "visitors")
@EntityListeners(AuditingEntityListener.class)
public class Visitor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_in_record_id", nullable = false)
    private CheckInRecord checkInRecord;

    @Column(nullable = false)
    private String name;

    @Column(name = "id_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private IdType idType;

    @Column(name = "id_number", nullable = false)
    private String idNumber;

    private String phone;

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    public enum IdType {
        ID_CARD,           // 身份证
        PASSPORT,          // 护照
        DRIVING_LICENSE    // 驾驶证
    }
}