package com.yuwang.usercenter.interfaces.admin;

import com.yuwang.usercenter.domain.log.service.OperationLogDomainService;
import com.yuwang.usercenter.domain.user.dto.UserAdminCreateRequest;
import com.yuwang.usercenter.domain.user.dto.UserCreateRequest;
import com.yuwang.usercenter.domain.user.dto.UserPageQuery;
import com.yuwang.usercenter.domain.user.dto.UserUpdateRequest;
import com.yuwang.usercenter.domain.user.entity.User;
import com.yuwang.usercenter.domain.user.entity.UserAdmin;
import com.yuwang.usercenter.domain.user.entity.UserStatus;
import com.yuwang.usercenter.domain.user.service.UserAdminDomainService;
import com.yuwang.usercenter.infrastructure.common.BasePageResult;
import com.yuwang.usercenter.infrastructure.common.BaseResult;
import com.yuwang.usercenter.infrastructure.exception.BusinessException;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {
    private final UserAdminDomainService userAdminDomainService;
    private final OperationLogDomainService operationLogDomainService;

    public UserAdminController(UserAdminDomainService userAdminDomainService,
                             OperationLogDomainService operationLogDomainService) {
        this.userAdminDomainService = userAdminDomainService;
        this.operationLogDomainService = operationLogDomainService;
    }

    @PostMapping
    public BaseResult<Void> createUser(@Valid @RequestBody UserAdminCreateRequest request) {
        UserAdmin user = new UserAdmin();
        user.setUsername(request.getUsername());
        user.setPasswordHash(request.getPassword());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAvatarUrl(request.getAvatar());
        user.setStatus(request.getStatus());

        userAdminDomainService.adminCreateUser(user);
        return BaseResult.success();
    }

    @PutMapping("/{id}")
    public BaseResult<Void> updateUser(@PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        UserAdmin user = new UserAdmin();
        user.setId(id);
        user.setUsername(request.getUsername());
        user.setPasswordHash(request.getPassword());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAvatarUrl(request.getAvatar());
        user.setStatus(request.getStatus());

        userAdminDomainService.adminUpdateUser(user);
        return BaseResult.success();
    }

    @DeleteMapping("/{id}")
    public BaseResult<Void> deleteUser(@PathVariable Long id) {
        UserAdmin user = userAdminDomainService.adminFindById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        userAdminDomainService.adminDeleteUser(id);
        operationLogDomainService.recordOperation("DELETE", "USER", id, 
                "删除用户：" + user.getUsername());

        return BaseResult.success();
    }

    @GetMapping("/{id}")
    public BaseResult<UserAdmin> getUser(@PathVariable Long id) {
        return BaseResult.success(userAdminDomainService.adminFindById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在")));
    }

    @GetMapping
    public BasePageResult<UserAdmin> getUserPage(@Valid UserPageQuery query) {
        return userAdminDomainService.adminFindUserPage(query);
    }

    @PutMapping("/{id}/status")
    public BaseResult<Void> updateUserStatus(@PathVariable Long id, 
            @RequestParam UserStatus status) {
        userAdminDomainService.adminUpdateUserStatus(id, status);
        operationLogDomainService.recordOperation("UPDATE", "USER", id, 
                "更新用户状态：" + status);
        return BaseResult.success();
    }

    @GetMapping("/profile")
    public BaseResult<UserAdmin> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new BusinessException("用户未登录");
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userAdminDomainService.adminFindByUsername(userDetails.getUsername())
                .map(BaseResult::success)
                .orElseThrow(() -> new BusinessException("用户信息不存在"));
    }

    @PutMapping("/{id}/password")
    public BaseResult<Void> resetPassword(@PathVariable Long id, 
            @RequestParam String newPassword) {
        userAdminDomainService.adminResetPassword(id, newPassword);
        operationLogDomainService.recordOperation("UPDATE", "USER", id, 
                "重置用户密码");
        return BaseResult.success();
    }
}