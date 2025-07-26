package com.leyue.usercenter.app.role;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.usercenter.api.RoleService;
import com.leyue.usercenter.app.role.executor.CreateRoleCmdExe;
import com.leyue.usercenter.app.role.executor.UpdateRoleCmdExe;
import com.leyue.usercenter.domain.menu.Menu;
import com.leyue.usercenter.domain.menu.gateway.MenuGateway;
import com.leyue.usercenter.domain.role.Role;
import com.leyue.usercenter.domain.role.gateway.RoleGateway;
import com.leyue.usercenter.domain.user.gateway.UserGateway;
import com.leyue.usercenter.dto.RoleDTO;
import com.leyue.usercenter.dto.command.CreateRoleCmd;
import com.leyue.usercenter.dto.command.UpdateRoleCmd;
import com.leyue.usercenter.dto.command.UpdateUserRoleCmd;
import com.leyue.usercenter.dto.query.RolePageQuery;
import com.leyue.usercenter.infrastructure.role.assembler.RoleAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final CreateRoleCmdExe createRoleCmdExe;
    private final UpdateRoleCmdExe updateRoleCmdExe;
    private final RoleGateway roleGateway;
    private final MenuGateway menuGateway;
    private final UserGateway userGateway;

    @Override
    public RoleDTO createRole(CreateRoleCmd cmd) {
        return createRoleCmdExe.execute(cmd);
    }

    @Override
    public RoleDTO updateRole(UpdateRoleCmd cmd) {
        return updateRoleCmdExe.execute(cmd);
    }

    @Override
    public void deleteRole(Long roleId) {
        roleGateway.delete(roleId);
    }

    @Override
    public RoleDTO getRole(Long roleId) {
        Role role = roleGateway.findById(roleId);
        return RoleAssembler.INSTANCE.toDTO(role);
    }

    @Override
    public List<RoleDTO> getUserRoles(Long userId) {
        List<Role> roles = roleGateway.findByUserId(userId);
        return roles.stream()
                .map(RoleAssembler.INSTANCE::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void assignMenus(Long roleId, List<Long> menuIds) {
        Role role = roleGateway.findById(roleId);
        if (role == null) {
            throw new BizException("角色不存在");
        }

        List<Menu> menus = menuIds.stream()
                .map(menuGateway::findById)
                .collect(Collectors.toList());
        role.setMenus(menus);
        roleGateway.save(role);
    }

    @Override
    public PageResponse<RoleDTO> pageRoles(RolePageQuery query) {
        PageResponse<Role> pageResponse = roleGateway.pageRoles(query);
        return PageResponse.of(pageResponse.getData().stream().map(RoleAssembler.INSTANCE::toDTO).collect(Collectors.toList())
                , pageResponse.getTotalCount(), pageResponse.getPageIndex(), pageResponse.getPageSize());
    }

    @Override
    public MultiResponse<RoleDTO> list() {
        return roleGateway.list();
    }

    @Override
    @Transactional
    public void updateUserRole(Long userId, @Valid UpdateUserRoleCmd cmd) {
        userGateway.assignRoles(userId, cmd.getRoleIds());
    }
}