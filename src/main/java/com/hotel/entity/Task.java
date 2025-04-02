package com.hotel.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String roomNumber;

    @Column(nullable = false)
    private String roomType;

    @Column(nullable = false)
    private String cleaner;

    @Column(nullable = false)
    private String priority;

    @Column(nullable = false)
    private String status;

    @Column(name = "expected_time")
    private LocalDateTime expectedTime;

    private String notes;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "complete_time")
    private LocalDateTime completeTime;

    @Column(name = "actual_duration")
    private Integer actualDuration;

    @Column(name = "clean_items")
    private String cleanItems;

    @Column(name = "supplies")
    private String supplies;

    private String issues;
}