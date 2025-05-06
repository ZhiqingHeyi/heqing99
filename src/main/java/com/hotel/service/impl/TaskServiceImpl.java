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
        BeanUtils.copyProperties(completionDetails, task);
        task.setStatus("completed");
        task.setCompleteTime(LocalDateTime.now());
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
            // 检查是否已存在该房间的待处理或进行中任务
            boolean taskExists = taskRepository.existsByRoomNumberAndStatusNot(
                room.getRoomNumber(), "completed");
                
            if (!taskExists) {
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
            }
        }
        
        logger.info("自动生成清洁任务完成，共创建 {} 个新任务", createdTasks.size());
        return createdTasks;
    }
}