package com.hotel.repository;

import com.hotel.entity.MemberLevel;
import com.hotel.entity.MemberLevelChangeRecord;
import com.hotel.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberLevelChangeRecordRepository extends JpaRepository<MemberLevelChangeRecord, Long> {
    /**
     * 获取用户的所有等级变更记录
     */
    List<MemberLevelChangeRecord> findByUser(User user);

    /**
     * 分页获取用户的等级变更记录
     */
    Page<MemberLevelChangeRecord> findByUser(User user, Pageable pageable);
    
    /**
     * 获取用户最新的等级变更记录
     */
    Optional<MemberLevelChangeRecord> findTopByUserOrderByCreateTimeDesc(User user);
    
    /**
     * 按时间范围查询用户的等级变更记录
     */
    List<MemberLevelChangeRecord> findByUserAndCreateTimeBetween(User user, LocalDateTime start, LocalDateTime end);
    
    /**
     * 按变更后等级查询记录
     */
    List<MemberLevelChangeRecord> findByNewLevel(MemberLevel level);
    
    /**
     * 统计特定等级的变更记录数量
     */
    long countByNewLevel(MemberLevel level);
} 