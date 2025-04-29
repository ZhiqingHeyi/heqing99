package com.hotel.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "room_types")
@EntityListeners(AuditingEntityListener.class)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class RoomType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;
    
    // 长描述，详细介绍房型
    @Column(name = "long_description", columnDefinition = "TEXT")
    private String longDescription;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;
    
    // 周末价格
    @Column(name = "weekend_price")
    private BigDecimal weekendPrice;
    
    // 假日价格
    @Column(name = "holiday_price")
    private BigDecimal holidayPrice;

    @Column(nullable = false)
    private Integer capacity;
    
    // 最大容量（含加床）
    @Column(name = "max_capacity")
    private Integer maxCapacity;
    
    // 额外床铺价格
    @Column(name = "extra_bed_price")
    private BigDecimal extraBedPrice;
    
    // 床型(如：大床、双床、三床等)
    @Column(name = "bed_type")
    private String bedType;
    
    // 床尺寸
    @Column(name = "bed_size")
    private String bedSize;
    
    // 房间面积（平方米）
    @Column(name = "area")
    private Integer area;
    
    // 楼层信息
    @Column(name = "floor")
    private String floor;

    // 设施，以逗号分隔
    private String amenities;
    
    // 房型图片，JSON格式存储
    @Column(name = "images", columnDefinition = "TEXT")
    private String images;
    
    // 预订政策，JSON格式存储
    @Column(name = "policies", columnDefinition = "TEXT")
    private String policies;
    
    @OneToMany(mappedBy = "roomType")
    @JsonIgnoreProperties("roomType")
    private List<Room> rooms;

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;
}