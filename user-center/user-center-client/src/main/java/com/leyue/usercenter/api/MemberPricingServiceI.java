package com.leyue.usercenter.api;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.dto.command.member.CreateMemberPricingCmd;
import com.leyue.usercenter.dto.command.member.UpdateMemberPricingCmd;
import com.leyue.usercenter.dto.data.member.MemberPricingDTO;
import com.leyue.usercenter.dto.query.member.MemberPricingPageQry;
import com.leyue.usercenter.dto.query.member.MemberPricingQry;

import java.math.BigDecimal;
import java.util.List;

public interface MemberPricingServiceI {
    
    SingleResponse<MemberPricingDTO> createMemberPricing(CreateMemberPricingCmd cmd);
    
    SingleResponse<MemberPricingDTO> updateMemberPricing(UpdateMemberPricingCmd cmd);
    
    SingleResponse<MemberPricingDTO> getMemberPricing(MemberPricingQry qry);
    
    MultiResponse<MemberPricingDTO> getMemberPricingByProduct(Long productId);
    
    MultiResponse<MemberPricingDTO> getMemberPricingByLevel(String memberLevel);
    
    PageResponse<MemberPricingDTO> getMemberPricingPage(MemberPricingPageQry qry);
    
    Response deleteMemberPricing(Long id);
    
    Response activateMemberPricing(Long id);
    
    Response deactivateMemberPricing(Long id);
    
    SingleResponse<BigDecimal> calculateMemberPrice(Long productId, String memberLevel, 
                                                   BigDecimal originalPrice, Integer quantity);
    
    MultiResponse<MemberPricingDTO> getBatchMemberPricing(List<Long> productIds, String memberLevel);
}