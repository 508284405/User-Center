package com.yuwang.usercenter.domain.user.service;

import com.yuwang.usercenter.domain.user.dto.UserPageQuery;
import com.yuwang.usercenter.domain.user.entity.User;
import com.yuwang.usercenter.infrastructure.common.BasePageResult;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface UserDomainService {

    User createUser(User user);

    User updateUser(User user);

    void lockUser(Long userId);

    void unlockUser(Long userId);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    Optional<User> findById(Long userId);

    boolean verifyPassword(User user, String rawPassword);

    BasePageResult<User> findUserPage(UserPageQuery query);
}