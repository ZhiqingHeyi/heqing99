package com.hotel.controller;

import com.hotel.dto.MembershipDTO;
import com.hotel.entity.MemberLevel;
import com.hotel.entity.User;
import com.hotel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/membership")
public class MembershipController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户会员信息
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getMembershipInfo(@PathVariable Long userId) {
        try {
            System.out.println("接收到获取会员信息请求, userId: " + userId);
            User user = userService.getUserById(userId);
            MembershipDTO memberInfo = MembershipDTO.fromUser(user);
            
            System.out.println("返回会员信息: " + memberInfo);
            return ResponseEntity.ok(memberInfo);
        } catch (Exception e) {
            System.err.println("获取会员信息失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取会员信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 更新会员等级
     */
    @PostMapping("/{userId}/update-level")
    public ResponseEntity<?> updateMemberLevel(@PathVariable Long userId) {
        try {
            System.out.println("接收到更新会员等级请求, userId: " + userId);
            User user = userService.updateMemberLevel(userId);
            MembershipDTO memberInfo = MembershipDTO.fromUser(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "会员等级已更新");
            response.put("data", memberInfo);
            
            System.out.println("会员等级更新成功: " + user.getMemberLevel());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("更新会员等级失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新会员等级失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 添加积分
     */
    @PostMapping("/{userId}/add-points")
    public ResponseEntity<?> addPoints(
            @PathVariable Long userId, 
            @RequestParam int points) {
        try {
            System.out.println("接收到添加积分请求, userId: " + userId + ", points: " + points);
            User user = userService.addPoints(userId, points);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "积分已添加");
            response.put("points", user.getPoints());
            
            System.out.println("积分添加成功, 当前积分: " + user.getPoints());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("添加积分失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "添加积分失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 使用积分
     */
    @PostMapping("/{userId}/redeem-points")
    public ResponseEntity<?> redeemPoints(
            @PathVariable Long userId, 
            @RequestParam int points) {
        try {
            System.out.println("接收到使用积分请求, userId: " + userId + ", points: " + points);
            User user = userService.redeemPoints(userId, points);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "积分已使用");
            response.put("points", user.getPoints());
            response.put("redeemedAmount", points / 10.0); // 100积分=10元
            
            System.out.println("积分使用成功, 当前积分: " + user.getPoints());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("使用积分失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "使用积分失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 记录消费
     */
    @PostMapping("/{userId}/add-spending")
    public ResponseEntity<?> addSpending(
            @PathVariable Long userId, 
            @RequestParam BigDecimal amount) {
        try {
            System.out.println("接收到添加消费记录请求, userId: " + userId + ", amount: " + amount);
            User user = userService.addSpending(userId, amount);
            MembershipDTO memberInfo = MembershipDTO.fromUser(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "消费记录已添加");
            response.put("data", memberInfo);
            
            System.out.println("消费记录添加成功, 当前累计消费: " + user.getTotalSpent() + ", 会员等级: " + user.getMemberLevel());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("添加消费记录失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "添加消费记录失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取会员折扣
     */
    @GetMapping("/{userId}/discount")
    public ResponseEntity<?> getDiscount(@PathVariable Long userId) {
        try {
            System.out.println("接收到获取折扣请求, userId: " + userId);
            BigDecimal discount = userService.getDiscountByUserId(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("discount", discount);
            
            System.out.println("返回折扣信息: " + discount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("获取折扣失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取折扣失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 计算会员等级
     */
    @GetMapping("/levels")
    public ResponseEntity<?> getMembershipLevels() {
        try {
            System.out.println("接收到获取会员等级列表请求");
            
            List<Map<String, Object>> levels = new ArrayList<>();
            
            // 从枚举中获取会员等级信息
            for (MemberLevel level : MemberLevel.values()) {
                if (level == MemberLevel.REGULAR) {
                    continue; // 跳过普通用户
                }
                
                Map<String, Object> levelInfo = new HashMap<>();
                levelInfo.put("name", level.getDisplayName());
                levelInfo.put("code", level.name());
                levelInfo.put("discount", level.getDiscount());
                
                // 设置阈值
                int threshold = 0;
                switch (level) {
                    case BRONZE:
                        threshold = 1500;
                        levelInfo.put("pointsRate", 100);
                        break;
                    case SILVER:
                        threshold = 5000;
                        levelInfo.put("pointsRate", 120);
                        break;
                    case GOLD:
                        threshold = 10000;
                        levelInfo.put("pointsRate", 150);
                        break;
                    case DIAMOND:
                        threshold = 30000;
                        levelInfo.put("pointsRate", 200);
                        break;
                }
                
                levelInfo.put("threshold", threshold);
                levels.add(levelInfo);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("levels", levels);
            
            System.out.println("返回会员等级列表成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("获取会员等级信息失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取会员等级信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 