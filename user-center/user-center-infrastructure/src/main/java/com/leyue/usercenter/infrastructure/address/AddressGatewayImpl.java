package com.leyue.usercenter.infrastructure.address;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leyue.usercenter.domain.address.Address;
import com.leyue.usercenter.domain.address.gateway.AddressGateway;
import com.leyue.usercenter.infrastructure.address.assembler.AddressAssembler;
import com.leyue.usercenter.infrastructure.address.dataobject.AddressDO;
import com.leyue.usercenter.infrastructure.address.mapper.AddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AddressGatewayImpl implements AddressGateway {
    private final AddressMapper addressMapper;
    
    @Override
    public Address findById(String addressId) {
        LambdaQueryWrapper<AddressDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AddressDO::getAddressId, addressId);
        AddressDO addressDO = addressMapper.selectOne(wrapper);
        return AddressAssembler.INSTANCE.toDomain(addressDO);
    }
    
    @Override
    public List<Address> findByUserId(String userId) {
        LambdaQueryWrapper<AddressDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AddressDO::getUserId, userId)
                .orderByDesc(AddressDO::getIsDefault)
                .orderByDesc(AddressDO::getUpdatedAt);
        List<AddressDO> addressDOs = addressMapper.selectList(wrapper);
        return addressDOs.stream()
                .map(AddressAssembler.INSTANCE::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public void save(Address address) {
        AddressDO addressDO = AddressAssembler.INSTANCE.toDO(address);
        if (addressDO.getId() == null) {
            addressDO.setCreatedAt(new Date());
            addressDO.setUpdatedAt(new Date());
            addressMapper.insert(addressDO);
        } else {
            addressDO.setUpdatedAt(new Date());
            addressMapper.updateById(addressDO);
        }
    }
    
    @Override
    public void delete(String addressId) {
        LambdaQueryWrapper<AddressDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AddressDO::getAddressId, addressId);
        addressMapper.delete(wrapper);
    }
    
    @Override
    @Transactional
    public void setDefault(String addressId, String userId) {
        // 先将该用户的所有地址设为非默认
        unsetDefault(userId);
        
        // 再将指定地址设为默认
        LambdaQueryWrapper<AddressDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AddressDO::getAddressId, addressId);
        AddressDO addressDO = new AddressDO();
        addressDO.setIsDefault(true);
        addressDO.setUpdatedAt(new Date());
        addressMapper.update(addressDO, wrapper);
    }
    
    @Override
    public void unsetDefault(String userId) {
        LambdaQueryWrapper<AddressDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AddressDO::getUserId, userId)
                .eq(AddressDO::getIsDefault, true);
        AddressDO addressDO = new AddressDO();
        addressDO.setIsDefault(false);
        addressDO.setUpdatedAt(new Date());
        addressMapper.update(addressDO, wrapper);
    }
} 