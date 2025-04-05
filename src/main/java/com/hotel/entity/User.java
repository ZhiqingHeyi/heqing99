package com.hotel.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    @JsonIgnoreProperties
    private String password;

    @Column(nullable = false)
    private String name;

    private String phone;

    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(nullable = false)
    private Boolean enabled = true;
    
    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties("user")
    private List<Reservation> reservations;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberLevel memberLevel = MemberLevel.REGULAR;
    
    @Column(nullable = false)
    private Integer points = 0;
    
    @Column(nullable = false)
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    @Transient
    private String invitationCode;
    
    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties("user")
    private List<ConsumptionRecord> consumptionRecords;
    
    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties("user")
    private List<MemberLevelChangeRecord> levelChangeRecords;
    
    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties("user")
    private List<PointsExchangeRecord> pointsExchangeRecords;

    public enum UserRole {
        SUPER_ADMIN, // 超级管理员
        admin,      // 管理员
        receptionist,      // 前台
        cleaner,    // 清洁人员
        CUSTOMER    // 普通用户
    }
    
    /**
     * 获取会员折扣率
     */
    public double getDiscountRate() {
        return memberLevel.getDiscount();
    }
    
    /**
     * 根据累计消费更新会员等级
     */
    public void updateMemberLevel() {
        this.memberLevel = MemberLevel.getByTotalSpent(totalSpent.intValue());
    }
}