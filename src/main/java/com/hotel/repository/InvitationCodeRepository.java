package com.hotel.repository;

import com.hotel.entity.InvitationCode;
import com.hotel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationCodeRepository extends JpaRepository<InvitationCode, Long> {
    Optional<InvitationCode> findByCode(String code);
    
    List<InvitationCode> findByRole(User.UserRole role);
    
    @Query("SELECT ic FROM InvitationCode ic WHERE ic.enabled = true AND ic.expiryDate > :now AND ic.currentUses < ic.maxUses")
    List<InvitationCode> findValidInvitationCodes(LocalDateTime now);
}