package com.yuwang.usercenter.domain.user.repository;

import com.yuwang.usercenter.domain.user.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuwang.usercenter.domain.user.entity.UserAdmin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface UserAdminMapper extends BaseMapper<UserAdmin> {
    @Select("SELECT * FROM user_admins WHERE username = #{username} LIMIT 1")
    Optional<User> findByUsername(String username);

    @Select("SELECT * FROM user_admins WHERE email = #{email} LIMIT 1")
    Optional<User> findByEmail(String email);

    @Select("SELECT * FROM user_admins WHERE phone = #{phone} LIMIT 1")
    Optional<User> findByPhone(String phone);

    @Select("SELECT COUNT(*) > 0 FROM user_admins WHERE username = #{username}")
    boolean existsByUsername(String username);

    @Select("SELECT COUNT(*) > 0 FROM user_admins WHERE email = #{email}")
    boolean existsByEmail(String email);

    @Select("SELECT COUNT(*) > 0 FROM user_admins WHERE phone = #{phone}")
    boolean existsByPhone(String phone);
}