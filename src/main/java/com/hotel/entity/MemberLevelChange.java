package com.hotel.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "member_level_change")
@EqualsAndHashCode(callSuper = false)
public class MemberLevelChange {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "previous_level", nullable = false)
    private String previousLevel;
    
    @Column(name = "new_level", nullable = false)
    private String newLevel;
    
    @Column(name = "change_type", nullable = false)
    private String changeType;
    
    @Column(name = "change_reason")
    private String changeReason;
    
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
} 