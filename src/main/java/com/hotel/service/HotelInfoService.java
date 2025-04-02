package com.hotel.service;

import com.hotel.entity.HotelInfo;

public interface HotelInfoService {
    /**
     * 获取酒店基本信息
     */
    HotelInfo getHotelInfo();

    /**
     * 更新酒店信息
     */
    HotelInfo updateHotelInfo(HotelInfo hotelInfo);

    /**
     * 通过名称查找酒店
     */
    HotelInfo findHotelByName(String name);

    /**
     * 初始化酒店信息（如果不存在）
     */
    HotelInfo initializeHotelInfo(HotelInfo hotelInfo);
}