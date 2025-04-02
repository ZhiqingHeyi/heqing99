package com.hotel.repository;

import com.hotel.entity.HotelInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HotelInfoBackupRepository extends JpaRepository<HotelInfo, Long> {
    
    @Query("SELECT h FROM HotelInfo h ORDER BY h.id ASC")
    Optional<HotelInfo> getFirstHotel();
} 