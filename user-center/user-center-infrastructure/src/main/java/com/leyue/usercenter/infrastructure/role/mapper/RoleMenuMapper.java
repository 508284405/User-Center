package com.leyue.usercenter.infrastructure.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.usercenter.infrastructure.role.dataobject.RoleDO;
import com.leyue.usercenter.infrastructure.role.dataobject.RoleMenuDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenuDO> {
} 