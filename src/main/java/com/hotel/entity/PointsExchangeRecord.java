package com.hotel.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 积分兑换记录实体类
 */
@Data
@Entity
@Table(name = "points_exchange_records")
@EntityListeners(AuditingEntityListener.class)
public class PointsExchangeRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 兑换使用的积分数量
     */
    @Column(nullable = false)
    private Integer pointsUsed;

    /**
     * 兑换等价值（元）
     */
    @Column(nullable = false)
    private BigDecimal cashValue;

    /**
     * 兑换类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExchangeType exchangeType;

    /**
     * 兑换描述
     */
    private String description;

    /**
     * 兑换状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExchangeStatus status;

    /**
     * 创建时间
     */
    @CreatedDate
    private LocalDateTime createTime;

    /**
     * 兑换时间
     */
    private LocalDateTime exchangeTime;

    /**
     * 操作人ID（管理员）
     */
    private Long operatorId;

    /**
     * 兑换类型枚举
     */
    public enum ExchangeType {
        CASH_VOUCHER("现金券"),
        ROOM_DISCOUNT("房费折扣"),
        FREE_BREAKFAST("免费早餐"),
        FREE_MINIBAR("免费迷你吧"),
        SPA_VOUCHER("SPA券"),
        GYM_PASS("健身通行证"),
        GIFT("礼品"),
        OTHER("其他");

        private final String displayName;

        ExchangeType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 兑换状态枚举
     */
    public enum ExchangeStatus {
        PENDING("待处理"),
        COMPLETED("已完成"),
        CANCELLED("已取消"),
        EXPIRED("已过期");

        private final String displayName;

        ExchangeStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
} 