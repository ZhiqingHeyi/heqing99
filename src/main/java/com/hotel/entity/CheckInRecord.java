package com.hotel.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "check_in_records")
@EntityListeners(AuditingEntityListener.class)
public class CheckInRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 入住单号
    @Column(name = "check_in_number", unique = true)
    private String checkInNumber;
    
    // 房间ID
    @Column(name = "room_id", nullable = false)
    private Long roomId;
    
    // 房间号
    @Column(name = "room_number")
    private String roomNumber;
    
    // 房间类型
    @Column(name = "room_type")
    private String roomType;
    
    // 客人姓名
    @Column(name = "guest_name", nullable = false)
    private String guestName;
    
    // 证件类型
    @Column(name = "guest_id_type", nullable = false)
    private String guestIdType;
    
    // 证件号码
    @Column(name = "guest_id_number", nullable = false)
    private String guestIdNumber;
    
    // 客人手机号
    @Column(name = "guest_mobile", nullable = false)
    private String guestMobile;
    
    // 入住日期
    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;
    
    // 离店日期
    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;
    
    // 实际入住时间
    @Column(name = "actual_check_in_time", nullable = false)
    private LocalDateTime actualCheckInTime;
    
    // 实际离店时间
    @Column(name = "actual_check_out_time")
    private LocalDateTime actualCheckOutTime;
    
    // 入住人数
    @Column(name = "guest_count", nullable = false)
    private Integer guestCount;
    
    // 押金金额
    @Column(name = "deposit", nullable = false)
    private BigDecimal deposit;
    
    // 支付方式
    @Column(name = "payment_method", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    
    // 状态
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private CheckInStatus status;
    
    // 房费总额
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;
    
    // 特殊要求
    @Column(name = "special_requests")
    private String specialRequests;
    
    // 备注
    @Column(name = "remarks")
    private String remarks;
    
    // 操作员ID
    @Column(name = "operator_id")
    private Long operatorId;
    
    // 操作员姓名
    @Column(name = "operator_name")
    private String operatorName;
    
    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;
    
    // 额外消费记录
    @OneToMany(mappedBy = "checkInRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AdditionalCharge> additionalCharges = new ArrayList<>();
    
    // --- 恢复 bookingId 字段，移除 Reservation 对象关联 ---
    @Column(name = "booking_id", nullable = false) // 明确映射到非空列
    private Long bookingId;
    // --- 恢复结束 ---
    
    // --- 添加 Transient 字段接收前端的 expectedCheckOutTime ---
    @Transient // 表示此字段不映射到数据库列
    private String expectedCheckOutTime;
    // --- 添加结束 ---
    
    // 入住状态枚举
    public enum CheckInStatus {
        CHECKED_IN,   // 已入住
        CHECKED_OUT   // 已退房
    }
    
    // 支付方式枚举
    public enum PaymentMethod {
        CASH,        // 现金
        CREDIT_CARD, // 信用卡
        DEBIT_CARD,  // 储蓄卡
        WECHAT,      // 微信支付
        ALIPAY       // 支付宝
    }
}