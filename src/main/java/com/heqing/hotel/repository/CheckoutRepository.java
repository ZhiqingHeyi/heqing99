package com.heqing.hotel.repository;

import com.heqing.hotel.model.Checkout;
import com.heqing.hotel.model.CheckoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CheckoutRepository extends JpaRepository<Checkout, Long>, JpaSpecificationExecutor<Checkout> {
    
    // 根据房间号查找
    List<Checkout> findByRoomNumber(String roomNumber);
    
    // 根据客户姓名查找
    List<Checkout> findByGuestNameContaining(String guestName);
    
    // 根据退房日期查找
    List<Checkout> findByCheckoutDate(LocalDate checkoutDate);
    
    // 根据状态查找
    List<Checkout> findByStatus(CheckoutStatus status);
    
    // 查找今日退房数量
    @Query("SELECT COUNT(c) FROM Checkout c WHERE c.checkoutDate = CURRENT_DATE")
    Long countTodayCheckouts();
    
    // 查找本月退房数量
    @Query("SELECT COUNT(c) FROM Checkout c WHERE YEAR(c.checkoutDate) = YEAR(CURRENT_DATE) AND MONTH(c.checkoutDate) = MONTH(CURRENT_DATE)")
    Long countMonthlyCheckouts();
    
    // 查找待清洁房间数量
    Long countByStatus(CheckoutStatus status);
} 