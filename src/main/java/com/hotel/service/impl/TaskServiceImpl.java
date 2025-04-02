package com.hotel.service.impl;

import com.hotel.entity.Task;
import com.hotel.dto.TaskDTO;
import com.hotel.repository.TaskRepository;
import com.hotel.service.TaskService;
import com.hotel.exception.BusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

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
        // TODO: 实现获取可分配房间的逻辑
        return taskRepository.findAvailableRooms();
    }

    @Override
    public List<TaskDTO> getAvailableCleaners() {
        // TODO: 实现获取可用保洁员的逻辑
        return taskRepository.findAvailableCleaners();
    }
}