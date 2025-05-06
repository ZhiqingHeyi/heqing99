package com.heqing.hotel.service.impl;

import com.heqing.hotel.model.Checkout;
import com.heqing.hotel.model.CheckoutStatus;
import com.heqing.hotel.repository.CheckoutRepository;
import com.heqing.hotel.service.CheckoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    @Autowired
    private CheckoutRepository checkoutRepository;

    @Override
    @Transactional
    public Checkout createCheckout(Checkout checkout) {
        checkout.setStatus(CheckoutStatus.PENDING);
        return checkoutRepository.save(checkout);
    }

    @Override
    @Transactional
    public Checkout confirmCheckout(Long id) {
        Checkout checkout = checkoutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("退房记录不存在"));
        
        if (checkout.getStatus() != CheckoutStatus.PENDING) {
            throw new RuntimeException("只能确认待退房状态的记录");
        }
        
        checkout.setStatus(CheckoutStatus.CLEANING);
        return checkoutRepository.save(checkout);
    }

    @Override
    public Checkout getCheckout(Long id) {
        return checkoutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("退房记录不存在"));
    }

    @Override
    public Page<Checkout> getCheckouts(String roomNumber, String guestName, LocalDate checkoutDate, Pageable pageable) {
        Specification<Checkout> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (roomNumber != null && !roomNumber.isEmpty()) {
                predicates.add(cb.equal(root.get("roomNumber"), roomNumber));
            }
            
            if (guestName != null && !guestName.isEmpty()) {
                predicates.add(cb.like(root.get("guestName"), "%" + guestName + "%"));
            }
            
            if (checkoutDate != null) {
                predicates.add(cb.equal(root.get("checkoutDate"), checkoutDate));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        return checkoutRepository.findAll(spec, pageable);
    }

    @Override
    public Map<String, Long> getCheckoutStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("todayCheckouts", checkoutRepository.countTodayCheckouts());
        stats.put("monthlyCheckouts", checkoutRepository.countMonthlyCheckouts());
        stats.put("pendingCleanings", checkoutRepository.countByStatus(CheckoutStatus.CLEANING));
        return stats;
    }

    @Override
    @Transactional
    public Checkout updateCheckout(Long id, Checkout checkoutDetails) {
        Checkout checkout = checkoutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("退房记录不存在"));
        
        checkout.setRoomNumber(checkoutDetails.getRoomNumber());
        checkout.setGuestName(checkoutDetails.getGuestName());
        checkout.setCheckoutDate(checkoutDetails.getCheckoutDate());
        checkout.setCheckoutTime(checkoutDetails.getCheckoutTime());
        checkout.setRemarks(checkoutDetails.getRemarks());
        
        return checkoutRepository.save(checkout);
    }

    @Override
    @Transactional
    public void deleteCheckout(Long id) {
        Checkout checkout = checkoutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("退房记录不存在"));
        
        checkoutRepository.delete(checkout);
    }
} 