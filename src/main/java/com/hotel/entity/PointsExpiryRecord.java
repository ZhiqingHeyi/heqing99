package com.hotel.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 积分到期记录实体类
 */
@Data
@Entity
@Table(name = "points_expiry_records")
@EntityListeners(AuditingEntityListener.class)
public class PointsExpiryRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"pointsExpiryRecords", "reservations", "consumptionRecords"})
    private User user;

    /**
     * 到期积分数量
     */
    @Column(nullable = false)
    private Integer points;

    /**
     * 过期日期
     */
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    /**
     * 是否已通知用户
     */
    @Column(nullable = false)
    private Boolean notified = false;

    /**
     * 通知时间
     */
    private LocalDateTime notificationTime;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 记录创建时间
     */
    @CreatedDate
    private LocalDateTime createTime;

    /**
     * 创建积分过期记录
     * 
     * @param user 用户
     * @param points 积分数量
     * @param expiryDate 过期日期
     * @param remarks 备注
     * @return 积分过期记录对象
     */
    public static PointsExpiryRecord create(User user, Integer points, LocalDateTime expiryDate, String remarks) {
        PointsExpiryRecord record = new PointsExpiryRecord();
        record.setUser(user);
        record.setPoints(points);
        record.setExpiryDate(expiryDate);
        record.setRemarks(remarks);
        return record;
    }
} 