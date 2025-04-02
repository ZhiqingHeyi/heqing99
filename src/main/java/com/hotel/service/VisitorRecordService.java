package com.hotel.service;

import com.hotel.entity.User;
import com.hotel.entity.VisitorRecord;

import java.time.LocalDateTime;
import java.util.List;

public interface VisitorRecordService {
    /**
     * 创建访客记录
     */
    VisitorRecord createVisitorRecord(VisitorRecord visitorRecord);

    /**
     * 更新访客记录
     */
    VisitorRecord updateVisitorRecord(VisitorRecord visitorRecord);

    /**
     * 登记访客离开
     */
    VisitorRecord recordVisitorLeave(Long recordId, LocalDateTime leaveTime);

    /**
     * 取消访客记录
     */
    void cancelVisitorRecord(Long recordId);

    /**
     * 获取访客记录详情
     */
    VisitorRecord getVisitorRecordById(Long id);

    /**
     * 获取被访问用户的所有访客记录
     */
    List<VisitorRecord> getVisitorRecordsByVisitedUser(User visitedUser);

    /**
     * 获取指定状态的访客记录
     */
    List<VisitorRecord> getVisitorRecordsByStatus(VisitorRecord.VisitStatus status);

    /**
     * 获取指定时间范围内的访客记录
     */
    List<VisitorRecord> getVisitorRecordsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取当前在访的访客记录
     */
    List<VisitorRecord> getCurrentVisitors();

    /**
     * 统计指定时间范围内的访客数量
     */
    long countVisitorsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 搜索访客记录
     */
    List<VisitorRecord> searchVisitorRecords(String keyword);

    /**
     * 获取指定用户当前的访客
     */
    List<VisitorRecord> getCurrentVisitorsByUser(User visitedUser);

    /**
     * 批量更新访客记录状态
     */
    void updateVisitorRecordsStatus(List<Long> recordIds, VisitorRecord.VisitStatus status);

    /**
     * 获取所有访客记录
     */
    List<VisitorRecord> getAllVisitorRecords();
}