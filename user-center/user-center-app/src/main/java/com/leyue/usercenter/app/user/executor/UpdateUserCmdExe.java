package com.leyue.usercenter.app.user.executor;

import java.util.Set;
import java.util.stream.Collectors;

import com.leyue.usercenter.domain.role.Role;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.cola.exception.BizException;
import com.leyue.usercenter.domain.role.gateway.RoleGateway;
import com.leyue.usercenter.domain.user.User;
import com.leyue.usercenter.domain.user.gateway.UserGateway;
import com.leyue.usercenter.dto.UserDTO;
import com.leyue.usercenter.dto.command.UpdateUserCmd;
import com.leyue.usercenter.infrastructure.user.assembler.UserAssembler;

import lombok.RequiredArgsConstructor;
@Component
@RequiredArgsConstructor
public class UpdateUserCmdExe {
    private final UserGateway userGateway;
    private final RoleGateway roleGateway;
    private final UserAssembler userAssembler;

    public UserDTO execute(UpdateUserCmd cmd) {
        User user = userGateway.findById(cmd.getId());
        if (user == null) {
            throw new BizException("用户不存在");
        }

        if (StringUtils.hasText(cmd.getPassword())) {
            user.setPassword(BCrypt.hashpw(cmd.getPassword(), BCrypt.gensalt()));
        }
        if (StringUtils.hasText(cmd.getEmail())) {
            user.setEmail(cmd.getEmail());
        }
        if (StringUtils.hasText(cmd.getAvatar())) {
            user.setAvatar(cmd.getAvatar());
        }
        if (cmd.getStatus() != null) {
            user.setStatus(cmd.getStatus());
        }

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