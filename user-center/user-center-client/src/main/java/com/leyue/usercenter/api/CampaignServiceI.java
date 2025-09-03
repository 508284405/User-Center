package com.leyue.usercenter.api;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.dto.command.promotion.CreateCampaignCmd;
import com.leyue.usercenter.dto.command.promotion.UpdateCampaignCmd;
import com.leyue.usercenter.dto.query.promotion.CampaignPageQuery;
import com.leyue.usercenter.dto.data.promotion.CampaignDTO;

public interface CampaignServiceI {
    
    SingleResponse<CampaignDTO> createCampaign(CreateCampaignCmd cmd);
    
    SingleResponse<CampaignDTO> updateCampaign(UpdateCampaignCmd cmd);
    
    SingleResponse<Void> activateCampaign(Long id);
    
    SingleResponse<Void> pauseCampaign(Long id);
    
    SingleResponse<Void> resumeCampaign(Long id);
    
    SingleResponse<Void> completeCampaign(Long id);
    
    PageResponse<CampaignDTO> getCampaignPage(CampaignPageQuery query);
    
    SingleResponse<CampaignDTO> getCampaignById(Long id);
}