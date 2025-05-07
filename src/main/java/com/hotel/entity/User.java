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
import java.time.LocalDate;
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

    @Column(nullable = false)
    private String email;
    
    @Column
    private String gender;
    
    @Column
    private LocalDate birthday;

    @Column(name = "id_type")
    @Enumerated(EnumType.STRING)
    private IdType idType;

    @Column(name = "id_number")
    private String idNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";
    
    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties("user")
    private List<Reservation> reservations;
    
    @Column(name = "member_level", nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberLevel memberLevel = MemberLevel.REGULAR;
    
    @Column(nullable = false)
    private Integer points = 0;
    
    @Column(name = "total_spent", nullable = false)
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;
    
    /**
     * 用户注册时间，与createTime相同
     */
    @Transient
    public LocalDateTime getRegistrationTime() {
        return this.createTime;
    }

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
    
    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties("user")
    private List<PointsExpiryRecord> pointsExpiryRecords;
    
    /**
     * 积分有效期（天数）
     */
    @Column
    private Integer pointsValidityDays = 365;

    public enum UserRole {
        SUPER_ADMIN, // 超级管理员
        ADMIN,      // 管理员 (原admin)
        RECEPTIONIST,      // 前台 (原receptionist)
        CLEANER,    // 清洁人员 (原cleaner)
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
    
    /**
     * 计算积分到期日期
     */
    public LocalDateTime calculatePointsExpiryDate() {
        return LocalDateTime.now().plusDays(pointsValidityDays);
    }
}