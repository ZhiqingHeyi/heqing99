package com.hotel.dto;

import lombok.Data;

@Data
public class StaffRegistrationDto {
    private String username;
    private String phone;
    private String password; // 前端传入原始密码
    private String email;
    private String realName;
    private String inviteCode;
    // 注意：不需要传 role，role 由后端根据 inviteCode 决定
} 