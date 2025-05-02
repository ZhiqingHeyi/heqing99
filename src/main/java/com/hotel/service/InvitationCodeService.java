package com.hotel.service;

import com.hotel.entity.InvitationCode;
import com.hotel.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface InvitationCodeService {
    InvitationCode createInvitationCode(User.UserRole role, Integer maxUses, LocalDateTime expiryDate);
    
    void validateInvitationCode(String code, User.UserRole role);
    
    void useInvitationCode(String code);
    
    List<InvitationCode> getValidInvitationCodes();
    
    List<InvitationCode> getInvitationCodesByRole(User.UserRole role);
    
    void disableInvitationCode(Long id);
    
    InvitationCode validateCodeOnly(String code);

    /**
     * 获取所有邀请码（包括有效、已使用、已过期）
     * @return 所有邀请码的列表
     */
    List<InvitationCode> getAllInvitationCodes();
}