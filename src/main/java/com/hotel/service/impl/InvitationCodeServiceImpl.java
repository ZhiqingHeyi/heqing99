package com.hotel.service.impl;

import com.hotel.entity.InvitationCode;
import com.hotel.entity.User;
import com.hotel.repository.InvitationCodeRepository;
import com.hotel.service.InvitationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@Transactional
public class InvitationCodeServiceImpl implements InvitationCodeService {

    @Autowired
    private InvitationCodeRepository invitationCodeRepository;

    @Override
    public InvitationCode createInvitationCode(User.UserRole role, Integer maxUses, LocalDateTime expiryDate) {
        InvitationCode invitationCode = new InvitationCode();
        invitationCode.setCode(generateUniqueCode());
        invitationCode.setRole(role);
        invitationCode.setMaxUses(maxUses);
        invitationCode.setExpiryDate(expiryDate);
        return invitationCodeRepository.save(invitationCode);
    }

    @Override
    public void validateInvitationCode(String code, User.UserRole role) {
        InvitationCode invitationCode = invitationCodeRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("邀请码无效"));

        if (!invitationCode.getEnabled()) {
            throw new RuntimeException("邀请码已被禁用");
        }

        if (invitationCode.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("邀请码已过期");
        }

        if (invitationCode.getCurrentUses() >= invitationCode.getMaxUses()) {
            throw new RuntimeException("邀请码已达到最大使用次数");
        }

        if (invitationCode.getRole() != role) {
            throw new RuntimeException("邀请码与角色不匹配");
        }
    }

    @Override
    public void useInvitationCode(String code) {
        InvitationCode invitationCode = invitationCodeRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("邀请码无效"));
        invitationCode.setCurrentUses(invitationCode.getCurrentUses() + 1);
        invitationCodeRepository.save(invitationCode);
    }

    @Override
    public List<InvitationCode> getValidInvitationCodes() {
        return invitationCodeRepository.findValidInvitationCodes(LocalDateTime.now());
    }

    @Override
    public List<InvitationCode> getInvitationCodesByRole(User.UserRole role) {
        return invitationCodeRepository.findByRole(role);
    }

    @Override
    public void disableInvitationCode(Long id) {
        InvitationCode invitationCode = invitationCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("邀请码不存在"));
        invitationCode.setEnabled(false);
        invitationCodeRepository.save(invitationCode);
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = generateRandomCode();
        } while (invitationCodeRepository.findByCode(code).isPresent());
        return code;
    }

    private String generateRandomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }
}