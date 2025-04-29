package com.hotel.controller;

import com.hotel.dto.MembershipDTO;
import com.hotel.dto.PointsHistoryDTO;
import com.hotel.entity.MemberLevel;
import com.hotel.entity.User;
import com.hotel.service.PointsHistoryService;
import com.hotel.service.UserService;
import com.hotel.util.UserContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/membership")
public class MembershipController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private PointsHistoryService pointsHistoryService;

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
     * 获取当前用户的会员信息
     */
    @GetMapping("/info")
    public ResponseEntity<?> getCurrentUserMembershipInfo() {
        try {
            // 获取当前登录用户ID
            Long userId = UserContextHolder.getCurrentUserId();
            User user = userService.getUserById(userId);
            MembershipDTO memberInfo = MembershipDTO.fromUser(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", memberInfo);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
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
     * 获取所有会员等级及其权益
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
                levelInfo.put("id", level.name().toLowerCase());
                levelInfo.put("name", level.getDisplayName());
                levelInfo.put("discount", level.getDiscount());
                
                // 设置积分率和阈值
                int threshold = 0;
                double pointRate = 0;
                String nextLevel = null;
                Integer nextLevelThreshold = null;
                List<String> privileges = new ArrayList<>();
                
                switch (level) {
                    case BRONZE:
                        threshold = 1500;
                        pointRate = 1.0;
                        nextLevel = "银牌会员";
                        nextLevelThreshold = 5000;
                        privileges.add("基础折扣9.8折");
                        privileges.add("积分兑换");
                        break;
                    case SILVER:
                        threshold = 5000;
                        pointRate = 1.2;
                        nextLevel = "金牌会员";
                        nextLevelThreshold = 10000;
                        privileges.add("折扣9.5折");
                        privileges.add("预订免押金");
                        privileges.add("生日礼遇");
                        break;
                    case GOLD:
                        threshold = 10000;
                        pointRate = 1.5;
                        nextLevel = "钻石会员";
                        nextLevelThreshold = 30000;
                        privileges.add("折扣9折");
                        privileges.add("预订免押金");
                        privileges.add("生日礼遇");
                        privileges.add("专属客服");
                        break;
                    case DIAMOND:
                        threshold = 30000;
                        pointRate = 2.0;
                        nextLevel = null;
                        nextLevelThreshold = null;
                        privileges.add("折扣8.5折");
                        privileges.add("预订免押金");
                        privileges.add("生日礼遇");
                        privileges.add("专属客服");
                        privileges.add("机场接送");
                        privileges.add("免费升级房型");
                        break;
                }
                
                levelInfo.put("pointRate", pointRate);
                levelInfo.put("threshold", threshold);
                levelInfo.put("nextLevel", nextLevel);
                levelInfo.put("nextLevelThreshold", nextLevelThreshold);
                levelInfo.put("privileges", privileges);
                
                levels.add(levelInfo);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", levels);
            
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
    
    /**
     * 获取积分变动历史
     */
    @GetMapping("/points/history")
    public ResponseEntity<?> getPointsHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            // 获取当前登录用户ID
            Long userId = UserContextHolder.getCurrentUserId();
            
            // 构建分页对象
            org.springframework.data.domain.Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
            
            // 获取积分历史
            Page<PointsHistoryDTO> historyPage = pointsHistoryService.getUserPointsHistory(userId, pageable);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("total", historyPage.getTotalElements());
            responseData.put("list", historyPage.getContent());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", responseData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取积分历史失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 根据会员等级计算折扣后的价格
     */
    @PostMapping("/calculate-discount")
    public ResponseEntity<?> calculateDiscount(
            @RequestParam BigDecimal originalPrice) {
        try {
            // 获取当前登录用户ID
            Long userId = UserContextHolder.getCurrentUserId();
            User user = userService.getUserById(userId);
            
            // 获取折扣率
            double discountRate = user.getDiscountRate();
            
            // 计算折扣价格
            BigDecimal discountPrice = originalPrice.multiply(BigDecimal.valueOf(discountRate)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal discountAmount = originalPrice.subtract(discountPrice).setScale(2, RoundingMode.HALF_UP);
            
            // 计算预计获得的积分
            int pointsRate = 0;
            switch (user.getMemberLevel()) {
                case BRONZE:
                    pointsRate = 100;
                    break;
                case SILVER:
                    pointsRate = 120;
                    break;
                case GOLD:
                    pointsRate = 150;
                    break;
                case DIAMOND:
                    pointsRate = 200;
                    break;
                default:
                    pointsRate = 0;
            }
            
            int estimatedPoints = discountPrice.multiply(BigDecimal.valueOf(pointsRate))
                    .divide(BigDecimal.valueOf(100), RoundingMode.FLOOR)
                    .intValue();
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("originalPrice", originalPrice);
            responseData.put("discountRate", discountRate);
            responseData.put("discountPrice", discountPrice);
            responseData.put("discountAmount", discountAmount);
            responseData.put("memberLevel", user.getMemberLevel().getDisplayName());
            responseData.put("estimatedPoints", estimatedPoints);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "计算成功");
            response.put("data", responseData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "计算折扣价格失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 