package com.hotel.service.impl;

import com.hotel.entity.CleaningRecord;
import com.hotel.entity.Room;
import com.hotel.entity.User;
import com.hotel.repository.CleaningRecordRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.service.CleaningService;
import com.hotel.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CleaningServiceImpl implements CleaningService {

    private static final Logger logger = LoggerFactory.getLogger(CleaningServiceImpl.class);

    @Autowired
    private CleaningRecordRepository cleaningRecordRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomService roomService;

    @Override
    public CleaningRecord createCleaningRecord(CleaningRecord cleaningRecord) {
        logger.info("Entering createCleaningRecord. Received record status: {}", cleaningRecord.getStatus());
        cleaningRecord.setCreateTime(LocalDateTime.now());
        // 如果传入的记录没有状态，则默认为PENDING
        if (cleaningRecord.getStatus() == null) {
            logger.info("Status was null, setting to PENDING.");
            cleaningRecord.setStatus(CleaningRecord.CleaningStatus.PENDING);
        } else {
            logger.info("Status was not null ({}), keeping existing status.", cleaningRecord.getStatus());
        }
        CleaningRecord savedRecord = cleaningRecordRepository.save(cleaningRecord);
        logger.info("Saved CleaningRecord. ID: {}, Final Status in DB: {}", savedRecord.getId(), savedRecord.getStatus());
        return savedRecord;
    }

    @Override
    public CleaningRecord assignCleaningTask(Long roomId, Long staffId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("房间不存在"));

        if (room.getStatus() != Room.RoomStatus.NEEDS_CLEANING) {
            throw new RuntimeException("房间当前不需要清洁");
        }

        // 检查是否已存在未完成的清洁任务
        List<CleaningRecord> existingTasks = cleaningRecordRepository.findByRoomAndStatus(
                room, CleaningRecord.CleaningStatus.PENDING);
        if (!existingTasks.isEmpty()) {
            throw new RuntimeException("该房间已存在未完成的清洁任务");
        }

        CleaningRecord cleaningRecord = new CleaningRecord();
        cleaningRecord.setRoom(room);
        cleaningRecord.setStaffId(staffId);
        cleaningRecord.setCreateTime(LocalDateTime.now());
        cleaningRecord.setStatus(CleaningRecord.CleaningStatus.ASSIGNED);

        return cleaningRecordRepository.save(cleaningRecord);
    }

    @Override
    public CleaningRecord startCleaning(Long recordId) {
        CleaningRecord record = getCleaningRecordById(recordId);

        if (record.getStatus() != CleaningRecord.CleaningStatus.ASSIGNED) {
            throw new RuntimeException("只能开始已分配的清洁任务");
        }

        record.setStatus(CleaningRecord.CleaningStatus.IN_PROGRESS);
        record.setStartTime(LocalDateTime.now());
        return cleaningRecordRepository.save(record);
    }

    @Override
    public CleaningRecord completeCleaning(Long recordId) {
        CleaningRecord record = getCleaningRecordById(recordId);

        if (record.getStatus() != CleaningRecord.CleaningStatus.IN_PROGRESS) {
            throw new RuntimeException("只能完成正在进行中的清洁任务");
        }

        record.setStatus(CleaningRecord.CleaningStatus.COMPLETED);
        record.setEndTime(LocalDateTime.now());
        return cleaningRecordRepository.save(record);
    }

    @Override
    public CleaningRecord verifyCleaningResult(Long recordId) {
        CleaningRecord record = getCleaningRecordById(recordId);

        if (record.getStatus() != CleaningRecord.CleaningStatus.COMPLETED) {
            throw new RuntimeException("只能验证已完成的清洁任务");
        }

        record.setStatus(CleaningRecord.CleaningStatus.VERIFIED);
        record.setVerifyTime(LocalDateTime.now());

        // 更新房间状态为可用
        roomService.confirmRoomCleaned(record.getRoom().getId());

        return cleaningRecordRepository.save(record);
    }

    @Override
    public CleaningRecord getCleaningRecordById(Long id) {
        return cleaningRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("清洁记录不存在"));
    }

    @Override
    public List<CleaningRecord> getCleaningRecordsByStaff(User staff) {
        return cleaningRecordRepository.findByStaffId(staff.getId());
    }

    @Override
    public List<CleaningRecord> getCleaningRecordsByRoom(Room room) {
        return cleaningRecordRepository.findByRoom(room);
    }

    @Override
    public List<CleaningRecord> getCleaningRecordsByStatus(CleaningRecord.CleaningStatus status) {
        return cleaningRecordRepository.findByStatus(status);
    }

    @Override
    public List<CleaningRecord> getCleaningRecordsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return cleaningRecordRepository.findByCreateTimeBetween(startTime, endTime);
    }

    @Override
    public long countCleaningTasksByStaff(User staff, CleaningRecord.CleaningStatus status) {
        return cleaningRecordRepository.countByStaffIdAndStatus(staff.getId(), status);
    }

    @Override
    public List<CleaningRecord> getCurrentCleaningByStaff(User staff) {
        return cleaningRecordRepository.findByStaffIdAndStatus(
                staff.getId(), CleaningRecord.CleaningStatus.IN_PROGRESS);
    }

    @Override
    public List<CleaningRecord> getLatestCleaningRecordsByRoom(Room room) {
        return cleaningRecordRepository.findTop5ByRoomOrderByCreateTimeDesc(room);
    }

    @Override
    public long countCompletedCleaningInTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return cleaningRecordRepository.countByStatusAndEndTimeBetween(
                CleaningRecord.CleaningStatus.VERIFIED, startTime, endTime);
    }

    @Override
    public CleaningRecord updateCleaningRecord(CleaningRecord cleaningRecord) {
        CleaningRecord existingRecord = getCleaningRecordById(cleaningRecord.getId());

        // 只允许更新某些字段
        existingRecord.setNotes(cleaningRecord.getNotes());
        existingRecord.setStaffId(cleaningRecord.getStaffId());

        return cleaningRecordRepository.save(existingRecord);
    }
    
    @Override
    public List<CleaningRecord> getAllCleaningRecords() {
        return cleaningRecordRepository.findAll();
    }

    @Override
    public java.util.Map<String, Long> getTasksStatistics() {
        java.util.Map<String, Long> statistics = new java.util.HashMap<>();
        
        // 统计已完成的任务
        long completedTasks = cleaningRecordRepository.countByStatus(CleaningRecord.CleaningStatus.COMPLETED) 
                            + cleaningRecordRepository.countByStatus(CleaningRecord.CleaningStatus.VERIFIED);
        statistics.put("completed", completedTasks);
        
        // 统计进行中的任务
        long inProgressTasks = cleaningRecordRepository.countByStatus(CleaningRecord.CleaningStatus.IN_PROGRESS);
        statistics.put("inProgress", inProgressTasks);
        
        // 统计待处理的任务
        long pendingTasks = cleaningRecordRepository.countByStatus(CleaningRecord.CleaningStatus.PENDING)
                          + cleaningRecordRepository.countByStatus(CleaningRecord.CleaningStatus.ASSIGNED);
        statistics.put("pending", pendingTasks);
        
        return statistics;
    }
}