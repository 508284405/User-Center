package com.leyue.usercenter.infrastructure.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.usercenter.infrastructure.user.dataobject.UserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
    UserDO selectByUsername(@Param("username") String username);
    
    UserDO selectByGoogleId(@Param("googleId") String googleId);

    List<UserDO> selectByRoleId(@Param("roleId") String roleId);
    
    List<UserDO> selectByRoleCode(@Param("roleCode") String roleCode);
} 