package com.hotel.service.impl;

import com.hotel.entity.HotelInfo;
import com.hotel.repository.HotelInfoRepository;
import com.hotel.service.HotelInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class HotelInfoServiceImpl implements HotelInfoService {

    @Autowired
    private HotelInfoRepository hotelInfoRepository;

    @Override
    public HotelInfo getHotelInfo() {
        return hotelInfoRepository.findFirst()
                .orElseThrow(() -> new RuntimeException("酒店信息不存在"));
    }

    @Override
    public HotelInfo updateHotelInfo(HotelInfo hotelInfo) {
        // 确保要更新的酒店信息存在
        HotelInfo existingInfo = hotelInfoRepository.findById(hotelInfo.getId())
                .orElseThrow(() -> new RuntimeException("酒店信息不存在"));

        // 更新酒店信息
        existingInfo.setName(hotelInfo.getName());
        existingInfo.setAddress(hotelInfo.getAddress());
        existingInfo.setPhone(hotelInfo.getPhone());
        existingInfo.setEmail(hotelInfo.getEmail());
        existingInfo.setDescription(hotelInfo.getDescription());
        existingInfo.setFacilities(hotelInfo.getFacilities());
        existingInfo.setCheckInTime(hotelInfo.getCheckInTime());
        existingInfo.setCheckOutTime(hotelInfo.getCheckOutTime());
        existingInfo.setImages(hotelInfo.getImages());

        return hotelInfoRepository.save(existingInfo);
    }

    @Override
    public HotelInfo findHotelByName(String name) {
        return hotelInfoRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("未找到指定名称的酒店"));
    }

    @Override
    public HotelInfo initializeHotelInfo(HotelInfo hotelInfo) {
        // 检查是否已存在酒店信息
        if (hotelInfoRepository.findFirst().isPresent()) {
            throw new RuntimeException("酒店信息已存在，不能重复初始化");
        }

        // 保存新的酒店信息
        return hotelInfoRepository.save(hotelInfo);
    }
}