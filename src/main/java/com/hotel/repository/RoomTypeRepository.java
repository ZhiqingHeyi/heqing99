package com.hotel.repository;

import com.hotel.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    // 根据房间类型名称查找
    RoomType findByName(String name);
    
    // 检查名称是否已存在
    boolean existsByName(String name);
} 