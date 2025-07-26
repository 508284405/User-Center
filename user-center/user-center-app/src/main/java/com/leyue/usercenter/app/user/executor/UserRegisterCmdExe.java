package com.leyue.usercenter.app.user.executor;

import com.alibaba.cola.exception.BizException;
import com.leyue.usercenter.domain.role.Role;
import com.leyue.usercenter.domain.role.gateway.RoleGateway;
import com.leyue.usercenter.domain.user.User;
import com.leyue.usercenter.domain.user.gateway.UserGateway;
import com.leyue.usercenter.dto.UserDTO;
import com.leyue.usercenter.dto.command.UserRegisterCmd;
import com.leyue.usercenter.infrastructure.user.assembler.UserAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class UserRegisterCmdExe {
    private final UserGateway userGateway;
    private final UserAssembler userAssembler;
    private final RoleGateway roleGateway;

    public UserDTO execute(UserRegisterCmd cmd) {
        // 检查用户名是否已存在
        User existUser = userGateway.findByUsername(cmd.getUsername());
        if (existUser != null) {
            throw new BizException("用户名已存在");
        }

        // 创建用户逻辑
        User user = new User();
        user.setUsername(cmd.getUsername());
        user.setPassword(BCrypt.hashpw(cmd.getPassword(), BCrypt.gensalt()));
        user.setStatus(1); // 默认启用状态

        // 查询默认角色
        Role defaultRole = roleGateway.findByRoleCode("USER");

        // 分配默认角色
        user.addRole(defaultRole);

        // 保存用户基本信息
        userGateway.save(user);

        // 保存用户与角色的关联关系
        userGateway.assignRoles(user.getId(), Collections.singletonList(defaultRole.getId()));

        return userAssembler.toDTO(user);
    }
}