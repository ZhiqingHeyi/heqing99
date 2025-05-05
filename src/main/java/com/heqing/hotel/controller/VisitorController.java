package com.heqing.hotel.controller;

import com.heqing.hotel.model.VisitorRecord;
import com.heqing.hotel.service.VisitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visitor")
public class VisitorController {

    @Autowired
    private VisitorService visitorService;

    @PostMapping("/record")
    public ResponseEntity<?> createVisitorRecord(@RequestBody VisitorRecord record) {
        try {
            VisitorRecord savedRecord = visitorService.createVisitorRecord(record);
            return ResponseEntity.ok(savedRecord);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("创建访客记录失败: " + e.getMessage());
        }
    }

    @GetMapping("/record/{id}")
    public ResponseEntity<?> getVisitorRecord(@PathVariable Long id) {
        try {
            VisitorRecord record = visitorService.getVisitorRecord(id);
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取访客记录失败: " + e.getMessage());
        }
    }

    @GetMapping("/records")
    public ResponseEntity<?> getVisitorRecords() {
        try {
            List<VisitorRecord> records = visitorService.getAllVisitorRecords();
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取访客记录列表失败: " + e.getMessage());
        }
    }

    @PutMapping("/record/{id}/cancel")
    public ResponseEntity<?> cancelVisitorRecord(@PathVariable Long id) {
        try {
            visitorService.cancelVisitorRecord(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("取消访客记录失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getVisitorStatistics() {
        try {
            return ResponseEntity.ok(visitorService.getVisitorStatistics());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取访客统计信息失败: " + e.getMessage());
        }
    }
}