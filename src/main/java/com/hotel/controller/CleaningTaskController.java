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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cleaning")
public class CleaningTaskController {

    private static final Logger logger = LoggerFactory.getLogger(CleaningTaskController.class);

    // API响应封装类
    public class ApiResponse {
        private boolean success;
        private String message;

        public ApiResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

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
    public ResponseEntity<?> getTaskStatistics() {
        try {
            TaskDTO statistics = taskService.getTaskStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", statistics);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取任务统计信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "获取任务统计信息失败: " + e.getMessage()));
        }
    }

    /**
     * 获取任务列表
     */
    @GetMapping("/tasks")
    public ResponseEntity<?> getTasks(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            logger.info("获取清洁任务列表，参数：status={}, page={}, size={}", status, page, size);
            
            // 先自动生成清洁任务
            List<Task> generatedTasks = taskService.generateCleaningTasksFromRooms();
            int generatedCount = generatedTasks.size();
            
            // 确保同步数据库中CLEANING状态的房间为processing状态的任务
            taskService.syncCleaningRoomTasks();
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
            Page<Task> tasksPage;
            
            if (status != null && !status.isEmpty()) {
                tasksPage = taskService.findTasks(status, pageable);
            } else {
                tasksPage = taskService.findTasks(null, pageable);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", tasksPage.getContent());
            response.put("totalPages", tasksPage.getTotalPages());
            response.put("totalElements", tasksPage.getTotalElements());
            response.put("currentPage", tasksPage.getNumber());
            response.put("generatedTasksCount", generatedCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取清洁任务失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "获取清洁任务失败: " + e.getMessage()));
        }
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
    public ResponseEntity<?> getAvailableRooms() {
        try {
            List<TaskDTO> rooms = taskService.getAvailableRooms();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", rooms);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取可用房间列表失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "获取可用房间列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取可用的保洁人员列表
     */
    @GetMapping("/available-cleaners")
    public ResponseEntity<?> getAvailableCleaners() {
        try {
            List<TaskDTO> cleaners = taskService.getAvailableCleaners();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", cleaners);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取可用保洁员列表失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "获取可用保洁员列表失败: " + e.getMessage()));
        }
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
    
    /**
     * 手动触发生成清洁任务
     */
    @PostMapping("/tasks/generate")
    public ResponseEntity<?> generateTasks() {
        try {
            List<Task> generatedTasks = taskService.generateCleaningTasksFromRooms();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("generatedTasksCount", generatedTasks.size());
            response.put("message", "成功生成" + generatedTasks.size() + "个清洁任务");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("生成清洁任务失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "生成清洁任务失败: " + e.getMessage()));
        }
    }
} 