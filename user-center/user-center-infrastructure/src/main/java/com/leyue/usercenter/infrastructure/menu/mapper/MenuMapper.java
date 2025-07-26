package com.leyue.usercenter.infrastructure.menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.usercenter.infrastructure.menu.dataobject.MenuDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MenuMapper extends BaseMapper<MenuDO> {
    List<MenuDO> selectByRoleId(@Param("roleId") Long roleId);
    void insertRoleMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId);
    void deleteRoleMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId);
    void deleteRoleMenuByRoleId(@Param("roleId") Long roleId);
} 