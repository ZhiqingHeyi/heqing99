package com.hotel.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员消费记录实体类
 */
@Data
@Entity
@Table(name = "consumption_records")
@EntityListeners(AuditingEntityListener.class)
public class ConsumptionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 消费金额
     */
    @Column(nullable = false)
    private BigDecimal amount;

    /**
     * 折扣后金额
     */
    @Column(nullable = false)
    private BigDecimal discountedAmount;

    /**
     * 应用的折扣率
     */
    @Column(nullable = false)
    private Double discountRate;

    /**
     * 获得的积分
     */
    @Column(nullable = false)
    private Integer pointsEarned;

    /**
     * 消费描述/备注
     */
    private String description;

    /**
     * 消费类型（住宿、餐饮、会议室等）
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConsumptionType type;

    /**
     * 关联的预订ID（如有）
     */
    private Long reservationId;

    /**
     * 关联的房间ID（如有）
     */
    private Long roomId;

    /**
     * 创建时间
     */
    @CreatedDate
    private LocalDateTime createTime;

    /**
     * 消费类型枚举
     */
    public enum ConsumptionType {
        ACCOMMODATION("住宿"),
        CATERING("餐饮"),
        MEETING_ROOM("会议室"),
        SPA("SPA"),
        GYM("健身"),
        OTHER("其他");

        private final String displayName;

        ConsumptionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
} 