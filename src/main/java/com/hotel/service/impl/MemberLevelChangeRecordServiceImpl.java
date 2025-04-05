package com.hotel.service.impl;

import com.hotel.entity.MemberLevel;
import com.hotel.entity.MemberLevelChangeRecord;
import com.hotel.entity.User;
import com.hotel.repository.MemberLevelChangeRecordRepository;
import com.hotel.service.MemberLevelChangeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MemberLevelChangeRecordServiceImpl implements MemberLevelChangeRecordService {

    @Autowired
    private MemberLevelChangeRecordRepository memberLevelChangeRecordRepository;

    @Override
    public MemberLevelChangeRecord createChangeRecord(MemberLevelChangeRecord record) {
        if (record.getCreateTime() == null) {
            record.setCreateTime(LocalDateTime.now());
        }
        return memberLevelChangeRecordRepository.save(record);
    }

    @Override
    public MemberLevelChangeRecord createChangeRecord(User user, MemberLevel oldLevel, MemberLevel newLevel, String reason) {
        MemberLevelChangeRecord record = MemberLevelChangeRecord.create(user, oldLevel, newLevel, reason);
        return createChangeRecord(record);
    }

    @Override
    public MemberLevelChangeRecord getChangeRecordById(Long id) {
        return memberLevelChangeRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("会员等级变更记录不存在"));
    }

    @Override
    public List<MemberLevelChangeRecord> getChangeRecordsByUser(User user) {
        return memberLevelChangeRecordRepository.findByUser(user);
    }

    @Override
    public Page<MemberLevelChangeRecord> getChangeRecordsByUser(User user, Pageable pageable) {
        return memberLevelChangeRecordRepository.findByUser(user, pageable);
    }

    @Override
    public MemberLevelChangeRecord getLatestChangeRecordByUser(User user) {
        return memberLevelChangeRecordRepository.findTopByUserOrderByCreateTimeDesc(user)
                .orElse(null);
    }

    @Override
    public List<MemberLevelChangeRecord> getChangeRecordsByUserAndTimeRange(User user, LocalDateTime start, LocalDateTime end) {
        return memberLevelChangeRecordRepository.findByUserAndCreateTimeBetween(user, start, end);
    }

    @Override
    public List<MemberLevelChangeRecord> getChangeRecordsByNewLevel(MemberLevel level) {
        return memberLevelChangeRecordRepository.findByNewLevel(level);
    }

    @Override
    public long countChangeRecordsByNewLevel(MemberLevel level) {
        return memberLevelChangeRecordRepository.countByNewLevel(level);
    }
} 