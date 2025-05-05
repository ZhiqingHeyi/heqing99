package com.heqing.hotel.repository;

import com.heqing.hotel.model.VisitorRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface VisitorRepository extends JpaRepository<VisitorRecord, Long> {

    /**
     * 统计指定时间之后的访客记录数量
     * @param visitTime 访问时间
     * @return 访客数量
     */
    long countByVisitTimeGreaterThanEqual(LocalDateTime visitTime);

    /**
     * 统计指定状态的访客记录数量
     * @param status 状态
     * @return 记录数量
     */
    long countByStatus(String status);
}