package com.hotel.service;

import com.hotel.entity.MemberLevelChange;
import java.util.List;

public interface MemberLevelChangeService {
    /**
     * 记录会员等级变更
     */
    MemberLevelChange recordLevelChange(Long userId, String previousLevel, String newLevel, String changeType, String changeReason);

    /**
     * 获取用户的等级变更历史
     */
    List<MemberLevelChange> getUserLevelChangeHistory(Long userId);

    /**
     * 获取最近的等级变更记录
     */
    MemberLevelChange getLatestLevelChange(Long userId);
} 