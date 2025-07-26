package com.leyue.usercenter.infrastructure.menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.usercenter.infrastructure.menu.dataobject.MenuPermissionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MenuPermissionMapper extends BaseMapper<MenuPermissionDO> {
    List<String> selectPermissionsByMenuId(@Param("menuId") Long menuId);
    void deleteByMenuId(@Param("menuId") Long menuId);
} 