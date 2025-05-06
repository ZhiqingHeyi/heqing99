package com.hotel.controller;

import com.hotel.entity.User;
import com.hotel.entity.Visitor;
import com.hotel.entity.VisitorRecord;
import com.hotel.service.UserService;
import com.hotel.service.VisitorRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 访客管理控制器
 */
@RestController
@RequestMapping("/api/visitor")
public class VisitorController {

    @Autowired
    private VisitorRecordService visitorRecordService;

    @Autowired
    private UserService userService;

    // ========== 访客记录相关接口 ==========

    /**
     * 创建访客记录
     */
    @PostMapping("/record")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<?> createVisitorRecord(@RequestBody Map<String, Object> payload) {
        try {
            VisitorRecord visitorRecord = new VisitorRecord();
            visitorRecord.setVisitorName((String) payload.get("visitorName"));
            visitorRecord.setPhone((String) payload.get("phone"));
            visitorRecord.setIdCard((String) payload.get("idCard"));
            visitorRecord.setPurpose((String) payload.get("purpose"));
            // 注意：duration 字段可能需要处理，如果后端需要的话

            Long visitedUserId = ((Number) payload.get("visitedUserId")).longValue();
            if (visitedUserId == null) {
                 return ResponseEntity.badRequest().body("Missing visitedUserId");
            }
            
            // 调用服务层方法，传入 record 和 userId
            VisitorRecord createdRecord = visitorRecordService.createVisitorRecord(visitorRecord, visitedUserId);
            return ResponseEntity.ok(createdRecord);
            
        } catch (RuntimeException e) {
            // 捕获服务层可能抛出的异常（例如用户未找到）
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating visitor record: " + e.getMessage());
        }
    }

    /**
     * 更新访客记录
     */
    @PutMapping("/record")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<VisitorRecord> updateVisitorRecord(@RequestBody VisitorRecord visitorRecord) {
        // 考虑：更新是否需要处理 visitedUser？
        return ResponseEntity.ok(visitorRecordService.updateVisitorRecord(visitorRecord));
    }

    /**
     * 登记访客离开
     */
    @PutMapping("/record/{id}/leave")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<VisitorRecord> recordVisitorRecordLeave(@PathVariable Long id) {
        return ResponseEntity.ok(visitorRecordService.recordVisitorLeave(id, LocalDateTime.now()));
    }

    /**
     * 取消访客记录
     */
    @PutMapping("/record/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Void> cancelVisitorRecord(@PathVariable Long id) {
        visitorRecordService.cancelVisitorRecord(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取访客记录详情
     */
    @GetMapping("/record/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'CUSTOMER')")
    public ResponseEntity<VisitorRecord> getVisitorRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(visitorRecordService.getVisitorRecordById(id));
    }

    /**
     * 获取当前用户的访客记录
     */
    @GetMapping("/record/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<VisitorRecord>> getMyVisitorRecords(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        return ResponseEntity.ok(visitorRecordService.getVisitorRecordsByVisitedUser(user));
    }

    /**
     * 获取当前用户的在访访客
     */
    @GetMapping("/record/my/current")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<VisitorRecord>> getMyCurrentVisitors(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        return ResponseEntity.ok(visitorRecordService.getCurrentVisitorsByUser(user));
    }

    /**
     * 获取指定用户的访客记录
     */
    @GetMapping("/record/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<List<VisitorRecord>> getVisitorRecordsByUserId(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(visitorRecordService.getVisitorRecordsByVisitedUser(user));
    }

    /**
     * 获取指定状态的访客记录
     */
    @GetMapping("/record/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<List<VisitorRecord>> getVisitorRecordsByStatus(@PathVariable VisitorRecord.VisitStatus status) {
        return ResponseEntity.ok(visitorRecordService.getVisitorRecordsByStatus(status));
    }

    /**
     * 获取指定时间范围内的访客记录
     */
    @GetMapping("/record/time")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<List<VisitorRecord>> getVisitorRecordsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(visitorRecordService.getVisitorRecordsByTimeRange(startTime, endTime));
    }

