package com.hotel.dto;

import com.hotel.entity.MemberLevel;
import com.hotel.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 数据传输对象 (DTO) 用于用户列表，不包含密码等敏感信息
 */
@Data
@NoArgsConstructor
public class UserDTO {

    private Long id;
    private String username;
    private String name;
    private String phone;
    private String email;
    private String gender;
    private LocalDate birthday;
    private String role;
    private String status;
    private String memberLevel;
    private Integer points;
    private BigDecimal totalSpent;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 从 User 实体转换的构造函数 (可选，但方便)
     */
    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.phone = user.getPhone();
        this.email = user.getEmail();
        this.gender = user.getGender();
        this.birthday = user.getBirthday();
        this.role = user.getRole() != null ? user.getRole().name() : null;
        this.status = user.getStatus();
        this.memberLevel = user.getMemberLevel() != null ? user.getMemberLevel().name() : null;
        this.points = user.getPoints();
        this.totalSpent = user.getTotalSpent();
        this.createTime = user.getCreateTime();
        this.updateTime = user.getUpdateTime();
    }

    /**
     * 静态工厂方法用于转换 (另一种方式)
     */
    public static UserDTO fromEntity(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setGender(user.getGender());
        dto.setBirthday(user.getBirthday());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setStatus(user.getStatus());
        dto.setMemberLevel(user.getMemberLevel() != null ? user.getMemberLevel().name() : null);
        dto.setPoints(user.getPoints());
        dto.setTotalSpent(user.getTotalSpent());
        dto.setCreateTime(user.getCreateTime());
        dto.setUpdateTime(user.getUpdateTime());
        return dto;
    }
} 