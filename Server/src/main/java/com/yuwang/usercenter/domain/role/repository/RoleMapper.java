package com.yuwang.usercenter.domain.role.repository;

import com.yuwang.usercenter.domain.role.entity.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    @Select("SELECT * FROM roles WHERE role_name = #{roleName} LIMIT 1")
    Optional<Role> findByRoleName(String roleName);

    @Select("SELECT * FROM roles WHERE system_id = #{systemId}")
    List<Role> findBySystemId(Long systemId);

    @Select("SELECT COUNT(*) > 0 FROM roles WHERE role_name = #{roleName}")
    boolean existsByRoleName(String roleName);

    @Select("SELECT * FROM roles WHERE level = #{level}")
    List<Role> findByLevel(Integer level);
}