package com.hotel.repository;

import com.hotel.entity.CleaningRecord;
import com.hotel.entity.Room;
import com.hotel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CleaningRecordRepository extends JpaRepository<CleaningRecord, Long> {
    List<CleaningRecord> findByStaff(User staff);

    List<CleaningRecord> findByRoom(Room room);

    List<CleaningRecord> findByStatus(CleaningRecord.CleaningStatus status);

    @Query("SELECT c FROM CleaningRecord c WHERE c.staff = ?1 AND c.status = ?2")
    List<CleaningRecord> findByStaffAndStatus(User staff, CleaningRecord.CleaningStatus status);

    @Query("SELECT c FROM CleaningRecord c WHERE c.startTime >= ?1 AND c.startTime <= ?2")
    List<CleaningRecord> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT COUNT(c) FROM CleaningRecord c WHERE c.status = ?1 AND c.staff = ?2")
    long countByStatusAndStaff(CleaningRecord.CleaningStatus status, User staff);

    @Query("SELECT c FROM CleaningRecord c WHERE c.status = 'IN_PROGRESS' AND c.staff = ?1")
    List<CleaningRecord> findCurrentCleaningByStaff(User staff);

    @Query("SELECT c FROM CleaningRecord c WHERE c.room = ?1 ORDER BY c.createTime DESC")
    List<CleaningRecord> findLatestByRoom(Room room);

    @Query("SELECT COUNT(c) FROM CleaningRecord c WHERE c.status = 'COMPLETED' AND c.endTime >= ?1 AND c.endTime <= ?2")
    long countCompletedCleaningInTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    List<CleaningRecord> findByRoomAndStatus(Room room, CleaningRecord.CleaningStatus status);

    List<CleaningRecord> findByStaffId(Long staffId);

    List<CleaningRecord> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    long countByStaffIdAndStatus(Long staffId, CleaningRecord.CleaningStatus status);

    List<CleaningRecord> findByStaffIdAndStatus(Long staffId, CleaningRecord.CleaningStatus status);

    List<CleaningRecord> findTop5ByRoomOrderByCreateTimeDesc(Room room);

    @Query("SELECT COUNT(c) FROM CleaningRecord c WHERE c.status = ?1 AND c.endTime >= ?2 AND c.endTime <= ?3")
    long countByStatusAndEndTimeBetween(CleaningRecord.CleaningStatus status, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计指定状态的任务数量
     */
    long countByStatus(CleaningRecord.CleaningStatus status);
}