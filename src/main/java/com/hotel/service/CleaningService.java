package com.hotel.service;

import com.hotel.entity.CleaningRecord;
import com.hotel.entity.Room;
import com.hotel.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface CleaningService {
    /**
     * 创建清洁记录
     */
    CleaningRecord createCleaningRecord(CleaningRecord cleaningRecord);

    /**
     * 分配清洁任务给保洁人员
     */
    CleaningRecord assignCleaningTask(Long roomId, Long staffId);

    /**
     * 开始清洁任务
     */
    CleaningRecord startCleaning(Long recordId);

    /**
     * 完成清洁任务
     */
    CleaningRecord completeCleaning(Long recordId);

    /**
     * 验证清洁结果
     */
    CleaningRecord verifyCleaningResult(Long recordId);

    /**
     * 获取清洁记录详情
     */
    CleaningRecord getCleaningRecordById(Long id);

    /**
     * 获取保洁人员的所有清洁记录
     */
    List<CleaningRecord> getCleaningRecordsByStaff(User staff);

    /**
     * 获取房间的所有清洁记录
     */
    List<CleaningRecord> getCleaningRecordsByRoom(Room room);

    /**
     * 获取指定状态的清洁记录
     */
    List<CleaningRecord> getCleaningRecordsByStatus(CleaningRecord.CleaningStatus status);

    /**
     * 获取指定时间范围内的清洁记录
     */
    List<CleaningRecord> getCleaningRecordsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计保洁人员的清洁任务数量
     */
    long countCleaningTasksByStaff(User staff, CleaningRecord.CleaningStatus status);

    /**
     * 获取保洁人员当前的清洁任务
     */
    List<CleaningRecord> getCurrentCleaningByStaff(User staff);

    /**
     * 获取房间最近的清洁记录
     */
    List<CleaningRecord> getLatestCleaningRecordsByRoom(Room room);

    /**
     * 统计指定时间范围内完成的清洁任务数量
     */
    long countCompletedCleaningInTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 更新清洁记录
     */
    CleaningRecord updateCleaningRecord(CleaningRecord cleaningRecord);

    /**
     * 获取所有清洁记录
     */
    List<CleaningRecord> getAllCleaningRecords();

    /**
     * 获取清洁任务统计数据
     * @return Map<String, Long> 包含各种状态任务数量的映射
     */
    java.util.Map<String, Long> getTasksStatistics();
}