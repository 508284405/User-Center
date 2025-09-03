package com.leyue.usercenter.domain.member.gateway;

import com.leyue.usercenter.domain.member.MemberPricing;

import java.util.List;

public interface MemberPricingGateway {
    
    MemberPricing save(MemberPricing memberPricing);
    
    MemberPricing getById(Long id);
    
    MemberPricing getByProductAndLevel(Long productId, String memberLevel);
    
    List<MemberPricing> getByProductId(Long productId);
    
    List<MemberPricing> getByMemberLevel(String memberLevel);
    
    List<MemberPricing> getActiveConfigs();
    
    List<MemberPricing> getByPage(int pageNum, int pageSize, Long productId, String memberLevel, Boolean isActive);
    
    long countByPage(Long productId, String memberLevel, Boolean isActive);
    
    void delete(Long id);
    
    boolean existsByProductAndLevel(Long productId, String memberLevel);
    
    List<MemberPricing> getBatchByProductAndLevel(List<Long> productIds, String memberLevel);
}