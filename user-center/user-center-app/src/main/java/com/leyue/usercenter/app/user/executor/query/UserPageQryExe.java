package com.leyue.usercenter.app.user.executor.query;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.usercenter.domain.user.User;
import com.leyue.usercenter.domain.user.gateway.UserGateway;
import com.leyue.usercenter.dto.UserDTO;
import com.leyue.usercenter.dto.query.UserPageQuery;
import com.leyue.usercenter.infrastructure.user.assembler.UserAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
@Component
@RequiredArgsConstructor
public class UserPageQryExe {
    private final UserGateway userGateway;
    private final UserAssembler userAssembler;

    public PageResponse<UserDTO> execute(UserPageQuery query) {
        PageResponse<User> pageResponse = userGateway.pageUsers(query);
        List<UserDTO> userDTOs = pageResponse.getData().stream()
                .map(userAssembler::toDTO)
                .collect(Collectors.toList());
        return PageResponse.of(userDTOs, pageResponse.getTotalCount(),
                pageResponse.getPageSize(), pageResponse.getPageIndex());
    }
} 