package com.hotel.service.impl;

import com.hotel.entity.Task;
import com.hotel.entity.Room;
import com.hotel.dto.TaskDTO;
import com.hotel.repository.TaskRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.service.TaskService;
import com.hotel.exception.BusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {
    
    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private RoomRepository roomRepository;

    @Override
    @Transactional
    public Task createTask(TaskDTO taskDTO) {
        Task task = new Task();
        BeanUtils.copyProperties(taskDTO, task);
        task.setStatus("pending");
        task.setCreateTime(LocalDateTime.now());
        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public Task startTask(Long taskId) {
        Task task = getTaskById(taskId);
        if (!"pending".equals(task.getStatus())) {
            throw new BusinessException("只有待处理的任务可以开始处理");
        }
        task.setStatus("processing");
        task.setStartTime(LocalDateTime.now());
        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public Task completeTask(Long taskId, TaskDTO completionDetails) {
        Task task = getTaskById(taskId);
        if (!"processing".equals(task.getStatus())) {
            throw new BusinessException("只有进行中的任务可以标记完成");
        }
        
        // 避免使用BeanUtils，而是手动设置属性
        if (completionDetails != null) {
            // 设置实际用时
            if (completionDetails.getActualDuration() != null) {
                task.setActualDuration(completionDetails.getActualDuration());
            } else {
                task.setActualDuration(30); // 设置默认值
            }
            
            // 设置问题记录
            if (completionDetails.getIssues() != null) {
                task.setIssues(completionDetails.getIssues());
            }
        }
        
        task.setStatus("completed");
        task.setCompleteTime(LocalDateTime.now());
        logger.info("任务完成：ID={}, 房间={}, 实际用时={}分钟", 
                   taskId, task.getRoomNumber(), task.getActualDuration());
        
        // 重要：更新房间状态为不需要清洁，并设置状态为可用
        Optional<Room> roomOptional = roomRepository.findByRoomNumber(task.getRoomNumber());
        if (roomOptional.isPresent()) {
            Room room = roomOptional.get();
            room.setNeedCleaning(false);
            room.setStatus(Room.RoomStatus.AVAILABLE);
            roomRepository.save(room);
            logger.info("已更新房间状态：房间号={}, 状态=AVAILABLE, 需要清洁=false", room.getRoomNumber());
        }
        
        return taskRepository.save(task);
    }

    @Override
    public Page<Task> findTasks(String status, Pageable pageable) {
        if (status != null && !status.equals("all")) {
            return taskRepository.findByStatus(status, pageable);
        }
        return taskRepository.findAll(pageable);
    }

    @Override
    public TaskDTO getTaskStatistics() {
        // 确保任务数据最新，先生成任务
        generateCleaningTasksFromRooms();
        
        TaskDTO stats = new TaskDTO();
        stats.setPendingCount(taskRepository.countByStatus("pending"));
        stats.setProcessingCount(taskRepository.countByStatus("processing"));
        stats.setCompletedCount(taskRepository.countByStatus("completed"));
        stats.setHighPriorityCount(taskRepository.countByPriority("high"));
        return stats;
    }

    @Override
    public Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在"));
    }

    @Override
    public List<TaskDTO> getAvailableRooms() {
        return taskRepository.findAvailableRooms();
    }

    @Override
    public List<TaskDTO> getAvailableCleaners() {
        return taskRepository.findAvailableCleaners();
    }
    
    /**
     * 为需要清洁的房间自动生成清洁任务
     * 此方法检查所有需要清洁的房间，如果没有相应的任务则创建
     */
    @Override
    @Transactional
    public List<Task> generateCleaningTasksFromRooms() {
        logger.info("开始自动生成清洁任务...");
        List<Task> createdTasks = new ArrayList<>();
        
        // 查询所有需要清洁的房间 - 使用枚举值
        List<Room> roomsNeedCleaning = roomRepository.findByStatusOrNeedCleaning(Room.RoomStatus.NEEDS_CLEANING, true);
        logger.info("找到 {} 个需要清洁的房间", roomsNeedCleaning.size());
        
        // 获取可用清洁员列表
        List<TaskDTO> availableCleaners = taskRepository.findAvailableCleaners();
        String defaultCleaner = "系统";
        
        // 如果有可用清洁员，使用第一个作为默认
        if (!availableCleaners.isEmpty() && availableCleaners.get(0) != null) {
            defaultCleaner = availableCleaners.get(0).getCleaner();
            logger.info("将使用默认清洁员: {}", defaultCleaner);
        }
        
        for (Room room : roomsNeedCleaning) {
            // 首先检查是否已存在该房间的任何状态的任务
            List<Task> existingTasks = taskRepository.findByRoomNumber(room.getRoomNumber());
            
            // 如果不存在任务，或者只存在已完成的任务但房间状态仍为"需要清洁"，则创建新任务
            boolean shouldCreateTask = existingTasks.isEmpty();
            
            if (!shouldCreateTask) {
                // 检查是否所有任务都已完成但房间状态仍为"需要清洁"
                boolean allCompleted = true;
                for (Task existingTask : existingTasks) {
                    if (!"completed".equals(existingTask.getStatus())) {
                        allCompleted = false;
                        break;
                    }
                }
                // 如果所有任务都已完成，但房间状态仍为NEEDS_CLEANING，更新房间状态而不是创建新任务
                if (allCompleted) {
                    room.setNeedCleaning(false);
                    room.setStatus(Room.RoomStatus.AVAILABLE);
                    roomRepository.save(room);
                    logger.info("已更新房间 {} 状态为可用，跳过任务创建", room.getRoomNumber());
                    continue;
                }
            }
                
            if (shouldCreateTask) {
                // 创建新任务
                Task task = new Task();
                task.setRoomNumber(room.getRoomNumber());
                task.setRoomType(room.getRoomType().getName());
                task.setPriority("medium"); // 默认优先级
                task.setStatus("pending");
                task.setCreateTime(LocalDateTime.now());
                task.setNotes("系统自动生成的清洁任务");
                task.setCleaner(defaultCleaner); // 设置默认清洁员，确保不为null
                
                // 保存任务
                Task savedTask = taskRepository.save(task);
                createdTasks.add(savedTask);
                logger.info("为房间 {} 创建了清洁任务，分配给清洁员: {}", room.getRoomNumber(), defaultCleaner);
            } else {
                logger.info("房间 {} 已存在有效的清洁任务，跳过任务创建", room.getRoomNumber());
            }
        }
        
        logger.info("自动生成清洁任务完成，共创建 {} 个新任务", createdTasks.size());
        return createdTasks;
    }
}