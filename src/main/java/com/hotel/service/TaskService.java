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
     * 获取需要清洁的房间列表
     */
    List<TaskDTO> getAvailableRooms();

    /**
     * 获取可用保洁员列表
     */
    List<TaskDTO> getAvailableCleaners();
    
    /**
     * 为需要清洁的房间自动生成清洁任务
     * 此方法检查所有需要清洁的房间，如果没有相应的任务则创建
     */
    List<Task> generateCleaningTasksFromRooms();
    
    /**
     * 同步数据库中CLEANING状态的房间为processing状态的任务
     * 这确保了即使房间状态被直接修改为CLEANING，相应的任务也会同步更新为processing状态
     */
    void syncCleaningRoomTasks();
}