package com.hotel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private Long id;
    private String roomNumber;
    private String roomType;
    private String cleaner;
    private String priority;
    private String status;
    private LocalDateTime expectedTime;
    private String notes;
    
    // 任务完成相关信息
    private Integer actualDuration;
    private String[] cleanItems;
    private String[] supplies;
    private String issues;
    
    // 统计信息
    private long pendingCount;
    private long processingCount;
    private long completedCount;
    private long highPriorityCount;
    
    // 用于房间和保洁员查询的构造函数
    public TaskDTO(String number, String type) {
        this.roomNumber = number;
        this.roomType = type;
    }
    
    public TaskDTO(Long id, String name) {
        this.id = id;
        this.cleaner = name;
    }
}