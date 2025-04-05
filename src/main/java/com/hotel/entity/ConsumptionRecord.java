package com.hotel.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    @JsonIgnoreProperties({"consumptionRecords", "reservations", "pointsExchangeRecords"})
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
    @Column(nullable = false)
    private String type;

    /**
     * 关联的预订ID（如有）
     */
    private Long reservationId;

    /**
     * 关联的房间ID（如有）
     */
    private Long roomId;

    /**
     * 消费时间
     */
    @Column(name = "consumption_time")
    @CreatedDate
    private LocalDateTime consumptionTime;

    /**
     * 创建消费记录
     * 
     * @param user 用户
     * @param amount 消费金额
     * @param type 消费类型
     * @param description 描述
     * @return 消费记录对象
     */
    public static ConsumptionRecord create(User user, BigDecimal amount, String type, String description) {
        ConsumptionRecord record = new ConsumptionRecord();
        record.setUser(user);
        record.setAmount(amount);
        record.setType(type);
        record.setDescription(description);
        record.setConsumptionTime(LocalDateTime.now());
        
        // 计算积分 (根据会员等级计算积分率)
        double pointsRate = 0;
        switch (user.getMemberLevel()) {
            case BRONZE:
                pointsRate = 1.0;
                break;
            case SILVER:
                pointsRate = 1.2;
                break;
            case GOLD:
                pointsRate = 1.5;
                break;
            case DIAMOND:
                pointsRate = 2.0;
                break;
            default:
                pointsRate = 0;
        }
        
        int earnedPoints = (int) (amount.doubleValue() * pointsRate);
        record.setPointsEarned(earnedPoints);
        
        return record;
    }
} 