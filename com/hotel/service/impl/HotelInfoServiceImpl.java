package com.hotel.service.impl;

import com.hotel.entity.HotelInfo;
import com.hotel.repository.HotelInfoBackupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HotelInfoServiceImpl {

    @Autowired
    private HotelInfoBackupRepository hotelInfoRepository;

    public HotelInfo getFirstHotel() {
        return hotelInfoRepository.getFirstHotel().orElse(null);
    }
} 