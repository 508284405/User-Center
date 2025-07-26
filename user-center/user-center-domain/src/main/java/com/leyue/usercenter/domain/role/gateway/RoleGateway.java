package com.leyue.usercenter.domain.role.gateway;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.leyue.usercenter.domain.role.Role;
import com.leyue.usercenter.dto.RoleDTO;
import com.leyue.usercenter.dto.query.RolePageQuery;

import java.util.List;

public interface RoleGateway {
    Role findById(Long roleId);
    Role findByRoleCode(String roleCode);
    List<Role> findByUserId(Long userId);
    void save(Role role);
    void delete(Long roleId);
    PageResponse<Role> pageRoles(RolePageQuery query);

    Role findByCode(String roleCode);

    MultiResponse<RoleDTO> list();
}