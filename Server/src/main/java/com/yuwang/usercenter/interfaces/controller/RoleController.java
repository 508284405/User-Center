package com.yuwang.usercenter.interfaces.controller;

import com.yuwang.usercenter.domain.role.dto.RolePageQuery;
import com.yuwang.usercenter.domain.role.entity.Role;
import com.yuwang.usercenter.domain.role.service.RoleDomainService;
import com.yuwang.usercenter.infrastructure.common.BaseResult;
import com.yuwang.usercenter.infrastructure.common.BasePageResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private final RoleDomainService roleDomainService;

    public RoleController(RoleDomainService roleDomainService) {
        this.roleDomainService = roleDomainService;
    }

    @PostMapping

    public BaseResult<Role> createRole(@RequestBody Role role) {
        return BaseResult.success(roleDomainService.createRole(role));
    }

    @PutMapping("/{id}")

    public BaseResult<Role> updateRole(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        return BaseResult.success(roleDomainService.updateRole(role));
    }

    @DeleteMapping("/{id}")

    public BaseResult<Void> deleteRole(@PathVariable Long id) {
        roleDomainService.deleteRole(id);
        return BaseResult.success();
    }

    @GetMapping("/{id}")

    public BaseResult<Role> getRole(@PathVariable Long id) {
        return roleDomainService.findById(id)
                .map(BaseResult::success)
                .orElse(BaseResult.error(404, "角色不存在"));
    }

    @GetMapping("/name/{roleName}")

    public BaseResult<Role> getRoleByName(@PathVariable String roleName) {
        return roleDomainService.findByRoleName(roleName)
                .map(BaseResult::success)
                .orElse(BaseResult.error(404, "角色不存在"));
    }

    @GetMapping("/system/{systemId}")
    public BaseResult<List<Role>> getRolesBySystemId(@PathVariable Long systemId) {
        return BaseResult.success(roleDomainService.findBySystemId(systemId));
    }

    @GetMapping("/level/{level}")
    public BaseResult<List<Role>> getRolesByLevel(@PathVariable Integer level) {
        return BaseResult.success(roleDomainService.findByLevel(level));
    }

    @GetMapping
    public BaseResult<List<Role>> getAllRoles() {
        return BaseResult.success(roleDomainService.findAllRoles());
    }

    @PostMapping("/page")
    public BasePageResult<Role> getRolePage(@RequestBody RolePageQuery query) {
        return roleDomainService.findRolePage(query);
    }
}