package com.heqing.hotel.service;

import com.heqing.hotel.model.VisitorRecord;
import java.util.List;
import java.util.Map;

public interface VisitorService {

    /**
     * 创建访客记录
     * @param record 访客记录信息
     * @return 保存后的访客记录
     */
    VisitorRecord createVisitorRecord(VisitorRecord record);

    /**
     * 获取指定访客记录
     * @param id 记录ID
     * @return 访客记录
     */
    VisitorRecord getVisitorRecord(Long id);

    /**
     * 获取所有访客记录
     * @return 访客记录列表
     */
    List<VisitorRecord> getAllVisitorRecords();

    /**
     * 取消访客记录
     * @param id 记录ID
     */
    void cancelVisitorRecord(Long id);

    /**
     * 获取访客统计信息
     * @return 统计数据
     */
    Map<String, Object> getVisitorStatistics();
}