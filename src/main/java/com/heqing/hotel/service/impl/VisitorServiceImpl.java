package com.heqing.hotel.service.impl;

import com.heqing.hotel.model.VisitorRecord;
import com.heqing.hotel.repository.VisitorRepository;
import com.heqing.hotel.service.VisitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VisitorServiceImpl implements VisitorService {

    @Autowired
    private VisitorRepository visitorRepository;

    @Override
    public VisitorRecord createVisitorRecord(VisitorRecord record) {
        if (record.getVisitTime() == null) {
            record.setVisitTime(LocalDateTime.now());
        }
        return visitorRepository.save(record);
    }

    @Override
    public VisitorRecord getVisitorRecord(Long id) {
        return visitorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("访客记录不存在"));
    }

    @Override
    public List<VisitorRecord> getAllVisitorRecords() {
        return visitorRepository.findAll();
    }

    @Override
    public void cancelVisitorRecord(Long id) {
        VisitorRecord record = getVisitorRecord(id);
        record.setStatus("CANCELLED");
        record.setUpdateTime(LocalDateTime.now());
        visitorRepository.save(record);
    }

    @Override
    public Map<String, Object> getVisitorStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取今日访客数量
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayVisitors = visitorRepository.countByVisitTimeGreaterThanEqual(today);
        statistics.put("todayVisitors", todayVisitors);
        
        // 获取待处理的访客记录数量
        long pendingVisitors = visitorRepository.countByStatus("PENDING");
        statistics.put("pendingVisitors", pendingVisitors);
        
        // 获取已完成的访客记录数量
        long completedVisitors = visitorRepository.countByStatus("COMPLETED");
        statistics.put("completedVisitors", completedVisitors);
        
        return statistics;
    }
}