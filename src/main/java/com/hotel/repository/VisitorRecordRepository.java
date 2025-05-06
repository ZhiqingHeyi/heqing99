package com.hotel.repository;

import com.hotel.entity.User;
import com.hotel.entity.VisitorRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitorRecordRepository extends JpaRepository<VisitorRecord, Long>, JpaSpecificationExecutor<VisitorRecord> {
    List<VisitorRecord> findByVisitedUser(User visitedUser);

    List<VisitorRecord> findByStatus(VisitorRecord.VisitStatus status);

    @Query("SELECT v FROM VisitorRecord v WHERE v.visitTime >= ?1 AND v.visitTime <= ?2")
    List<VisitorRecord> findByVisitTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT v FROM VisitorRecord v WHERE v.status = 'VISITING' AND v.visitTime <= ?1")
    List<VisitorRecord> findCurrentVisitors(LocalDateTime currentTime);

    @Query("SELECT COUNT(v) FROM VisitorRecord v WHERE v.visitTime >= ?1 AND v.visitTime <= ?2")
    long countVisitorsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT v FROM VisitorRecord v WHERE v.visitorName LIKE %?1% OR v.phone LIKE %?1%")
    List<VisitorRecord> searchVisitors(String keyword);

    @Query("SELECT v FROM VisitorRecord v WHERE v.status = 'VISITING' AND v.visitedUser = ?1")
    List<VisitorRecord> findCurrentVisitorsByUser(User visitedUser);

    long countByVisitTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
}