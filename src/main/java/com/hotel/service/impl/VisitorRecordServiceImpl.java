package com.hotel.service.impl;

import com.hotel.entity.User;
import com.hotel.entity.Visitor;
import com.hotel.entity.VisitorRecord;
import com.hotel.repository.UserRepository;
import com.hotel.repository.VisitorRepository;
import com.hotel.repository.VisitorRecordRepository;
import com.hotel.service.UserService;
import com.hotel.service.VisitorRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VisitorRecordServiceImpl implements VisitorRecordService {

    @Autowired
    private VisitorRepository visitorRepository;
    
    @Autowired
    private VisitorRecordRepository visitorRecordRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;

    // ========== VisitorRecord 方法实现 ==========
    
    @Override
    public VisitorRecord createVisitorRecord(VisitorRecord visitorRecord, Long visitedUserId) {
        User visitedUser = userService.getUserById(visitedUserId);
        if (visitedUser == null) {
            throw new RuntimeException("Visited user not found with ID: " + visitedUserId);
        }
        visitorRecord.setVisitedUser(visitedUser);
        
        visitorRecord.setStatus(VisitorRecord.VisitStatus.VISITING);
        visitorRecord.setVisitTime(LocalDateTime.now());
        return visitorRecordRepository.save(visitorRecord);
    }

    @Override
    public VisitorRecord updateVisitorRecord(VisitorRecord visitorRecord) {
        VisitorRecord existingRecord = getVisitorRecordById(visitorRecord.getId());
        visitorRecord.setVisitedUser(existingRecord.getVisitedUser());
        return visitorRecordRepository.save(visitorRecord);
    }

    @Override
    public VisitorRecord recordVisitorLeave(Long recordId, LocalDateTime leaveTime) {
        VisitorRecord record = getVisitorRecordById(recordId);
        record.setLeaveTime(leaveTime);
        record.setStatus(VisitorRecord.VisitStatus.COMPLETED);
        return visitorRecordRepository.save(record);
    }

    @Override
    public void cancelVisitorRecord(Long recordId) {
        VisitorRecord record = getVisitorRecordById(recordId);
        record.setStatus(VisitorRecord.VisitStatus.CANCELLED);
        visitorRecordRepository.save(record);
    }

    @Override
    public VisitorRecord getVisitorRecordById(Long id) {
        return visitorRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("访客记录不存在"));
    }

    @Override
    public List<VisitorRecord> getVisitorRecordsByVisitedUser(User visitedUser) {
        return visitorRecordRepository.findByVisitedUser(visitedUser);
    }

    @Override
    public List<VisitorRecord> getVisitorRecordsByStatus(VisitorRecord.VisitStatus status) {
        return visitorRecordRepository.findByStatus(status);
    }

    @Override
    public List<VisitorRecord> getVisitorRecordsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return visitorRecordRepository.findByVisitTimeBetween(startTime, endTime);
    }

    @Override
    public List<VisitorRecord> getCurrentVisitors() {
        return visitorRecordRepository.findCurrentVisitors(LocalDateTime.now());
    }

    @Override
    public long countVisitorsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return visitorRecordRepository.countVisitorsByTimeRange(startTime, endTime);
    }

    @Override
    public List<VisitorRecord> searchVisitorRecords(String keyword) {
        return visitorRecordRepository.searchVisitors(keyword);
    }

    @Override
    public List<VisitorRecord> getCurrentVisitorsByUser(User visitedUser) {
        return visitorRecordRepository.findCurrentVisitorsByUser(visitedUser);
    }

    @Override
    public void updateVisitorRecordsStatus(List<Long> recordIds, VisitorRecord.VisitStatus status) {
        for (Long id : recordIds) {
            VisitorRecord record = getVisitorRecordById(id);
            record.setStatus(status);
            visitorRecordRepository.save(record);
        }
    }

    @Override
    public List<VisitorRecord> getAllVisitorRecords() {
        return visitorRecordRepository.findAll();
    }

    @Override
    public Page<VisitorRecord> searchVisitorRecordsPageable(
            String keyword,
            String status,
            String roomNumber,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable
    ) {
        Specification<VisitorRecord> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                predicates.add(cb.or(
                    cb.like(root.get("visitorName"), "%" + keyword + "%"),
                    cb.like(root.get("phone"), "%" + keyword + "%")
                ));
            }

            if (StringUtils.hasText(status)) {
                try {
                    VisitorRecord.VisitStatus visitStatus = VisitorRecord.VisitStatus.valueOf(status.toUpperCase());
                    predicates.add(cb.equal(root.get("status"), visitStatus));
                } catch (IllegalArgumentException e) {
                    System.out.println("Warning: Invalid visit status: " + status);
                }
            }

            if (StringUtils.hasText(roomNumber)) {
                predicates.add(cb.equal(root.join("visitedUser").get("roomNumber"), roomNumber));
            }

            if (startTime != null && endTime != null) {
                predicates.add(cb.between(root.get("visitTime"), startTime, endTime));
            } else if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("visitTime"), startTime));
            } else if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("visitTime"), endTime));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return visitorRecordRepository.findAll(spec, pageable);
    }

    // ========== Visitor 方法实现 ==========

    @Override
    public Visitor registerVisitor(Visitor visitor) {
        visitor.setVisitTime(LocalDateTime.now());
        return visitorRepository.save(visitor);
    }

    @Override
    public List<Visitor> getAllVisitors() {
        return visitorRepository.findAll();
    }

    @Override
    public Visitor getVisitorById(Long id) {
        return visitorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("访客不存在"));
    }

    @Override
    public List<Visitor> getVisitorsByName(String name) {
        return visitorRepository.findByName(name);
    }

    @Override
    public Visitor getVisitorByPhone(String phone) {
        List<Visitor> visitors = visitorRepository.findByPhone(phone);
        return visitors.isEmpty() ? null : visitors.get(0);
    }

    @Override
    public List<Visitor> getTodayVisitors() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        
        List<Visitor> allVisitors = visitorRepository.findAll();
        List<Visitor> todayVisitors = new ArrayList<>();
        
        for (Visitor visitor : allVisitors) {
            if (visitor.getVisitTime() != null && 
                visitor.getVisitTime().isAfter(startOfDay) && 
                visitor.getVisitTime().isBefore(endOfDay)) {
                todayVisitors.add(visitor);
            }
        }
        
        return todayVisitors;
    }

    @Override
    public List<Visitor> getVisitorsByDate(LocalDateTime startTime, LocalDateTime endTime) {
        List<Visitor> allVisitors = visitorRepository.findAll();
        List<Visitor> filteredVisitors = new ArrayList<>();
        
        for (Visitor visitor : allVisitors) {
            if (visitor.getVisitTime() != null && 
                visitor.getVisitTime().isAfter(startTime) && 
                visitor.getVisitTime().isBefore(endTime)) {
                filteredVisitors.add(visitor);
            }
        }
        
        return filteredVisitors;
    }

    @Override
    public List<Visitor> getVisitorsByRoom(String roomNumber) {
        List<Visitor> allVisitors = visitorRepository.findAll();
        List<Visitor> filteredVisitors = new ArrayList<>();
        
        for (Visitor visitor : allVisitors) {
            if (roomNumber.equals(visitor.getRoomNumber())) {
                filteredVisitors.add(visitor);
            }
        }
        
        return filteredVisitors;
    }

    @Override
    public Visitor updateVisitor(Visitor visitor) {
        getVisitorById(visitor.getId());
        return visitorRepository.save(visitor);
    }

    @Override
    public void deleteVisitor(Long id) {
        getVisitorById(id);
        visitorRepository.deleteById(id);
    }

    @Override
    public long countTodayVisitors() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        return visitorRecordRepository.countByVisitTimeBetween(startOfDay, endOfDay);
    }

    @Override
    public Visitor recordVisitorLeave(Long id) {
        Visitor visitor = getVisitorById(id);
        visitor.setLeaveTime(LocalDateTime.now());
        return visitorRepository.save(visitor);
    }
} 