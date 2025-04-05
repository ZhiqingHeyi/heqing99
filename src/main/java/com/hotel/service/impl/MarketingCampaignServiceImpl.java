package com.hotel.service.impl;

import com.hotel.entity.MarketingCampaign;
import com.hotel.service.MarketingCampaignService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MarketingCampaignServiceImpl implements MarketingCampaignService {

    // 临时存储活动的列表，实际项目中应该使用数据库
    private final List<MarketingCampaign> campaigns = new ArrayList<>();
    private Long nextId = 1L;

    @Override
    public MarketingCampaign createCampaign(MarketingCampaign campaign) {
        campaign.setId(nextId++);
        campaigns.add(campaign);
        return campaign;
    }

    @Override
    public MarketingCampaign getCampaignById(Long id) {
        return campaigns.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public MarketingCampaign updateCampaign(MarketingCampaign campaign) {
        for (int i = 0; i < campaigns.size(); i++) {
            if (campaigns.get(i).getId().equals(campaign.getId())) {
                campaigns.set(i, campaign);
                return campaign;
            }
        }
        return null;
    }

    @Override
    public void deleteCampaign(Long id) {
        campaigns.removeIf(c -> c.getId().equals(id));
    }

    @Override
    public List<MarketingCampaign> getAllCampaigns() {
        return new ArrayList<>(campaigns);
    }

    @Override
    public List<MarketingCampaign> getActiveCampaigns() {
        return campaigns.stream()
                .filter(c -> c.getStatus() == MarketingCampaign.CampaignStatus.ACTIVE)
                .toList();
    }

    @Override
    public List<MarketingCampaign> getCampaignsByMemberLevel(String memberLevel) {
        return campaigns.stream()
                .filter(c -> c.getTargetMemberLevel() == null || 
                        c.getTargetMemberLevel().equals(memberLevel))
                .filter(c -> c.getStatus() == MarketingCampaign.CampaignStatus.ACTIVE)
                .toList();
    }

    @Override
    public MarketingCampaign activateCampaign(Long id) {
        MarketingCampaign campaign = getCampaignById(id);
        if (campaign != null) {
            campaign.setStatus(MarketingCampaign.CampaignStatus.ACTIVE);
            return campaign;
        }
        return null;
    }

    @Override
    public MarketingCampaign cancelCampaign(Long id) {
        MarketingCampaign campaign = getCampaignById(id);
        if (campaign != null) {
            campaign.setStatus(MarketingCampaign.CampaignStatus.CANCELLED);
            return campaign;
        }
        return null;
    }
} 