package com.hotel.service;

import com.hotel.dto.PointsHistoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 积分历史记录服务接口
 */
public interface PointsHistoryService {
    /**
     * 获取用户的积分历史记录
     */
    Page<PointsHistoryDTO> getUserPointsHistory(Long userId, Pageable pageable);
} 