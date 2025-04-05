package com.hotel.repository;

import com.hotel.entity.PointsExchangeRecord;
import com.hotel.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointsExchangeRecordRepository extends JpaRepository<PointsExchangeRecord, Long> {
    /**
     * 获取用户的所有积分兑换记录
     */
    List<PointsExchangeRecord> findByUser(User user);

    /**
     * 分页获取用户的积分兑换记录
     */
    Page<PointsExchangeRecord> findByUser(User user, Pageable pageable);
    
    /**
     * 获取特定状态的兑换记录
     */
    List<PointsExchangeRecord> findByUserAndStatus(User user, PointsExchangeRecord.ExchangeStatus status);
    
    /**
     * 按兑换类型查询记录
     */
    List<PointsExchangeRecord> findByUserAndExchangeType(User user, PointsExchangeRecord.ExchangeType type);
    
    /**
     * 按时间范围查询记录
     */
    List<PointsExchangeRecord> findByUserAndCreateTimeBetween(User user, LocalDateTime start, LocalDateTime end);
    
    /**
     * 统计用户兑换使用的总积分
     */
    @Query("SELECT SUM(p.pointsUsed) FROM PointsExchangeRecord p WHERE p.user = ?1 AND p.status = 'COMPLETED'")
    Integer sumPointsUsedByUser(User user);
    
    /**
     * 统计特定兑换类型的记录数量
     */
    long countByUserAndExchangeType(User user, PointsExchangeRecord.ExchangeType type);
    
    /**
     * 查询待处理的兑换请求
     */
    List<PointsExchangeRecord> findByStatus(PointsExchangeRecord.ExchangeStatus status);
} 