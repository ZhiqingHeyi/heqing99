package com.hotel.controller;

import com.hotel.entity.MemberLevel;
import com.hotel.entity.User;
import com.hotel.service.MemberLevelChangeRecordService;
import com.hotel.service.PointsExpiryService;
import com.hotel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 会员批量操作控制器
 */
@RestController
@RequestMapping("/api/membership-batch")
public class MembershipBatchController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private MemberLevelChangeRecordService memberLevelChangeRecordService;
    
    @Autowired
    private PointsExpiryService pointsExpiryService;

    /**
     * 批量修改会员等级
     */
    @PostMapping("/update-levels")
    public ResponseEntity<?> batchUpdateMemberLevels(
            @RequestParam List<Long> userIds,
            @RequestParam MemberLevel newLevel,
            @RequestParam String reason) {
        try {
            List<Map<String, Object>> results = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            
            for (Long userId : userIds) {
                try {
                    User user = userService.getUserById(userId);
                    MemberLevel oldLevel = user.getMemberLevel();
                    
                    // 如果等级没变，跳过
                    if (oldLevel == newLevel) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("userId", userId);
                        result.put("status", "skipped");
                        result.put("message", "会员等级未变化");
                        results.add(result);
                        continue;
                    }
                    
                    // 创建等级变更记录
                    memberLevelChangeRecordService.createChangeRecord(user, oldLevel, newLevel, reason);
                    
                    // 更新用户等级
                    user.setMemberLevel(newLevel);
                    userService.updateUser(user);
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("userId", userId);
                    result.put("username", user.getUsername());
                    result.put("name", user.getName());
                    result.put("oldLevel", oldLevel.name());
                    result.put("newLevel", newLevel.name());
                    result.put("status", "success");
                    results.add(result);
                } catch (Exception e) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("userId", userId);
                    result.put("status", "error");
                    result.put("message", e.getMessage());
                    results.add(result);
                    errors.add("用户ID " + userId + ": " + e.getMessage());
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", errors.isEmpty());
            response.put("results", results);
            response.put("totalProcessed", userIds.size());
            response.put("successCount", results.stream().filter(r -> "success".equals(r.get("status"))).count());
            response.put("skipCount", results.stream().filter(r -> "skipped".equals(r.get("status"))).count());
            response.put("errorCount", errors.size());
            
            if (!errors.isEmpty()) {
                response.put("errors", errors);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "批量更新会员等级失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 批量添加积分
     */
    @PostMapping("/add-points")
    public ResponseEntity<?> batchAddPoints(
            @RequestParam List<Long> userIds,
            @RequestParam Integer points,
            @RequestParam String source) {
        try {
            List<Map<String, Object>> results = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            
            for (Long userId : userIds) {
                try {
                    User user = pointsExpiryService.addPointsWithExpiry(userId, points, source);
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("userId", userId);
                    result.put("username", user.getUsername());
                    result.put("name", user.getName());
                    result.put("pointsAdded", points);
                    result.put("newTotalPoints", user.getPoints());
                    result.put("status", "success");
                    results.add(result);
                } catch (Exception e) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("userId", userId);
                    result.put("status", "error");
                    result.put("message", e.getMessage());
                    results.add(result);
                    errors.add("用户ID " + userId + ": " + e.getMessage());
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", errors.isEmpty());
            response.put("results", results);
            response.put("totalProcessed", userIds.size());
            response.put("successCount", results.stream().filter(r -> "success".equals(r.get("status"))).count());
            response.put("errorCount", errors.size());
            
            if (!errors.isEmpty()) {
                response.put("errors", errors);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "批量添加积分失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 按会员等级搜索用户
     */
    @GetMapping("/search-by-level")
    public ResponseEntity<?> searchUsersByMemberLevel(@RequestParam MemberLevel level) {
        try {
            List<User> allUsers = userService.getAllUsers();
            
            List<User> filteredUsers = allUsers.stream()
                    .filter(user -> user.getRole() == User.UserRole.CUSTOMER)
                    .filter(user -> user.getMemberLevel() == level)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("level", level.name());
            response.put("displayName", level.getDisplayName());
            response.put("count", filteredUsers.size());
            response.put("users", filteredUsers);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "按会员等级搜索失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 按积分范围搜索会员
     */
    @GetMapping("/search-by-points")
    public ResponseEntity<?> searchUsersByPointsRange(
            @RequestParam(required = false) Integer minPoints,
            @RequestParam(required = false) Integer maxPoints) {
        try {
            List<User> allUsers = userService.getAllUsers();
            
            List<User> filteredUsers = allUsers.stream()
                    .filter(user -> user.getRole() == User.UserRole.CUSTOMER)
                    .filter(user -> {
                        boolean matchesMin = minPoints == null || user.getPoints() >= minPoints;
                        boolean matchesMax = maxPoints == null || user.getPoints() <= maxPoints;
                        return matchesMin && matchesMax;
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("minPoints", minPoints);
            response.put("maxPoints", maxPoints);
            response.put("count", filteredUsers.size());
            response.put("users", filteredUsers);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "按积分范围搜索失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 按消费金额范围搜索会员
     */
    @GetMapping("/search-by-spending")
    public ResponseEntity<?> searchUsersBySpendingRange(
            @RequestParam(required = false) Double minSpending,
            @RequestParam(required = false) Double maxSpending) {
        try {
            List<User> allUsers = userService.getAllUsers();
            
            List<User> filteredUsers = allUsers.stream()
                    .filter(user -> user.getRole() == User.UserRole.CUSTOMER)
                    .filter(user -> {
                        boolean matchesMin = minSpending == null || 
                                user.getTotalSpent().doubleValue() >= minSpending;
                        boolean matchesMax = maxSpending == null || 
                                user.getTotalSpent().doubleValue() <= maxSpending;
                        return matchesMin && matchesMax;
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("minSpending", minSpending);
            response.put("maxSpending", maxSpending);
            response.put("count", filteredUsers.size());
            response.put("users", filteredUsers);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "按消费金额搜索失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 组合条件搜索会员
     */
    @GetMapping("/advanced-search")
    public ResponseEntity<?> advancedSearch(
            @RequestParam(required = false) MemberLevel level,
            @RequestParam(required = false) Integer minPoints,
            @RequestParam(required = false) Integer maxPoints,
            @RequestParam(required = false) Double minSpending,
            @RequestParam(required = false) Double maxSpending) {
        try {
            List<User> allUsers = userService.getAllUsers();
            
            List<User> filteredUsers = allUsers.stream()
                    .filter(user -> user.getRole() == User.UserRole.CUSTOMER)
                    .filter(user -> level == null || user.getMemberLevel() == level)
                    .filter(user -> minPoints == null || user.getPoints() >= minPoints)
                    .filter(user -> maxPoints == null || user.getPoints() <= maxPoints)
                    .filter(user -> minSpending == null || 
                            user.getTotalSpent().doubleValue() >= minSpending)
                    .filter(user -> maxSpending == null || 
                            user.getTotalSpent().doubleValue() <= maxSpending)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", filteredUsers.size());
            response.put("users", filteredUsers);
            
            Map<String, Object> criteria = new HashMap<>();
            if (level != null) criteria.put("level", level.name());
            if (minPoints != null) criteria.put("minPoints", minPoints);
            if (maxPoints != null) criteria.put("maxPoints", maxPoints);
            if (minSpending != null) criteria.put("minSpending", minSpending);
            if (maxSpending != null) criteria.put("maxSpending", maxSpending);
            
            response.put("searchCriteria", criteria);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "高级搜索失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 