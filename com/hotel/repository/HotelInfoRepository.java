package com.hotel.repository;

import com.hotel.entity.HotelInfo;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HotelInfoRepository extends JpaRepository<HotelInfo, Long> {
    
    // 使用完全不同的名称
    @Query("SELECT h FROM HotelInfo h WHERE 1=1 ORDER BY h.id ASC")
    Optional<HotelInfo> getHotelWithMinimumId();
    
    // 使用显式的JPQL查询，避免Spring Data JPA自动生成查询
    @Query("SELECT h FROM HotelInfo h ORDER BY h.id ASC")
    Optional<HotelInfo> findFirstHotel();
    
    // 显式重写任何可能导致问题的方法，并抛出异常
    @Override
    default <S extends HotelInfo> Optional<S> findFirst(Example<S> example) {
        throw new UnsupportedOperationException("Method findFirst is not supported");
    }
    
    // 如果还有其他findFirst的变种，也一并重写
    default Optional<HotelInfo> findFirst() {
        throw new UnsupportedOperationException("Method findFirst is not supported");
    }
} 