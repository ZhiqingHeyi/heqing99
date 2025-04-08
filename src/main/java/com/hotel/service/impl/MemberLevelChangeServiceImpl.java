package com.hotel.service.impl;

import com.hotel.entity.MemberLevelChange;
import com.hotel.repository.MemberLevelChangeRepository;
import com.hotel.service.MemberLevelChangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberLevelChangeServiceImpl implements MemberLevelChangeService {

    private final MemberLevelChangeRepository memberLevelChangeRepository;

    @Override
    @Transactional
    public MemberLevelChange recordLevelChange(Long userId, String previousLevel, String newLevel, String changeType, String changeReason) {
        MemberLevelChange levelChange = new MemberLevelChange();
        levelChange.setUserId(userId);
        levelChange.setPreviousLevel(previousLevel);
        levelChange.setNewLevel(newLevel);
        levelChange.setChangeType(changeType);
        levelChange.setChangeReason(changeReason);
        
        return memberLevelChangeRepository.save(levelChange);
    }

    @Override
    public List<MemberLevelChange> getUserLevelChangeHistory(Long userId) {
        return memberLevelChangeRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    @Override
    public MemberLevelChange getLatestLevelChange(Long userId) {
        return memberLevelChangeRepository.findFirstByUserIdOrderByCreateTimeDesc(userId)
                .orElse(null);
    }
}