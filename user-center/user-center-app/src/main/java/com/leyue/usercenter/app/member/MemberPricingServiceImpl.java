package com.leyue.usercenter.app.member;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.api.MemberPricingServiceI;
import com.leyue.usercenter.domain.member.MemberPricing;
import com.leyue.usercenter.domain.member.gateway.MemberPricingGateway;
import com.leyue.usercenter.dto.command.member.CreateMemberPricingCmd;
import com.leyue.usercenter.dto.command.member.UpdateMemberPricingCmd;
import com.leyue.usercenter.dto.data.member.MemberPricingDTO;
import com.leyue.usercenter.dto.query.member.MemberPricingPageQry;
import com.leyue.usercenter.dto.query.member.MemberPricingQry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MemberPricingServiceImpl implements MemberPricingServiceI {

    @Resource
    private MemberPricingGateway memberPricingGateway;

    @Override
    public SingleResponse<MemberPricingDTO> createMemberPricing(CreateMemberPricingCmd cmd) {
        log.info("创建会员价配置, productId: {}, memberLevel: {}", cmd.getProductId(), cmd.getMemberLevel());
        
        if (memberPricingGateway.existsByProductAndLevel(cmd.getProductId(), cmd.getMemberLevel())) {
            return SingleResponse.buildFailure("该商品的会员等级配置已存在");
        }
        
        try {
            MemberPricing.DiscountType discountType = MemberPricing.DiscountType.valueOf(cmd.getDiscountType());
            
            MemberPricing memberPricing = new MemberPricing(
                cmd.getProductId(),
                cmd.getMemberLevel(),
                discountType,
                cmd.getDiscountValue(),
                cmd.getCreatedBy()
            );
            
            if (cmd.getMinPurchaseQuantity() != null || cmd.getMaxPurchaseQuantity() != null) {
                memberPricing.setPurchaseQuantityLimit(cmd.getMinPurchaseQuantity(), cmd.getMaxPurchaseQuantity());
            }
            
            if (cmd.getStartTime() != null || cmd.getEndTime() != null) {
                memberPricing.setValidityPeriod(cmd.getStartTime(), cmd.getEndTime());
            }
            
            if (cmd.getIsActive() != null) {
                memberPricing.setIsActive(cmd.getIsActive());
            }
            
            MemberPricing savedPricing = memberPricingGateway.save(memberPricing);
            return SingleResponse.of(convertToDTO(savedPricing));
            
        } catch (IllegalArgumentException e) {
            log.error("创建会员价配置失败: {}", e.getMessage());
            return SingleResponse.buildFailure("折扣类型不正确: " + cmd.getDiscountType());
        } catch (Exception e) {
            log.error("创建会员价配置异常", e);
            return SingleResponse.buildFailure("创建会员价配置失败");
        }
    }

    @Override
    public SingleResponse<MemberPricingDTO> updateMemberPricing(UpdateMemberPricingCmd cmd) {
        log.info("更新会员价配置, id: {}", cmd.getId());
        
        MemberPricing memberPricing = memberPricingGateway.getById(cmd.getId());
        if (memberPricing == null) {
            return SingleResponse.buildFailure("会员价配置不存在");
        }
        
        try {
            if (cmd.getDiscountType() != null) {
                MemberPricing.DiscountType discountType = MemberPricing.DiscountType.valueOf(cmd.getDiscountType());
                memberPricing.setDiscountType(discountType);
            }
            
            if (cmd.getDiscountValue() != null) {
                memberPricing.setDiscountValue(cmd.getDiscountValue());
            }
            
            if (cmd.getMinPurchaseQuantity() != null || cmd.getMaxPurchaseQuantity() != null) {
                memberPricing.setPurchaseQuantityLimit(cmd.getMinPurchaseQuantity(), cmd.getMaxPurchaseQuantity());
            }
            
            if (cmd.getStartTime() != null || cmd.getEndTime() != null) {
                memberPricing.setValidityPeriod(cmd.getStartTime(), cmd.getEndTime());
            }
            
            if (cmd.getIsActive() != null) {
                memberPricing.setIsActive(cmd.getIsActive());
            }
            
            if (cmd.getUpdatedBy() != null) {
                memberPricing.setUpdatedBy(cmd.getUpdatedBy());
                memberPricing.setUpdatedAt(LocalDateTime.now());
            }
            
            MemberPricing updatedPricing = memberPricingGateway.save(memberPricing);
            return SingleResponse.of(convertToDTO(updatedPricing));
            
        } catch (IllegalArgumentException e) {
            log.error("更新会员价配置失败: {}", e.getMessage());
            return SingleResponse.buildFailure(e.getMessage());
        } catch (Exception e) {
            log.error("更新会员价配置异常", e);
            return SingleResponse.buildFailure("更新会员价配置失败");
        }
    }

    @Override
    public SingleResponse<MemberPricingDTO> getMemberPricing(MemberPricingQry qry) {
        log.info("查询会员价配置, id: {}, productId: {}, memberLevel: {}", 
                qry.getId(), qry.getProductId(), qry.getMemberLevel());
        
        MemberPricing memberPricing = null;
        
        if (qry.getId() != null) {
            memberPricing = memberPricingGateway.getById(qry.getId());
        } else if (qry.getProductId() != null && qry.getMemberLevel() != null) {
            memberPricing = memberPricingGateway.getByProductAndLevel(qry.getProductId(), qry.getMemberLevel());
        }
        
        if (memberPricing == null) {
            return SingleResponse.buildFailure("会员价配置不存在");
        }
        
        return SingleResponse.of(convertToDTO(memberPricing));
    }

    @Override
    public MultiResponse<MemberPricingDTO> getMemberPricingByProduct(Long productId) {
        log.info("查询商品会员价配置, productId: {}", productId);
        
        List<MemberPricing> pricingList = memberPricingGateway.getByProductId(productId);
        List<MemberPricingDTO> dtoList = pricingList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return MultiResponse.of(dtoList);
    }

    @Override
    public MultiResponse<MemberPricingDTO> getMemberPricingByLevel(String memberLevel) {
        log.info("查询会员等级价格配置, memberLevel: {}", memberLevel);
        
        List<MemberPricing> pricingList = memberPricingGateway.getByMemberLevel(memberLevel);
        List<MemberPricingDTO> dtoList = pricingList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return MultiResponse.of(dtoList);
    }

    @Override
    public PageResponse<MemberPricingDTO> getMemberPricingPage(MemberPricingPageQry qry) {
        log.info("分页查询会员价配置, pageNum: {}, pageSize: {}", qry.getPageIndex(), qry.getPageSize());
        
        List<MemberPricing> pricingList = memberPricingGateway.getByPage(
                qry.getPageIndex(),
                qry.getPageSize(),
                qry.getProductId(),
                qry.getMemberLevel(),
                qry.getIsActive()
        );
        
        long totalCount = memberPricingGateway.countByPage(
                qry.getProductId(),
                qry.getMemberLevel(),
                qry.getIsActive()
        );
        
        List<MemberPricingDTO> dtoList = pricingList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return PageResponse.of(dtoList, (int) totalCount, qry.getPageIndex(), qry.getPageSize());
    }

    @Override
    public Response deleteMemberPricing(Long id) {
        log.info("删除会员价配置, id: {}", id);
        
        MemberPricing memberPricing = memberPricingGateway.getById(id);
        if (memberPricing == null) {
            return Response.buildFailure("会员价配置不存在");
        }
        
        memberPricingGateway.delete(id);
        return Response.buildSuccess();
    }

    @Override
    public Response activateMemberPricing(Long id) {
        log.info("激活会员价配置, id: {}", id);
        
        MemberPricing memberPricing = memberPricingGateway.getById(id);
        if (memberPricing == null) {
            return Response.buildFailure("会员价配置不存在");
        }
        
        memberPricing.activate();
        memberPricingGateway.save(memberPricing);
        return Response.buildSuccess();
    }

    @Override
    public Response deactivateMemberPricing(Long id) {
        log.info("停用会员价配置, id: {}", id);
        
        MemberPricing memberPricing = memberPricingGateway.getById(id);
        if (memberPricing == null) {
            return Response.buildFailure("会员价配置不存在");
        }
        
        memberPricing.deactivate();
        memberPricingGateway.save(memberPricing);
        return Response.buildSuccess();
    }

    @Override
    public SingleResponse<BigDecimal> calculateMemberPrice(Long productId, String memberLevel,
                                                          BigDecimal originalPrice, Integer quantity) {
        log.info("计算会员价格, productId: {}, memberLevel: {}, originalPrice: {}, quantity: {}",
                productId, memberLevel, originalPrice, quantity);
        
        MemberPricing memberPricing = memberPricingGateway.getByProductAndLevel(productId, memberLevel);
        if (memberPricing == null) {
            return SingleResponse.of(originalPrice);
        }
        
        BigDecimal memberPrice = memberPricing.calculateMemberPrice(originalPrice, quantity);
        return SingleResponse.of(memberPrice);
    }

    @Override
    public MultiResponse<MemberPricingDTO> getBatchMemberPricing(List<Long> productIds, String memberLevel) {
        log.info("批量查询会员价配置, productIds size: {}, memberLevel: {}", 
                productIds != null ? productIds.size() : 0, memberLevel);
        
        if (productIds == null || productIds.isEmpty()) {
            return MultiResponse.of(List.of());
        }
        
        List<MemberPricing> pricingList = memberPricingGateway.getBatchByProductAndLevel(productIds, memberLevel);
        List<MemberPricingDTO> dtoList = pricingList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return MultiResponse.of(dtoList);
    }

    private MemberPricingDTO convertToDTO(MemberPricing memberPricing) {
        MemberPricingDTO dto = new MemberPricingDTO();
        BeanUtils.copyProperties(memberPricing, dto);
        
        if (memberPricing.getDiscountType() != null) {
            dto.setDiscountType(memberPricing.getDiscountType().name());
            dto.setDiscountTypeDesc(memberPricing.getDiscountType().getDescription());
        }
        
        return dto;
    }
}