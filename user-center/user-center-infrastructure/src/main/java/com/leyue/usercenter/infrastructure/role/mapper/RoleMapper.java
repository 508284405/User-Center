package com.leyue.usercenter.infrastructure.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.usercenter.infrastructure.role.dataobject.RoleDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<RoleDO> {
    List<RoleDO> selectByUserId(@Param("userId") Long userId);
    void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
    void deleteUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
    void deleteUserRoleByUserId(@Param("userId") Long userId);
} 