package com.hotel.controller;

import com.hotel.entity.InvitationCode;
import com.hotel.entity.User;
import com.hotel.service.InvitationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invitation-codes")
public class InvitationCodeController {

    @Autowired
    private InvitationCodeService invitationCodeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InvitationCode> createInvitationCode(@RequestBody Map<String, Object> request) {
        User.UserRole role = User.UserRole.valueOf((String) request.get("role"));
        Integer maxUses = (Integer) request.get("maxUses");
        
        String expiryDateStr = (String) request.get("expiryDate");
        Instant instant = Instant.parse(expiryDateStr);
        LocalDateTime expiryDate = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        
        InvitationCode invitationCode = invitationCodeService.createInvitationCode(role, maxUses, expiryDate);
        return ResponseEntity.ok(invitationCode);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InvitationCode>> getValidInvitationCodes() {
        return ResponseEntity.ok(invitationCodeService.getValidInvitationCodes());
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InvitationCode>> getInvitationCodesByRole(@PathVariable User.UserRole role) {
        return ResponseEntity.ok(invitationCodeService.getInvitationCodesByRole(role));
    }

    @PutMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> disableInvitationCode(@PathVariable Long id) {
        invitationCodeService.disableInvitationCode(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/validate/{code}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> validateCode(@PathVariable String code) {
        try {
            InvitationCode validCode = invitationCodeService.validateCodeOnly(code);
            return ResponseEntity.ok(Collections.singletonMap("role", validCode.getRole().name()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("message", e.getMessage()));
        }
    }
}