package com.yuwang.usercenter.interfaces.controller;

import com.yuwang.usercenter.domain.log.entity.OperationLog;
import com.yuwang.usercenter.domain.log.service.OperationLogDomainService;
import com.yuwang.usercenter.domain.role.dto.RoleCreateRequest;
import com.yuwang.usercenter.domain.role.dto.RolePageQuery;
import com.yuwang.usercenter.domain.role.dto.RoleUpdateRequest;
import com.yuwang.usercenter.domain.role.entity.Role;
import com.yuwang.usercenter.domain.role.service.RoleDomainService;
import com.yuwang.usercenter.infrastructure.common.BaseResult;
import com.yuwang.usercenter.infrastructure.common.BasePageResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private final RoleDomainService roleDomainService;
    private final OperationLogDomainService operationLogDomainService;

    public RoleController(RoleDomainService roleDomainService, OperationLogDomainService operationLogDomainService) {
        this.roleDomainService = roleDomainService;
        this.operationLogDomainService = operationLogDomainService;
    }

    /**
     * 创建新角色
     * @param createRequest 角色信息
     * @return 创建成功的角色信息
     */
    @PostMapping
    public BaseResult<Role> createRole(@RequestBody @Validated RoleCreateRequest createRequest, HttpServletRequest request) {
        Role role = new Role();
        BeanUtils.copyProperties(createRequest, role);
        Role createdRole = roleDomainService.createRole(role);

        // 记录操作日志
        OperationLog log = new OperationLog();
        log.setUserId(0L); // 系统操作
        log.setUsername("system");
        log.setOperationType("CREATE");
        log.setModule("ROLE");
        log.setTargetId(createdRole.getId());
        log.setDescription("创建角色：" + createdRole.getRoleName());
        log.setIpAddress(request.getRemoteAddr());
        operationLogDomainService.createLog(log);

        return BaseResult.success(createdRole);
    }

    /**
     * 更新角色信息
     * @param id 角色ID
     * @param updateRequest 更新的角色信息
     * @return 更新后的角色信息
     */
    @PutMapping("/{id}")
    public BaseResult<Role> updateRole(@PathVariable Long id, @RequestBody @Validated RoleUpdateRequest updateRequest, HttpServletRequest request) {
        Role role = new Role();
        BeanUtils.copyProperties(updateRequest, role);
        role.setId(id);
        Role updatedRole = roleDomainService.updateRole(role);

        // 记录操作日志
        OperationLog log = new OperationLog();
        log.setUserId(0L); // 系统操作
        log.setUsername("system");
        log.setOperationType("UPDATE");
        log.setModule("ROLE");
        log.setTargetId(updatedRole.getId());
        log.setDescription("更新角色：" + updatedRole.getRoleName());
        log.setIpAddress(request.getRemoteAddr());
        operationLogDomainService.createLog(log);

        return BaseResult.success(updatedRole);
    }

    /**
     * 删除角色
     * @param id 要删除的角色ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public BaseResult<Void> deleteRole(@PathVariable Long id, HttpServletRequest request) {
        Role role = roleDomainService.findById(id).orElseThrow(() -> new IllegalArgumentException("角色不存在"));
        roleDomainService.deleteRole(id);

        // 记录操作日志
        OperationLog log = new OperationLog();
        log.setUserId(0L); // 系统操作
        log.setUsername("system");
        log.setOperationType("DELETE");
        log.setModule("ROLE");
        log.setTargetId(id);
        log.setDescription("删除角色：" + role.getRoleName());
        log.setIpAddress(request.getRemoteAddr());
        operationLogDomainService.createLog(log);

        return BaseResult.success();
    }

    /**
     * 根据ID获取角色信息
     * @param id 角色ID
     * @return 角色信息
     */
    @GetMapping("/{id}")
    public BaseResult<Role> getRole(@PathVariable Long id) {
        return roleDomainService.findById(id)
                .map(BaseResult::success)
                .orElse(BaseResult.error(404, "角色不存在"));
    }

    /**
     * 根据角色名称获取角色信息
     * @param roleName 角色名称
     * @return 角色信息
     */
    @GetMapping("/name/{roleName}")
    public BaseResult<Role> getRoleByName(@PathVariable String roleName) {
        return roleDomainService.findByRoleName(roleName)
                .map(BaseResult::success)
                .orElse(BaseResult.error(404, "角色不存在"));
    }

    /**
     * 获取指定系统的所有角色
     * @param systemId 系统ID
     * @return 角色列表
     */
    @GetMapping("/system/{systemId}")
    public BaseResult<List<Role>> getRolesBySystemId(@PathVariable Long systemId) {
        return BaseResult.success(roleDomainService.findBySystemId(systemId));
    }

    /**
     * 获取指定级别的所有角色
     * @param level 角色级别
     * @return 角色列表
     */
    @GetMapping("/level/{level}")
    public BaseResult<List<Role>> getRolesByLevel(@PathVariable Integer level) {
        return BaseResult.success(roleDomainService.findByLevel(level));
    }

    /**
     * 获取所有角色
     * @return 所有角色列表
     */
    @GetMapping
    public BaseResult<List<Role>> getAllRoles() {
        return BaseResult.success(roleDomainService.findAllRoles());
    }

    /**
     * 分页查询角色列表
     * @param query 分页查询参数
     * @return 分页角色数据
     */
    @PostMapping("/page")
    public BasePageResult<Role> getRolePage(@RequestBody RolePageQuery query) {
        return roleDomainService.findRolePage(query);
    }
}