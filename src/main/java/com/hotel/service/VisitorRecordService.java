package com.hotel.service;

import com.hotel.entity.User;
import com.hotel.entity.VisitorRecord;
import com.hotel.entity.Visitor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 访客记录服务接口
 */
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

    /**
     * 注册访客信息
     */
    Visitor registerVisitor(Visitor visitor);

    /**
     * 获取所有访客列表
     */
    List<Visitor> getAllVisitors();

    /**
     * 根据ID查询访客信息
     */
    Visitor getVisitorById(Long id);

    /**
     * 根据姓名查询访客信息
     */
    List<Visitor> getVisitorsByName(String name);

    /**
     * 根据手机号查询访客信息
     */
    Visitor getVisitorByPhone(String phone);

    /**
     * 获取今日访客列表
     */
    List<Visitor> getTodayVisitors();

    /**
     * 获取指定日期访客列表
     */
    List<Visitor> getVisitorsByDate(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取访问特定房间的访客列表
     */
    List<Visitor> getVisitorsByRoom(String roomNumber);

    /**
     * 更新访客信息
     */
    Visitor updateVisitor(Visitor visitor);

    /**
     * 删除访客记录
     */
    void deleteVisitor(Long id);

    /**
     * 统计今日访客数量
     */
    long countTodayVisitors();

    /**
     * 记录访客离开时间
     */
    Visitor recordVisitorLeave(Long id);
}