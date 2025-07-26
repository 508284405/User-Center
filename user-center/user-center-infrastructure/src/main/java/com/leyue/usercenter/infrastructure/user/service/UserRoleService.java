package com.leyue.usercenter.infrastructure.user.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.leyue.usercenter.infrastructure.role.mapper.RoleMapper;
import com.leyue.usercenter.infrastructure.user.dataobject.UserRoleDO;
import com.leyue.usercenter.infrastructure.user.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRoleService {
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;


    public void deleteByUserId(Long userId) {
        userRoleMapper.delete(Wrappers.<UserRoleDO>lambdaQuery()
                .eq(UserRoleDO::getUserId, userId));
    }

    public void batchInsert(Long userId, List<Long> roleIds) {
        userRoleMapper.insertBatch(userId, roleIds);
    }
}