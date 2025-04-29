package com.hotel.dto;

import lombok.Data;

/**
 * 房间筛选条件DTO
 * 用于封装房间筛选的请求参数
 */
@Data
public class RoomFilterDTO {
    
    /**
     * 楼层
     */
    private Integer floor;
    
    /**
     * 房间类型ID
     */
    private String roomTypeId;
    
    /**
     * 房间状态
     */
    private String status;
    
    /**
     * 关键词搜索
     */
    private String keyword;
    
    /**
     * 页码
     */
    private Integer page = 0;
    
    /**
     * 每页大小
     */
    private Integer size = 10;
} 