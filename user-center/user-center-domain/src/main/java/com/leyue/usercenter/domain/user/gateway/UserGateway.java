package com.leyue.usercenter.domain.user.gateway;

import com.leyue.usercenter.domain.user.User;
import java.util.List;
import com.alibaba.cola.dto.PageResponse;
import com.leyue.usercenter.dto.query.UserPageQuery;

public interface UserGateway {
    User findById(Long id);
    User findByUsername(String username);
    User findByGoogleId(String googleId);
    User findByWechatOpenId(String openId);
    User findByAlipayId(String alipayId);
    void save(User user);
    void delete(Long id);
    void assignRoles(Long userId, List<Long> roleIds);
    PageResponse<User> pageUsers(UserPageQuery query);
    List<User> findByRoleCode(String roleCode);
}