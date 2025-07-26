package com.leyue.usercenter.infrastructure.user;

import com.alibaba.cola.dto.PageResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.usercenter.domain.user.User;
import com.leyue.usercenter.domain.user.gateway.UserGateway;
import com.leyue.usercenter.infrastructure.user.assembler.UserAssembler;
import com.leyue.usercenter.infrastructure.user.dataobject.UserDO;
import com.leyue.usercenter.infrastructure.user.mapper.UserMapper;
import com.leyue.usercenter.infrastructure.user.service.UserRoleService;
import com.leyue.usercenter.dto.query.UserPageQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class UserGatewayImpl implements UserGateway {
    private final UserMapper userMapper;
    private final UserRoleService userRoleService;
    private final UserAssembler userAssembler;
    
    @Override
    public User findById(Long id) {
        UserDO userDO = userMapper.selectById(id);
        return userAssembler.toDomain(userDO);
    }
    
    @Override
    public User findByUsername(String username) {
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getUsername, username);
        UserDO userDO = userMapper.selectOne(wrapper);
        return userAssembler.toDomain(userDO);
    }
    
    @Override
    public User findByGoogleId(String googleId) {
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getGoogleId, googleId);
        UserDO userDO = userMapper.selectOne(wrapper);
        return userAssembler.toDomain(userDO);
    }
    
    @Override
    public User findByWechatOpenId(String openId) {
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getWechatOpenId, openId);
        UserDO userDO = userMapper.selectOne(wrapper);
        return userAssembler.toDomain(userDO);
    }
    
    @Override
    public User findByAlipayId(String alipayId) {
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getAlipayId, alipayId);
        UserDO userDO = userMapper.selectOne(wrapper);
        return userAssembler.toDomain(userDO);
    }
    
    @Override
    public void save(User user) {
        UserDO userDO = userAssembler.toDO(user);
        if (userDO.getId() == null) {
            userDO.setCreatedAt(new Date());
            userDO.setUpdatedAt(new Date());
            userMapper.insert(userDO);
            user.setId(userDO.getId());
        } else {
            userDO.setUpdatedAt(new Date());
            userMapper.updateById(userDO);
        }
    }
    
    @Override
    public void delete(Long id) {
        userMapper.deleteById(id);
    }

    @Override
    public void assignRoles(Long userId, List<Long> roleIds) {
        // 先删除用户现有的角色关联
        userRoleService.deleteByUserId(userId);
        
        // 批量插入新的用户-角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            userRoleService.batchInsert(userId, roleIds);
        }
    }

    @Override
    public PageResponse<User> pageUsers(UserPageQuery query) {

        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getUsername())) {
            wrapper.like(UserDO::getUsername, query.getUsername());
        }
        if (StringUtils.hasText(query.getEmail())) {
            wrapper.like(UserDO::getEmail, query.getEmail());
        }
        if (query.getStatus() != null) {
            wrapper.eq(UserDO::getStatus, query.getStatus());
        }
        if (query.getIsGoogleUser() != null) {
            if (query.getIsGoogleUser()) {
                wrapper.isNotNull(UserDO::getGoogleId);
            } else {
                wrapper.isNull(UserDO::getGoogleId);
            }
        }
        
        IPage<UserDO> page = userMapper.selectPage(
            new Page<>(query.getPageNum(), query.getPageSize()),
            wrapper
        );
        
        List<User> users = page.getRecords().stream()
                .map(userAssembler::toDomain)
                .collect(Collectors.toList());
                
        return PageResponse.of(users, (int)page.getTotal(), 
                query.getPageNum(), query.getPageSize());
    }

    @Override
    public List<User> findByRoleCode(String roleCode) {
        List<UserDO> userDOs = userMapper.selectByRoleCode(roleCode);
        return userDOs.stream()
                .map(userAssembler::toDomain)
                .collect(Collectors.toList());
    }
}