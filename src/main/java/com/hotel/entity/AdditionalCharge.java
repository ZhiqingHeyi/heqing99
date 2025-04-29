package com.hotel.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Entity
@Table(name = "additional_charges")
@EntityListeners(AuditingEntityListener.class)
public class AdditionalCharge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_in_record_id", nullable = false)
    private CheckInRecord checkInRecord;
    
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChargeType type;
    
    @Column(name = "description", nullable = false)
    private String description;
    
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
    
    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    private CheckInRecord.PaymentMethod paymentMethod;
    
    @Column(name = "notes")
    private String notes;
    
    @Column(name = "operator_id")
    private Long operatorId;
    
    @Column(name = "operator_name")
    private String operatorName;
    
    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    public enum ChargeType {
        MINIBAR,       // 迷你吧消费
        ROOM_SERVICE,  // 客房服务
        RESTAURANT,    // 餐厅消费
        SPA,           // 水疗服务
        DAMAGE,        // 损坏赔偿
        OTHER          // 其他消费
    }
    
    /**
     * 将Map列表转换为AdditionalCharge对象列表
     */
    public static List<AdditionalCharge> fromList(List<Map<String, Object>> chargesList) {
        List<AdditionalCharge> result = new ArrayList<>();
        
        if (chargesList != null && !chargesList.isEmpty()) {
            for (Map<String, Object> chargeMap : chargesList) {
                AdditionalCharge charge = new AdditionalCharge();
                
                if (chargeMap.containsKey("type")) {
                    charge.setType(ChargeType.valueOf(chargeMap.get("type").toString()));
                }
                
                if (chargeMap.containsKey("description")) {
                    charge.setDescription((String) chargeMap.get("description"));
                }
                
                if (chargeMap.containsKey("amount")) {
                    charge.setAmount(new BigDecimal(chargeMap.get("amount").toString()));
                }
                
                if (chargeMap.containsKey("paymentMethod")) {
                    charge.setPaymentMethod(CheckInRecord.PaymentMethod.valueOf(chargeMap.get("paymentMethod").toString()));
                }
                
                if (chargeMap.containsKey("notes")) {
                    charge.setNotes((String) chargeMap.get("notes"));
                }
                
                if (chargeMap.containsKey("operatorId")) {
                    charge.setOperatorId(Long.valueOf(chargeMap.get("operatorId").toString()));
                }
                
                if (chargeMap.containsKey("operatorName")) {
                    charge.setOperatorName((String) chargeMap.get("operatorName"));
                }
                
                result.add(charge);
            }
        }
        
        return result;
    }
} 