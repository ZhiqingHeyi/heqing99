package com.hotel.service;

import com.hotel.entity.Task;
import com.hotel.dto.TaskDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaskService {
    /**
     * 创建清洁任务
     */
    Task createTask(TaskDTO taskDTO);

    /**
     * 开始处理任务
     */
    Task startTask(Long taskId);

    /**
     * 完成任务
     */
    Task completeTask(Long taskId, TaskDTO completionDetails);

    /**
     * 分页查询任务列表
     */
    Page<Task> findTasks(String status, Pageable pageable);

    /**
     * 获取任务统计信息
     */
    TaskDTO getTaskStatistics();

    /**
     * 根据ID获取任务详情
     */
    Task getTaskById(Long taskId);

    /**
     * 获取可分配的房间列表
     */
    List<TaskDTO> getAvailableRooms();

    /**
     * 获取可用保洁员列表
     */
    List<TaskDTO> getAvailableCleaners();
}