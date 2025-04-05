package com.hotel.service;

import com.hotel.entity.MarketingCampaign;
import java.util.List;

public interface MarketingCampaignService {
    /**
     * 创建营销活动
     */
    MarketingCampaign createCampaign(MarketingCampaign campaign);
    
    /**
     * 获取营销活动详情
     */
    MarketingCampaign getCampaignById(Long id);
    
    /**
     * 更新营销活动
     */
    MarketingCampaign updateCampaign(MarketingCampaign campaign);
    
    /**
     * 删除营销活动
     */
    void deleteCampaign(Long id);
    
    /**
     * 获取所有营销活动
     */
    List<MarketingCampaign> getAllCampaigns();
    
    /**
     * 获取活动中的营销活动
     */
    List<MarketingCampaign> getActiveCampaigns();
    
    /**
     * 根据会员等级获取适用的营销活动
     */
    List<MarketingCampaign> getCampaignsByMemberLevel(String memberLevel);
    
    /**
     * 激活营销活动
     */
    MarketingCampaign activateCampaign(Long id);
    
    /**
     * 取消营销活动
     */
    MarketingCampaign cancelCampaign(Long id);
} 