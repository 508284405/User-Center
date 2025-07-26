package com.leyue.usercenter.infrastructure.menu.assembler;

import com.leyue.usercenter.domain.menu.MenuPermission;
import com.leyue.usercenter.infrastructure.menu.dataobject.MenuPermissionDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MenuPermissionAssembler {
    MenuPermissionAssembler INSTANCE = Mappers.getMapper(MenuPermissionAssembler.class);
    
    MenuPermission toDomain(MenuPermissionDO menuPermissionDO);
    MenuPermissionDO toDO(MenuPermission menuPermission);
} 