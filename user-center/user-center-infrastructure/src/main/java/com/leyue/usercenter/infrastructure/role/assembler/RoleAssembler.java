package com.leyue.usercenter.infrastructure.role.assembler;

import com.leyue.usercenter.domain.role.Role;
import com.leyue.usercenter.dto.RoleDTO;
import com.leyue.usercenter.infrastructure.role.dataobject.RoleDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RoleAssembler {
    RoleAssembler INSTANCE = Mappers.getMapper(RoleAssembler.class);
    
    @Mapping(target = "menus", ignore = true)
    Role toDomain(RoleDO roleDO);
    
    RoleDO toDO(Role role);

    RoleDTO toDTO(Role role);

    RoleDTO toDTO(RoleDO role);
}