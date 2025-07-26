package com.leyue.usercenter.app.user;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.api.UserService;
import com.leyue.usercenter.app.user.executor.*;
import com.leyue.usercenter.app.user.executor.query.UserPageQryExe;
import com.leyue.usercenter.domain.menu.gateway.MenuGateway;
import com.leyue.usercenter.domain.role.gateway.RoleGateway;
import com.leyue.usercenter.domain.user.User;
import com.leyue.usercenter.domain.user.gateway.UserGateway;
import com.leyue.usercenter.dto.AuthenticationResponse;
import com.leyue.usercenter.dto.UserDTO;
import com.leyue.usercenter.dto.WechatLoginStatusResponse;
import com.leyue.usercenter.dto.WechatQrResponse;
import com.leyue.usercenter.dto.command.*;
import com.leyue.usercenter.dto.query.UserPageQuery;
import com.leyue.usercenter.infrastructure.config.security.context.UserContext;
import com.leyue.usercenter.infrastructure.user.assembler.UserAssembler;
import com.leyue.usercenter.infrastructure.wechat.WechatClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserLoginCmdExe userLoginCmdExe;
    private final GoogleLoginCmdExe googleLoginCmdExe;
    private final CreateUserCmdExe createUserCmdExe;
    private final UpdateUserCmdExe updateUserCmdExe;
    private final DeleteUserCmdExe deleteUserCmdExe;
    private final UserPageQryExe userPageQryExe;
    private final UserGateway userGateway;
    private final RoleGateway roleGateway;
    private final MenuGateway menuGateway;
    private final UserAssembler userAssembler;
    private final WechatLoginCmdExe wechatLoginCmdExe;
    private final UserRegisterCmdExe userRegisterCmdExe;
    private final WechatQrCodeCmdExe wechatQrCodeCmdExe;
    private final WechatCallbackCmdExe wechatCallbackCmdExe;
    private final WechatClient wechatClient;


    @Override
    public AuthenticationResponse register(UserRegisterCmd cmd) {
        // 执行注册命令
        UserDTO dto = userRegisterCmdExe.execute(cmd);
        // 执行登陆命令
        UserLoginCmd loginCmd = userAssembler.toUserLoginCmd(cmd);
        AuthenticationResponse response = login(loginCmd);
        return response;
    }

    @Override
    public AuthenticationResponse login(UserLoginCmd cmd) {
        return userLoginCmdExe.execute(cmd);
    }

    @Override
    public AuthenticationResponse googleLogin(GoogleLoginCmd cmd) {
        return googleLoginCmdExe.execute(cmd);
    }

    @Override
    public AuthenticationResponse wechatLogin(WechatLoginCmd cmd) {
        return wechatLoginCmdExe.execute(cmd);
    }

    @Override
    public UserDTO createUser(CreateUserCmd cmd) {
        return createUserCmdExe.execute(cmd);
    }

    @Override
    public UserDTO updateUser(UpdateUserCmd cmd) {
        return updateUserCmdExe.execute(cmd);
    }

    @Override
    public void deleteUser(Long id) {
        deleteUserCmdExe.execute(id);
    }

    @Override
    public UserDTO getUser(Long userId) {
        User user = userGateway.findById(userId);
        return userAssembler.toDTO(user);
    }

    @Override
    public void updateStatus(Long userId, Integer status) {
        User user = userGateway.findById(userId);
        user.setStatus(status);
        userGateway.save(user);
    }

    @Override
    public PageResponse<UserDTO> pageUsers(UserPageQuery query) {
        return userPageQryExe.execute(query);
    }

    @Override
    public UserDTO getCurrentUserInfo() {
        UserContext.UserInfo userInfo = UserContext.getCurrentUser();
        // 补充其他信息
        User user = userGateway.findById(userInfo.getUserId());
        UserDTO dto = userAssembler.toDTO(userInfo);
        // 头像
        dto.setAvatar(user.getAvatar());
        dto.setJoinDate(user.getCreateTime());
        return dto;
    }

    @Override
    public SingleResponse<WechatQrResponse> generateWechatQrCode(WechatQrCodeCmd cmd) {
        return wechatQrCodeCmdExe.execute(cmd);
    }

    @Override
    public boolean handleWechatCallback(String code, String state) {
        return wechatCallbackCmdExe.execute(code, state);
    }

    @Override
    public SingleResponse<WechatLoginStatusResponse> getWechatLoginStatus(String state) {
        WechatLoginStatusResponse statusResponse = wechatClient.getLoginStatus(state);
        return SingleResponse.of(statusResponse);
    }

    @Override
    public List<UserDTO> listUsersByRoleCode(String roleCode) {
        List<User> users = userGateway.findByRoleCode(roleCode);
        return users.stream()
                .map(userAssembler::toDTO)
                .collect(Collectors.toList());
    }
}