package com.yuwang.usercenter.domain.user.service;

import com.yuwang.usercenter.domain.user.dto.UserCreateRequest;
import com.yuwang.usercenter.domain.user.dto.UserPageQuery;
import com.yuwang.usercenter.domain.user.dto.UserUpdateRequest;
import com.yuwang.usercenter.domain.user.entity.User;
import com.yuwang.usercenter.domain.user.entity.UserAdmin;
import com.yuwang.usercenter.domain.user.entity.UserStatus;
import com.yuwang.usercenter.infrastructure.common.BasePageResult;

import java.util.Optional;

public interface UserAdminDomainService {
    /**
     * 管理员创建用户
     * @param user 用户信息
     * @return 创建的用户
     */
    void adminCreateUser(UserAdmin user);

    /**
     * 管理员更新用户信息
     * @param user 用户信息
     * @return 更新后的用户
     */
    void adminUpdateUser(UserAdmin user);

    /**
     * 管理员删除用户
     * @param id 用户ID
     */
    void adminDeleteUser(Long id);

    /**
     * 管理员查询用户
     * @param id 用户ID
     * @return 用户信息
     */
    Optional<UserAdmin> adminFindById(Long id);

    /**
     * 管理员分页查询用户
     * @param query 查询条件
     * @return 分页结果
     */
    BasePageResult<UserAdmin> adminFindUserPage(UserPageQuery query);

    /**
     * 管理员更新用户状态
     * @param id 用户ID
     * @param status 新状态
     */
    void adminUpdateUserStatus(Long id, UserStatus status);

    /**
     * 管理员重置用户密码
     * @param id 用户ID
     * @param newPassword 新密码
     */
    void adminResetPassword(Long id, String newPassword);

    /**
     * 根据用户名查找运营用户
     * @param username 用户名
     * @return 用户信息
     */
    Optional<UserAdmin> adminFindByUsername(String username);
}