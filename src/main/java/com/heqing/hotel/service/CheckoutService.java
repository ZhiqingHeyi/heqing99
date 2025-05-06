package com.heqing.hotel.service;

import com.heqing.hotel.model.Checkout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.Map;

public interface CheckoutService {
    
    // 创建退房记录
    Checkout createCheckout(Checkout checkout);
    
    // 确认退房
    Checkout confirmCheckout(Long id);
    
    // 获取退房记录
    Checkout getCheckout(Long id);
    
    // 分页查询退房记录
    Page<Checkout> getCheckouts(String roomNumber, String guestName, LocalDate checkoutDate, Pageable pageable);
    
    // 获取统计数据
    Map<String, Long> getCheckoutStats();
    
    // 更新退房记录
    Checkout updateCheckout(Long id, Checkout checkout);
    
    // 删除退房记录
    void deleteCheckout(Long id);
} 