    /**
     * 获取当前在访的访客记录
     */
    @GetMapping("/record/current")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<List<VisitorRecord>> getCurrentVisitors() {
        return ResponseEntity.ok(visitorRecordService.getCurrentVisitors());
    }

    /**
     * 统计指定时间范围内的访客数量
     */
    @GetMapping("/record/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Long> countVisitorsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(visitorRecordService.countVisitorsByTimeRange(startTime, endTime));
    }

    /**
     * 搜索访客记录
     */
    @GetMapping("/record/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<List<VisitorRecord>> searchVisitorRecords(@RequestParam String keyword) {
        return ResponseEntity.ok(visitorRecordService.searchVisitorRecords(keyword));
    }

    /**
     * 批量更新访客记录状态
     */
    @PutMapping("/record/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Void> updateVisitorRecordsStatus(
            @RequestParam List<Long> recordIds,
            @RequestParam VisitorRecord.VisitStatus status) {
        visitorRecordService.updateVisitorRecordsStatus(recordIds, status);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取所有访客记录
     */
    @GetMapping("/record/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<List<VisitorRecord>> getAllVisitorRecords() {
        return ResponseEntity.ok(visitorRecordService.getAllVisitorRecords());
    }

    /**
     * 获取所有访客记录 (修改为分页和过滤)
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Page<VisitorRecord>> getAllVisitorRecordsPageable(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "visitTime,desc") String[] sort
    ) {
        Sort sortOrder = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Page<VisitorRecord> visitorPage = visitorRecordService.searchVisitorRecordsPageable(
                keyword, status, roomNumber, startTime, endTime, pageable
        );
        return ResponseEntity.ok(visitorPage);
    }

    // ========== 访客信息相关接口 ==========

    /**
     * 注册访客信息
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Visitor> registerVisitor(@RequestBody Visitor visitor) {
        return ResponseEntity.ok(visitorRecordService.registerVisitor(visitor));
    }

    /**
     * 根据ID查询访客信息
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'CUSTOMER')")
    public ResponseEntity<Visitor> getVisitorById(@PathVariable Long id) {
        return ResponseEntity.ok(visitorRecordService.getVisitorById(id));
    }

    /**
     * 根据姓名查询访客信息
     */
    @GetMapping("/name/{name}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<List<Visitor>> getVisitorsByName(@PathVariable String name) {
        return ResponseEntity.ok(visitorRecordService.getVisitorsByName(name));
    }

    /**
     * 根据手机号查询访客信息
     */
    @GetMapping("/phone/{phone}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Visitor> getVisitorByPhone(@PathVariable String phone) {
        return ResponseEntity.ok(visitorRecordService.getVisitorByPhone(phone));
    }

    /**
     * 获取今日访客列表
     */
    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<List<Visitor>> getTodayVisitors() {
        return ResponseEntity.ok(visitorRecordService.getTodayVisitors());
    }

    /**
     * 获取指定日期访客列表
     */
    @GetMapping("/date")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<List<Visitor>> getVisitorsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(visitorRecordService.getVisitorsByDate(startTime, endTime));
    }

    /**
     * 获取访问特定房间的访客列表
     */
    @GetMapping("/room/{roomNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<List<Visitor>> getVisitorsByRoom(@PathVariable String roomNumber) {
        return ResponseEntity.ok(visitorRecordService.getVisitorsByRoom(roomNumber));
    }

    /**
     * 更新访客信息
     */
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Visitor> updateVisitor(@RequestBody Visitor visitor) {
        return ResponseEntity.ok(visitorRecordService.updateVisitor(visitor));
    }

    /**
     * 删除访客记录
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> deleteVisitor(@PathVariable Long id) {
        visitorRecordService.deleteVisitor(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 统计今日访客数量
     */
    @GetMapping("/count/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Long> countTodayVisitors() {
        return ResponseEntity.ok(visitorRecordService.countTodayVisitors());
    }
} 