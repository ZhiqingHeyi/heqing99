package com.hotel.service;

import com.hotel.entity.MemberLevelChange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberLevelService {
    /**
     * 变更会员等级
     */
    MemberLevelChange changeMemberLevel(MemberLevelChange change);

    /**
     * 获取会员等级变更历史
     */
    Page<MemberLevelChange> getMemberLevelHistory(Pageable pageable);

    /**
     * 检查并处理会员升级
     */
    Boolean checkAndHandleUpgrade();
} 