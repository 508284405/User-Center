package com.leyue.usercenter.api;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.leyue.usercenter.dto.RoleDTO;
import com.leyue.usercenter.dto.command.CreateRoleCmd;
import com.leyue.usercenter.dto.command.UpdateRoleCmd;
import com.leyue.usercenter.dto.command.UpdateUserRoleCmd;
import com.leyue.usercenter.dto.query.RolePageQuery;
import jakarta.validation.Valid;

import java.util.List;

public interface RoleService {
    RoleDTO createRole(CreateRoleCmd cmd);

    RoleDTO updateRole(UpdateRoleCmd cmd);

    void deleteRole(Long roleId);

    RoleDTO getRole(Long roleId);

    List<RoleDTO> getUserRoles(Long userId);

    void assignMenus(Long roleId, List<Long> menuIds);

    PageResponse<RoleDTO> pageRoles(RolePageQuery query);

    MultiResponse<RoleDTO> list();

    void updateUserRole(Long userId, @Valid UpdateUserRoleCmd cmd);
}