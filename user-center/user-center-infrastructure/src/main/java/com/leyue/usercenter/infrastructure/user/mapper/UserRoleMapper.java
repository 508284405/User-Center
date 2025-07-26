package com.leyue.usercenter.infrastructure.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.usercenter.infrastructure.user.dataobject.UserDO;
import com.leyue.usercenter.infrastructure.user.dataobject.UserRoleDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRoleDO> {
    void insertBatch(@Param("userId") Long userId,@Param("roleIds") List<Long> roleIds);
}