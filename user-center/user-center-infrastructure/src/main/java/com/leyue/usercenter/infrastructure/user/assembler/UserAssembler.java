package com.leyue.usercenter.infrastructure.user.assembler;

import com.leyue.usercenter.domain.role.Role;
import com.leyue.usercenter.domain.user.User;
import com.leyue.usercenter.dto.UserDTO;
import com.leyue.usercenter.dto.command.UserLoginCmd;
import com.leyue.usercenter.dto.command.UserRegisterCmd;
import com.leyue.usercenter.infrastructure.config.security.context.UserContext;
import com.leyue.usercenter.infrastructure.user.dataobject.UserDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 用户装配器
 * 负责在 User 实体和 UserDTO 之间进行转换
 *
 * @author Trae
 * @since 2024-01-20
 */
@Mapper(componentModel = "spring")
public interface UserAssembler {

    UserDTO toDTO(User user);

    @Mapping(target = "roles", ignore = true)
    User toDomain(UserDO userDO);

    UserDO toDO(User user);

    UserLoginCmd toUserLoginCmd(UserRegisterCmd cmd);

    default List<String> map(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) return new ArrayList<>(0);
        return roles.stream()
                .map(Role::getRoleCode)
                .collect(java.util.stream.Collectors.toList());
    }

    @Mapping(target = "userId", source = "id")
    UserContext.UserInfo toUserInfo(User user);

    @Mapping(target = "id", source = "userId")
    UserDTO toDTO(UserContext.UserInfo userInfo);
}