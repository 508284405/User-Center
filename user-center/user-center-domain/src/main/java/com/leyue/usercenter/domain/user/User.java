package com.leyue.usercenter.domain.user;

import com.leyue.usercenter.domain.role.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class User {
    public void addRole(Role role) {
        if (roles == null) {
            roles = new HashSet<>();
        }
        roles.add(role);
    }
    private Long id;
    private String username;
    private String password;
    private String googleId;
    private String wechatOpenId;
    private String alipayId;
    private String email;
    private String avatar;
    private Integer userType; // 0-客户端用户 1-运营端用户
    private Integer status; // 0-禁用 1-启用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Set<Role> roles;

    public boolean isEnabled() {
        return status == 1;
    }

    // 领域行为
    public boolean hasPermission(String permission) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return roles.stream()
                .flatMap(role -> role.getMenus().stream())
                .flatMap(menu -> menu.getAllPermissions().stream())
                .anyMatch(p -> p.equals(permission));
    }

    public void assignRole(Role role) {
        if (roles == null) {
            roles = new HashSet<>();
        }
        roles.add(role);
    }

    public void assignRoles(List<Role> roles) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.clear();
        this.roles.addAll(roles);
    }

    public void removeRole(Role role) {
        if (roles == null || roles.isEmpty()) {
            return;
        }
        this.roles.remove(role);
    }

    public boolean hasRole(String roleCode) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return roles.stream()
                .anyMatch(role -> role.getRoleCode().equals(roleCode));
    }

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    public boolean isOperationUser() {
        return userType != null && userType == 1;
    }

    public boolean isClientUser() {
        return userType == null || userType == 0;
    }

    public Set<String> getAllPermissions() {
        Set<String> permissions = new HashSet<>();
        if (roles == null || roles.isEmpty()) {
            return new HashSet<>(0);
        }
        roles.forEach(role ->
                permissions.addAll(role.getAllPermissions())
        );
        return permissions;
    }

    public boolean isGoogleUser() {
        return googleId != null;
    }

    public void updateGoogleInfo(String googleId, String email) {
        this.googleId = googleId;
        this.email = email;
    }

    public void updateWechatInfo(String openId, String nickname, String avatar) {
        this.wechatOpenId = openId;
        this.username = nickname;
        this.avatar = avatar;
    }

    public boolean isWechatUser() {
        return wechatOpenId != null;
    }
    
    public boolean isAlipayUser() {
        return alipayId != null;
    }
    
    public void updateAlipayInfo(String alipayId, String nickname, String avatar) {
        this.alipayId = alipayId;
        if (nickname != null) {
            this.username = nickname;
        }
        if (avatar != null) {
            this.avatar = avatar;
        }
    }
}