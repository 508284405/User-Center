package com.yuwang.usercenter.domain.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuwang.usercenter.domain.user.entity.User;
import com.yuwang.usercenter.domain.user.entity.UserStatus;
import com.yuwang.usercenter.domain.user.repository.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserDomainService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserDomainService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(User user) {
        // 检查用户名是否已存在
        if (userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()))) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (user.getEmail() != null && userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getEmail, user.getEmail()))) {
            throw new IllegalArgumentException("邮箱已被使用");
        }

        // 检查手机号是否已存在
        if (user.getPhone() != null && userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getPhone, user.getPhone()))) {
            throw new IllegalArgumentException("手机号已被使用");
        }

        // 加密密码
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        
        userMapper.insert(user);
        return user;
    }

    @Transactional
    public User updateUser(User user) {
        userMapper.updateById(user);
        return user;
    }

    @Transactional
    public void lockUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setStatus(UserStatus.LOCKED);
            userMapper.updateById(user);
        }
    }

    @Transactional
    public void unlockUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setStatus(UserStatus.ACTIVE);
            userMapper.updateById(user);
        }
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username)));
    }

    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email)));
    }

    public Optional<User> findByPhone(String phone) {
        return Optional.ofNullable(userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone)));
    }

    public boolean verifyPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }
}