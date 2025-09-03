package com.leyue.usercenter.infrastructure.member;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.usercenter.domain.member.MemberPricing;
import com.leyue.usercenter.domain.member.gateway.MemberPricingGateway;
import com.leyue.usercenter.infrastructure.common.dataobject.MemberPricingConfigDO;
import com.leyue.usercenter.infrastructure.common.mapper.MemberPricingConfigMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MemberPricingGatewayImpl implements MemberPricingGateway {

    @Resource
    private MemberPricingConfigMapper memberPricingConfigMapper;

    @Override
    public MemberPricing save(MemberPricing memberPricing) {
        MemberPricingConfigDO configDO = convertToConfigDO(memberPricing);
        
        if (memberPricing.getId() == null) {
            memberPricingConfigMapper.insert(configDO);
            memberPricing.setId(configDO.getId());
        } else {
            memberPricingConfigMapper.updateById(configDO);
        }
        
        return memberPricing;
    }

    @Override
    public MemberPricing getById(Long id) {
        MemberPricingConfigDO configDO = memberPricingConfigMapper.selectById(id);
        return configDO != null ? convertToMemberPricing(configDO) : null;
    }

    @Override
    public MemberPricing getByProductAndLevel(Long productId, String memberLevel) {
        LambdaQueryWrapper<MemberPricingConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberPricingConfigDO::getProductId, productId)
               .eq(MemberPricingConfigDO::getMemberLevel, memberLevel)
               .eq(MemberPricingConfigDO::getIsActive, true);
        
        MemberPricingConfigDO configDO = memberPricingConfigMapper.selectOne(wrapper);
        return configDO != null ? convertToMemberPricing(configDO) : null;
    }

    @Override
    public List<MemberPricing> getByProductId(Long productId) {
        LambdaQueryWrapper<MemberPricingConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberPricingConfigDO::getProductId, productId)
               .eq(MemberPricingConfigDO::getIsActive, true);
        
        List<MemberPricingConfigDO> configDOs = memberPricingConfigMapper.selectList(wrapper);
        return configDOs.stream()
                .map(this::convertToMemberPricing)
                .collect(Collectors.toList());
    }

    @Override
    public List<MemberPricing> getByMemberLevel(String memberLevel) {
        LambdaQueryWrapper<MemberPricingConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberPricingConfigDO::getMemberLevel, memberLevel)
               .eq(MemberPricingConfigDO::getIsActive, true);
        
        List<MemberPricingConfigDO> configDOs = memberPricingConfigMapper.selectList(wrapper);
        return configDOs.stream()
                .map(this::convertToMemberPricing)
                .collect(Collectors.toList());
    }

    @Override
    public List<MemberPricing> getActiveConfigs() {
        LambdaQueryWrapper<MemberPricingConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberPricingConfigDO::getIsActive, true);
        
        List<MemberPricingConfigDO> configDOs = memberPricingConfigMapper.selectList(wrapper);
        return configDOs.stream()
                .map(this::convertToMemberPricing)
                .collect(Collectors.toList());
    }

    @Override
    public List<MemberPricing> getByPage(int pageNum, int pageSize, Long productId, String memberLevel, Boolean isActive) {
        Page<MemberPricingConfigDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MemberPricingConfigDO> wrapper = new LambdaQueryWrapper<>();
        
        if (productId != null) {
            wrapper.eq(MemberPricingConfigDO::getProductId, productId);
        }
        if (memberLevel != null && !memberLevel.trim().isEmpty()) {
            wrapper.eq(MemberPricingConfigDO::getMemberLevel, memberLevel);
        }
        if (isActive != null) {
            wrapper.eq(MemberPricingConfigDO::getIsActive, isActive);
        }
        
        wrapper.orderByDesc(MemberPricingConfigDO::getUpdatedAt);
        
        IPage<MemberPricingConfigDO> pageResult = memberPricingConfigMapper.selectPage(page, wrapper);
        return pageResult.getRecords().stream()
                .map(this::convertToMemberPricing)
                .collect(Collectors.toList());
    }

    @Override
    public long countByPage(Long productId, String memberLevel, Boolean isActive) {
        LambdaQueryWrapper<MemberPricingConfigDO> wrapper = new LambdaQueryWrapper<>();
        
        if (productId != null) {
            wrapper.eq(MemberPricingConfigDO::getProductId, productId);
        }
        if (memberLevel != null && !memberLevel.trim().isEmpty()) {
            wrapper.eq(MemberPricingConfigDO::getMemberLevel, memberLevel);
        }
        if (isActive != null) {
            wrapper.eq(MemberPricingConfigDO::getIsActive, isActive);
        }
        
        return memberPricingConfigMapper.selectCount(wrapper);
    }

    @Override
    public void delete(Long id) {
        memberPricingConfigMapper.deleteById(id);
    }

    @Override
    public boolean existsByProductAndLevel(Long productId, String memberLevel) {
        LambdaQueryWrapper<MemberPricingConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberPricingConfigDO::getProductId, productId)
               .eq(MemberPricingConfigDO::getMemberLevel, memberLevel);
        
        return memberPricingConfigMapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<MemberPricing> getBatchByProductAndLevel(List<Long> productIds, String memberLevel) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        
        LambdaQueryWrapper<MemberPricingConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(MemberPricingConfigDO::getProductId, productIds)
               .eq(MemberPricingConfigDO::getMemberLevel, memberLevel)
               .eq(MemberPricingConfigDO::getIsActive, true);
        
        List<MemberPricingConfigDO> configDOs = memberPricingConfigMapper.selectList(wrapper);
        return configDOs.stream()
                .map(this::convertToMemberPricing)
                .collect(Collectors.toList());
    }

    private MemberPricingConfigDO convertToConfigDO(MemberPricing memberPricing) {
        MemberPricingConfigDO configDO = new MemberPricingConfigDO();
        BeanUtils.copyProperties(memberPricing, configDO);
        return configDO;
    }

    private MemberPricing convertToMemberPricing(MemberPricingConfigDO configDO) {
        MemberPricing memberPricing = new MemberPricing();
        BeanUtils.copyProperties(configDO, memberPricing);
        return memberPricing;
    }
}