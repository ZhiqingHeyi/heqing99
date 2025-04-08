package com.hotel.service.impl;

import com.hotel.entity.MemberLevelChange;
import com.hotel.repository.MemberLevelChangeRepository;
import com.hotel.service.MemberLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberLevelServiceImpl implements MemberLevelService {

    @Autowired
    private MemberLevelChangeRepository memberLevelChangeRepository;

    @Override
    @Transactional
    public MemberLevelChange changeMemberLevel(MemberLevelChange change) {
        // 设置当前用户ID
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        change.setUserId(Long.parseLong(username));
        return memberLevelChangeRepository.save(change);
    }

    @Override
    public Page<MemberLevelChange> getMemberLevelHistory(Pageable pageable) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(username);
        return memberLevelChangeRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Boolean checkAndHandleUpgrade() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(username);
        // TODO: 实现具体的升级逻辑
        return false;
    }
} 