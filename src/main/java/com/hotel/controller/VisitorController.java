package com.hotel.controller;

import com.hotel.entity.User;
import com.hotel.entity.Visitor;
import com.hotel.entity.VisitorRecord;
import com.hotel.service.UserService;
import com.hotel.service.VisitorRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<VisitorRecord> createVisitorRecord(@RequestBody VisitorRecord visitorRecord) {
        return ResponseEntity.ok(visitorRecordService.createVisitorRecord(visitorRecord));
    }

    /**
     * 更新访客记录
     */
    @PutMapping("/record")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<VisitorRecord> updateVisitorRecord(@RequestBody VisitorRecord visitorRecord) {
        return ResponseEntity.ok(visitorRecordService.updateVisitorRecord(visitorRecord));
    }

    /**
     * 登记访客离开
     */
    @PutMapping("/record/{id}/leave")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<VisitorRecord> recordVisitorRecordLeave(@PathVariable Long id) {
        return ResponseEntity.ok(visitorRecordService.recordVisitorLeave(id, LocalDateTime.now()));
    }

    /**
     * 取消访客记录
     */
    @PutMapping("/record/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<Void> cancelVisitorRecord(@PathVariable Long id) {
        visitorRecordService.cancelVisitorRecord(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取访客记录详情
     */
    @GetMapping("/record/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION', 'USER')")
    public ResponseEntity<VisitorRecord> getVisitorRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(visitorRecordService.getVisitorRecordById(id));
    }

    /**
     * 获取当前用户的访客记录
     */
    @GetMapping("/record/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<VisitorRecord>> getMyVisitorRecords(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        return ResponseEntity.ok(visitorRecordService.getVisitorRecordsByVisitedUser(user));
    }

    /**
     * 获取当前用户的在访访客
     */
    @GetMapping("/record/my/current")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<VisitorRecord>> getMyCurrentVisitors(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        return ResponseEntity.ok(visitorRecordService.getCurrentVisitorsByUser(user));
    }

    /**
     * 获取指定用户的访客记录
     */
    @GetMapping("/record/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<List<VisitorRecord>> getVisitorRecordsByUserId(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(visitorRecordService.getVisitorRecordsByVisitedUser(user));
    }

    /**
     * 获取指定状态的访客记录
     */
    @GetMapping("/record/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<List<VisitorRecord>> getVisitorRecordsByStatus(@PathVariable VisitorRecord.VisitStatus status) {
        return ResponseEntity.ok(visitorRecordService.getVisitorRecordsByStatus(status));
    }

    /**
     * 获取指定时间范围内的访客记录
     */
    @GetMapping("/record/time")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<List<VisitorRecord>> getVisitorRecordsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(visitorRecordService.getVisitorRecordsByTimeRange(startTime, endTime));
    }

    /**
     * 获取当前在访的访客记录
     */
    @GetMapping("/record/current")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<List<VisitorRecord>> getCurrentVisitors() {
        return ResponseEntity.ok(visitorRecordService.getCurrentVisitors());
    }

    /**
     * 统计指定时间范围内的访客数量
     */
    @GetMapping("/record/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<Long> countVisitorsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(visitorRecordService.countVisitorsByTimeRange(startTime, endTime));
    }

    /**
     * 搜索访客记录
     */
    @GetMapping("/record/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<List<VisitorRecord>> searchVisitorRecords(@RequestParam String keyword) {
        return ResponseEntity.ok(visitorRecordService.searchVisitorRecords(keyword));
    }

    /**
     * 批量更新访客记录状态
     */
    @PutMapping("/record/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<List<VisitorRecord>> getAllVisitorRecords() {
        return ResponseEntity.ok(visitorRecordService.getAllVisitorRecords());
    }

    // ========== 访客信息相关接口 ==========

    /**
     * 注册访客信息
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<Visitor> registerVisitor(@RequestBody Visitor visitor) {
        return ResponseEntity.ok(visitorRecordService.registerVisitor(visitor));
    }

    /**
     * 获取所有访客列表
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<List<Visitor>> getAllVisitors() {
        return ResponseEntity.ok(visitorRecordService.getAllVisitors());
    }

    /**
     * 根据ID查询访客信息
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION', 'USER')")
    public ResponseEntity<Visitor> getVisitorById(@PathVariable Long id) {
        return ResponseEntity.ok(visitorRecordService.getVisitorById(id));
    }

    /**
     * 根据姓名查询访客信息
     */
    @GetMapping("/name/{name}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<List<Visitor>> getVisitorsByName(@PathVariable String name) {
        return ResponseEntity.ok(visitorRecordService.getVisitorsByName(name));
    }

    /**
     * 根据手机号查询访客信息
     */
    @GetMapping("/phone/{phone}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<Visitor> getVisitorByPhone(@PathVariable String phone) {
        return ResponseEntity.ok(visitorRecordService.getVisitorByPhone(phone));
    }

    /**
     * 获取今日访客列表
     */
    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<List<Visitor>> getTodayVisitors() {
        return ResponseEntity.ok(visitorRecordService.getTodayVisitors());
    }

    /**
     * 获取指定日期访客列表
     */
    @GetMapping("/date")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<List<Visitor>> getVisitorsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(visitorRecordService.getVisitorsByDate(startTime, endTime));
    }

    /**
     * 获取访问特定房间的访客列表
     */
    @GetMapping("/room/{roomNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<List<Visitor>> getVisitorsByRoom(@PathVariable String roomNumber) {
        return ResponseEntity.ok(visitorRecordService.getVisitorsByRoom(roomNumber));
    }

    /**
     * 更新访客信息
     */
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<Long> countTodayVisitors() {
        return ResponseEntity.ok(visitorRecordService.countTodayVisitors());
    }

    /**
     * 记录访客离开时间
     */
    @PutMapping("/{id}/leave")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<Visitor> recordVisitorLeave(@PathVariable Long id) {
        return ResponseEntity.ok(visitorRecordService.recordVisitorLeave(id));
    }
} 