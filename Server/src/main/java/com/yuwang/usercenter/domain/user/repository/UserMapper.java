package com.yuwang.usercenter.domain.user.repository;

import com.yuwang.usercenter.domain.user.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT * FROM users WHERE username = #{username} LIMIT 1")
    Optional<User> findByUsername(String username);

    @Select("SELECT * FROM users WHERE email = #{email} LIMIT 1")
    Optional<User> findByEmail(String email);

    @Select("SELECT * FROM users WHERE phone = #{phone} LIMIT 1")
    Optional<User> findByPhone(String phone);

    @Select("SELECT COUNT(*) > 0 FROM users WHERE username = #{username}")
    boolean existsByUsername(String username);

    @Select("SELECT COUNT(*) > 0 FROM users WHERE email = #{email}")
    boolean existsByEmail(String email);

    @Select("SELECT COUNT(*) > 0 FROM users WHERE phone = #{phone}")
    boolean existsByPhone(String phone);
}