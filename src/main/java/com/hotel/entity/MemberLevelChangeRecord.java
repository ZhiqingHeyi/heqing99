package com.hotel.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 会员等级变更记录实体类
 */
@Data
@Entity
@Table(name = "member_level_change_records")
@EntityListeners(AuditingEntityListener.class)
public class MemberLevelChangeRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 变更前的会员等级
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberLevel oldLevel;

    /**
     * 变更后的会员等级
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberLevel newLevel;

    /**
     * 变更原因
     */
    private String reason;

    /**
     * 变更时间
     */
    @CreatedDate
    private LocalDateTime createTime;

    /**
     * 创建会员等级变更记录
     * 
     * @param user 用户对象
     * @param oldLevel 旧等级
     * @param newLevel 新等级
     * @param reason 变更原因
     * @return 变更记录对象
     */
    public static MemberLevelChangeRecord create(User user, MemberLevel oldLevel, MemberLevel newLevel, String reason) {
        MemberLevelChangeRecord record = new MemberLevelChangeRecord();
        record.setUser(user);
        record.setOldLevel(oldLevel);
        record.setNewLevel(newLevel);
        record.setReason(reason);
        return record;
    }
} 