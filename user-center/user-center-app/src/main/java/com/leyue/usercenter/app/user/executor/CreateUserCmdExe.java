package com.leyue.usercenter.app.user.executor;

import java.util.Set;
import java.util.stream.Collectors;

import com.leyue.usercenter.domain.role.Role;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

import com.alibaba.cola.exception.BizException;
import com.leyue.usercenter.domain.role.gateway.RoleGateway;
import com.leyue.usercenter.domain.user.User;
import com.leyue.usercenter.domain.user.gateway.UserGateway;
import com.leyue.usercenter.dto.UserDTO;
import com.leyue.usercenter.dto.command.CreateUserCmd;
import com.leyue.usercenter.infrastructure.user.assembler.UserAssembler;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CreateUserCmdExe {
    private final UserGateway userGateway;
    private final RoleGateway roleGateway;
    private final UserAssembler userAssembler;

    public UserDTO execute(CreateUserCmd cmd) {
        User existUser = userGateway.findByUsername(cmd.getUsername());
        if (existUser != null) {
            throw new BizException("用户名已存在");
        }

        User user = new User();
        user.setUsername(cmd.getUsername());
        user.setPassword(BCrypt.hashpw(cmd.getPassword(), BCrypt.gensalt()));
        user.setEmail(cmd.getEmail());
        user.setAvatar(cmd.getAvatar());
        user.setStatus(1);

        if (cmd.getRoleIds() != null) {
            Set<Role> roles = cmd.getRoleIds().stream()
                    .map(roleGateway::findById)
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }
        userGateway.save(user);
        return userAssembler.toDTO(user);
    }
}