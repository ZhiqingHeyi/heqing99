package com.hotel.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "marketing_campaigns")
@EntityListeners(AuditingEntityListener.class)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class MarketingCampaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CampaignType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CampaignStatus status;

    @Column
    private BigDecimal discountRate;

    @Column
    private Integer bonusPoints;

    @Column
    private String targetMemberLevel;

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    public enum CampaignType {
        DISCOUNT,      // 折扣活动
        BONUS_POINTS,  // 积分奖励
        ROOM_UPGRADE,  // 房间升级
        SPECIAL_OFFER  // 特别优惠
    }

    public enum CampaignStatus {
        DRAFT,      // 草稿
        SCHEDULED,  // 已安排
        ACTIVE,     // 进行中
        COMPLETED,  // 已完成
        CANCELLED   // 已取消
    }
} 