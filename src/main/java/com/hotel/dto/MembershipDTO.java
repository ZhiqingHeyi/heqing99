package com.hotel.dto;

import com.hotel.entity.MemberLevel;
import com.hotel.entity.User;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 会员信息数据传输对象
 */
@Data
public class MembershipDTO {
    private Long userId;
    private String username;
    private String name;
    private String memberLevel;
    private String memberLevelCode;
    private Integer points;
    private BigDecimal totalSpent;
    private Double discount;
    private BigDecimal nextLevelSpend;
    private String nextLevel;
    private Integer pointsRate;
    
    /**
     * 从用户实体构建会员DTO
     */
    public static MembershipDTO fromUser(User user) {
        if (user == null) {
            return null;
        }
        
        MembershipDTO dto = new MembershipDTO();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        
        // 处理会员等级
        MemberLevel level = user.getMemberLevel();
        dto.setMemberLevel(level.getDisplayName());
        dto.setMemberLevelCode(level.name());
        dto.setDiscount(level.getDiscount());
        
        dto.setPoints(user.getPoints());
        dto.setTotalSpent(user.getTotalSpent());
        
        // 设置积分率
        switch (level) {
            case REGULAR:
                dto.setPointsRate(0);
                break;
            case BRONZE:
                dto.setPointsRate(100);
                break;
            case SILVER:
                dto.setPointsRate(120);
                break;
            case GOLD:
                dto.setPointsRate(150);
                break;
            case DIAMOND:
                dto.setPointsRate(200);
                break;
        }
        
        // 计算下一等级所需消费金额
        switch (level) {
            case REGULAR:
                dto.setNextLevelSpend(BigDecimal.valueOf(1500).subtract(user.getTotalSpent()));
                dto.setNextLevel("铜牌会员");
                break;
            case BRONZE:
                dto.setNextLevelSpend(BigDecimal.valueOf(5000).subtract(user.getTotalSpent()));
                dto.setNextLevel("银牌会员");
                break;
            case SILVER:
                dto.setNextLevelSpend(BigDecimal.valueOf(10000).subtract(user.getTotalSpent()));
                dto.setNextLevel("金牌会员");
                break;
            case GOLD:
                dto.setNextLevelSpend(BigDecimal.valueOf(30000).subtract(user.getTotalSpent()));
                dto.setNextLevel("钻石会员");
                break;
            case DIAMOND:
                dto.setNextLevelSpend(BigDecimal.ZERO);
                dto.setNextLevel("已达最高等级");
                break;
        }
        
        // 确保下一等级所需消费不为负数
        if (dto.getNextLevelSpend().compareTo(BigDecimal.ZERO) < 0) {
            dto.setNextLevelSpend(BigDecimal.ZERO);
        }
        
        return dto;
    }
} 