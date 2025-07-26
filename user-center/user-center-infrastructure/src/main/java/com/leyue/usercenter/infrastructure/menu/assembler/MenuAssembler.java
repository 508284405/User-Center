package com.leyue.usercenter.infrastructure.menu.assembler;

import com.leyue.usercenter.domain.menu.Menu;
import com.leyue.usercenter.dto.MenuDTO;
import com.leyue.usercenter.infrastructure.menu.dataobject.MenuDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MenuAssembler {
    MenuAssembler INSTANCE = Mappers.getMapper(MenuAssembler.class);
    
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "children", ignore = true)
    Menu toDomain(MenuDO menuDO);
    
    MenuDO toDO(Menu menu);

    MenuDTO toDTO(Menu menu);
}