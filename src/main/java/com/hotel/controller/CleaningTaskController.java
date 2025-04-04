package com.hotel.controller;

import com.hotel.dto.TaskDTO;
import com.hotel.entity.Task;
import com.hotel.entity.Room;
import com.hotel.service.TaskService;
import com.hotel.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cleaning")
public class CleaningTaskController {

    private final TaskService taskService;
    private final RoomService roomService;

    @Autowired
    public CleaningTaskController(TaskService taskService, RoomService roomService) {
        this.taskService = taskService;
        this.roomService = roomService;
    }

    /**
     * 获取任务统计信息
     */
    @GetMapping("/tasks/statistics")
    public ResponseEntity<TaskDTO> getTaskStatistics() {
        TaskDTO statistics = taskService.getTaskStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * 获取任务列表
     */
    @GetMapping("/tasks")
    public ResponseEntity<Page<Task>> getTasks(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<Task> tasks = taskService.findTasks(status, pageable);
        return ResponseEntity.ok(tasks);
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/tasks/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    /**
     * 开始处理任务
     */
    @PostMapping("/tasks/{id}/start")
    public ResponseEntity<Task> startTask(@PathVariable Long id) {
        try {
            Task task = taskService.startTask(id);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 完成清洁任务
     */
    @PostMapping("/tasks/{id}/complete")
    public ResponseEntity<Task> completeTask(@PathVariable Long id, @RequestBody TaskDTO completionDetails) {
        try {
            Task task = taskService.completeTask(id, completionDetails);
            
            // 更新房间状态为可用
            Room room = roomService.getRoomByNumber(task.getRoomNumber());
            if (room != null) {
                roomService.updateRoomStatus(room.getId(), Room.RoomStatus.AVAILABLE);
            }
            
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 获取可用于分配任务的房间列表
     */
    @GetMapping("/available-rooms")
    public ResponseEntity<List<TaskDTO>> getAvailableRooms() {
        List<TaskDTO> rooms = taskService.getAvailableRooms();
        return ResponseEntity.ok(rooms);
    }
    
    /**
     * 获取可用的保洁人员列表
     */
    @GetMapping("/available-cleaners")
    public ResponseEntity<List<TaskDTO>> getAvailableCleaners() {
        List<TaskDTO> cleaners = taskService.getAvailableCleaners();
        return ResponseEntity.ok(cleaners);
    }
    
    /**
     * 分配清洁任务
     */
    @PostMapping("/tasks")
    public ResponseEntity<?> createTask(@RequestBody TaskDTO taskDTO) {
        try {
            Task task = taskService.createTask(taskDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(task);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "任务创建失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
} 