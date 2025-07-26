package com.leyue.usercenter.app.user.executor;

import com.alibaba.cola.exception.BizException;
import com.leyue.usercenter.domain.user.User;
import com.leyue.usercenter.domain.user.gateway.UserGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class DeleteUserCmdExe {
    private final UserGateway userGateway;

    public void execute(Long userId) {
        User user = userGateway.findById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        userGateway.delete(userId);
    }
} 