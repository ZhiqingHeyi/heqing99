package com.hotel.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    @JsonIgnoreProperties({"pointsExchangeRecords", "reservations", "consumptionRecords"})
    private User user;

    /**
     * 兑换使用的积分数量
     */
    @Column(name = "points_used", nullable = false)
    private Integer pointsUsed;

    /**
     * 兑换等价值（元）
     */
    @Column(name = "cash_value", nullable = false)
    private BigDecimal cashValue;

    /**
     * 兑换类型
     */
    @Column(name = "exchange_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ExchangeType exchangeType;

    /**
     * 兑换描述
     */
    private String description;

    /**
     * 兑换状态
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExchangeStatus status;

    /**
     * 兑换时间
     */
    @Column(name = "exchange_time")
    @CreatedDate
    private LocalDateTime exchangeTime;

    /**
     * 记录创建时间
     */
    @CreatedDate
    private LocalDateTime createTime;

    /**
     * 操作人ID（管理员）
     */
    private Long operatorId;

    /**
     * 兑换类型枚举
     */
    public enum ExchangeType {
        ROOM_DISCOUNT,  // 房间折扣
        CASH_VOUCHER,   // 现金券
        FREE_BREAKFAST, // 免费早餐
        SPA_VOUCHER,    // 水疗券
        GYM_PASS        // 健身通行证
    }

    /**
     * 兑换状态枚举
     */
    public enum ExchangeStatus {
        PENDING,    // 待处理
        COMPLETED,  // 已完成
        CANCELLED,  // 已取消
        REFUNDED    // 已退款
    }
} 