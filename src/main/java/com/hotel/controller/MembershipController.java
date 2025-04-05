package com.hotel.controller;

import com.hotel.dto.MembershipDTO;
import com.hotel.entity.User;
import com.hotel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
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
            Map<String, Object> bronzeLevel = new HashMap<>();
            bronzeLevel.put("name", "铜牌会员");
            bronzeLevel.put("code", "BRONZE");
            bronzeLevel.put("threshold", 1500);
            bronzeLevel.put("discount", 0.98);
            bronzeLevel.put("pointsRate", 100);
            
            Map<String, Object> silverLevel = new HashMap<>();
            silverLevel.put("name", "银牌会员");
            silverLevel.put("code", "SILVER");
            silverLevel.put("threshold", 5000);
            silverLevel.put("discount", 0.95);
            silverLevel.put("pointsRate", 120);
            
            Map<String, Object> goldLevel = new HashMap<>();
            goldLevel.put("name", "金牌会员");
            goldLevel.put("code", "GOLD");
            goldLevel.put("threshold", 10000);
            goldLevel.put("discount", 0.9);
            goldLevel.put("pointsRate", 150);
            
            Map<String, Object> diamondLevel = new HashMap<>();
            diamondLevel.put("name", "钻石会员");
            diamondLevel.put("code", "DIAMOND");
            diamondLevel.put("threshold", 30000);
            diamondLevel.put("discount", 0.85);
            diamondLevel.put("pointsRate", 200);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("levels", new Object[]{bronzeLevel, silverLevel, goldLevel, diamondLevel});
            
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