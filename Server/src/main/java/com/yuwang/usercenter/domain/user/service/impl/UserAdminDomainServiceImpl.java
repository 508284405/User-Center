package com.yuwang.usercenter.domain.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuwang.usercenter.domain.user.dto.UserPageQuery;
import com.yuwang.usercenter.domain.user.entity.UserAdmin;
import com.yuwang.usercenter.domain.user.entity.UserStatus;
import com.yuwang.usercenter.domain.user.repository.UserAdminMapper;
import com.yuwang.usercenter.domain.user.service.UserAdminDomainService;
import com.yuwang.usercenter.infrastructure.common.BasePageResult;
import com.yuwang.usercenter.infrastructure.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserAdminDomainServiceImpl implements UserAdminDomainService {
    private final UserAdminMapper userAdminMapper;
    private final PasswordEncoder passwordEncoder;

    public UserAdminDomainServiceImpl(UserAdminMapper userAdminMapper, PasswordEncoder passwordEncoder) {
        this.userAdminMapper = userAdminMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void adminCreateUser(UserAdmin user) {
        // 管理员创建用户时的特殊处理
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        userAdminMapper.insert(user);
    }

    @Override
    public void adminUpdateUser(UserAdmin user) {
        UserAdmin existingUser = userAdminMapper.selectById(user.getId());
        if (existingUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 管理员更新用户时的特殊处理
        if (user.getPasswordHash() != null) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }

        userAdminMapper.updateById(user);
    }

    @Override
    public void adminDeleteUser(Long id) {
        UserAdmin userAdmin = userAdminMapper.selectById(id);
        if (userAdmin == null) {
            throw new BusinessException("用户不存在");
        }
        userAdminMapper.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAdmin> adminFindById(Long id) {
        return Optional.ofNullable(userAdminMapper.selectById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAdmin> adminFindByUsername(String username) {
        return Optional.ofNullable(userAdminMapper.selectOne(
            new LambdaQueryWrapper<UserAdmin>().eq(UserAdmin::getUsername, username)
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public BasePageResult<UserAdmin> adminFindUserPage(UserPageQuery query) {
        Page<UserAdmin> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<UserAdmin> wrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        wrapper.like(StringUtils.isNotBlank(query.getUsername()), UserAdmin::getUsername, query.getUsername())
               .like(StringUtils.isNotBlank(query.getEmail()), UserAdmin::getEmail, query.getEmail())
               .like(StringUtils.isNotBlank(query.getPhone()), UserAdmin::getPhone, query.getPhone())
               .eq(query.getStatus() != null, UserAdmin::getStatus, query.getStatus())
               .orderByDesc(UserAdmin::getCreatedAt);
        
        Page<UserAdmin> userPage = userAdminMapper.selectPage(page, wrapper);
        return BasePageResult.success(userPage.getRecords(), query.getPageNum(), query.getPageSize(), userPage.getTotal());
    }

    @Override
    public void adminUpdateUserStatus(Long id, UserStatus status) {
        UserAdmin user = userAdminMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setStatus(status);
        userAdminMapper.updateById(user);
    }

    @Override
    public void adminResetPassword(Long id, String newPassword) {
        UserAdmin user = userAdminMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userAdminMapper.updateById(user);
    }
}