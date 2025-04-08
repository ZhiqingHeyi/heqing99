package com.hotel.repository;

import com.hotel.entity.MemberLevelChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberLevelChangeRepository extends JpaRepository<MemberLevelChange, Long> {
    
    List<MemberLevelChange> findByUserIdOrderByCreateTimeDesc(Long userId);
    
    Optional<MemberLevelChange> findFirstByUserIdOrderByCreateTimeDesc(Long userId);
} 