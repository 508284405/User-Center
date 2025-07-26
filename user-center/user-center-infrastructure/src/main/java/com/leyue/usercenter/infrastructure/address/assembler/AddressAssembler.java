package com.leyue.usercenter.infrastructure.address.assembler;

import com.leyue.usercenter.domain.address.Address;
import com.leyue.usercenter.dto.AddressDTO;
import com.leyue.usercenter.infrastructure.address.dataobject.AddressDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AddressAssembler {
    AddressAssembler INSTANCE = Mappers.getMapper(AddressAssembler.class);
    
    Address toDomain(AddressDO addressDO);
    
    AddressDO toDO(Address address);
    
    AddressDTO toDTO(Address address);
} 