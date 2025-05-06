package com.hotel.repository;

import com.hotel.entity.Task;
import com.hotel.dto.TaskDTO;
import com.hotel.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    /**
     * 根据状态查询任务
     */
    Page<Task> findByStatus(String status, Pageable pageable);

    /**
     * 统计特定状态的任务数量
     */
    long countByStatus(String status);

    /**
     * 统计特定优先级的任务数量
     */
    long countByPriority(String priority);

    /**
     * 检查指定房间是否已存在非指定状态的任务
     */
    boolean existsByRoomNumberAndStatusNot(String roomNumber, String status);

    /**
     * 查询需要清洁的房间
     */
    @Query("SELECT new com.hotel.dto.TaskDTO(r.roomNumber, r.roomType.name) FROM Room r WHERE r.status = 'NEEDS_CLEANING' OR r.needCleaning = true")
    List<TaskDTO> findAvailableRooms();

    /**
     * 查询可用的清洁员
     */
    @Query("SELECT new com.hotel.dto.TaskDTO(u.id, u.name) FROM User u WHERE u.role = 'CLEANER' AND u.status = 'ACTIVE'")
    List<TaskDTO> findAvailableCleaners();
}