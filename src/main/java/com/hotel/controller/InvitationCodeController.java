package com.hotel.controller;

import com.hotel.entity.InvitationCode;
import com.hotel.entity.User;
import com.hotel.service.InvitationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invitation-codes")
@PreAuthorize("hasRole('ADMIN')")
public class InvitationCodeController {

    @Autowired
    private InvitationCodeService invitationCodeService;

    @PostMapping
    public ResponseEntity<InvitationCode> createInvitationCode(@RequestBody Map<String, Object> request) {
        User.UserRole role = User.UserRole.valueOf((String) request.get("role"));
        Integer maxUses = (Integer) request.get("maxUses");
        LocalDateTime expiryDate = LocalDateTime.parse((String) request.get("expiryDate"));
        
        InvitationCode invitationCode = invitationCodeService.createInvitationCode(role, maxUses, expiryDate);
        return ResponseEntity.ok(invitationCode);
    }

    @GetMapping
    public ResponseEntity<List<InvitationCode>> getValidInvitationCodes() {
        return ResponseEntity.ok(invitationCodeService.getValidInvitationCodes());
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<InvitationCode>> getInvitationCodesByRole(@PathVariable User.UserRole role) {
        return ResponseEntity.ok(invitationCodeService.getInvitationCodesByRole(role));
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<?> disableInvitationCode(@PathVariable Long id) {
        invitationCodeService.disableInvitationCode(id);
        return ResponseEntity.ok().build();
    }
}