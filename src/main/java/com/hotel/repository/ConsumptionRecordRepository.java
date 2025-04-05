package com.hotel.repository;

import com.hotel.entity.ConsumptionRecord;
import com.hotel.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsumptionRecordRepository extends JpaRepository<ConsumptionRecord, Long> {
    /**
     * 获取用户的所有消费记录
     */
    List<ConsumptionRecord> findByUser(User user);

    /**
     * 分页获取用户的消费记录
     */
    Page<ConsumptionRecord> findByUser(User user, Pageable pageable);
    
    /**
     * 按时间范围查询用户的消费记录
     */
    List<ConsumptionRecord> findByUserAndConsumptionTimeBetween(User user, LocalDateTime start, LocalDateTime end);
    
    /**
     * 按时间范围查询所有消费记录
     */
    List<ConsumptionRecord> findByConsumptionTimeBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * 按消费类型查询用户的消费记录
     */
    List<ConsumptionRecord> findByUserAndType(User user, String type);
    
    /**
     * 统计用户在一段时间内的消费总额
     */
    @Query("SELECT SUM(c.amount) FROM ConsumptionRecord c WHERE c.user = ?1 AND c.consumptionTime BETWEEN ?2 AND ?3")
    BigDecimal sumAmountByUserAndConsumptionTimeBetween(User user, LocalDateTime start, LocalDateTime end);
    
    /**
     * 统计用户在一段时间内获得的积分总额
     */
    @Query("SELECT SUM(c.pointsEarned) FROM ConsumptionRecord c WHERE c.user = ?1 AND c.consumptionTime BETWEEN ?2 AND ?3")
    Integer sumPointsEarnedByUserAndConsumptionTimeBetween(User user, LocalDateTime start, LocalDateTime end);
    
    /**
     * 获取消费金额最高的记录
     */
    List<ConsumptionRecord> findTop10ByUserOrderByAmountDesc(User user);
    
    /**
     * 查询特定消费类型的记录数量
     */
    long countByUserAndType(User user, String type);
} 