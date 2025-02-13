package com.yuwang.usercenter.interfaces.controller;

import com.yuwang.usercenter.domain.log.entity.OperationLog;
import com.yuwang.usercenter.domain.log.service.OperationLogDomainService;
import com.yuwang.usercenter.domain.user.dto.UserCreateRequest;
import com.yuwang.usercenter.domain.user.dto.UserPageQuery;
import com.yuwang.usercenter.domain.user.dto.UserUpdateRequest;
import com.yuwang.usercenter.domain.user.entity.User;
import com.yuwang.usercenter.domain.user.service.UserDomainService;
import jakarta.servlet.http.HttpServletRequest;
import com.yuwang.usercenter.infrastructure.common.BaseResult;
import com.yuwang.usercenter.infrastructure.common.BasePageResult;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserDomainService userDomainService;
    private final OperationLogDomainService operationLogDomainService;

    public UserController(UserDomainService userDomainService, OperationLogDomainService operationLogDomainService) {
        this.userDomainService = userDomainService;
        this.operationLogDomainService = operationLogDomainService;
    }

    /**
     * 创建新用户
     * @param createRequest 用户信息
     * @param request HTTP请求对象，用于获取IP地址
     * @return 创建成功的用户信息
     */
    @PostMapping
    public BaseResult<User> createUser(@RequestBody @Validated UserCreateRequest createRequest, HttpServletRequest request) {
        User user = new User();
        BeanUtils.copyProperties(createRequest, user);
        User createdUser = userDomainService.createUser(user);

        // 记录操作日志
        OperationLog log = new OperationLog();
        log.setUserId(createdUser.getId());
        log.setUsername(createdUser.getUsername());
        log.setOperationType("CREATE");
        log.setModule("USER");
        log.setTargetId(createdUser.getId());
        log.setDescription("创建用户：" + createdUser.getUsername());
        log.setIpAddress(request.getRemoteAddr());
        operationLogDomainService.createLog(log);

        return BaseResult.success(createdUser);
    }

    /**
     * 更新用户信息
     * @param id 用户ID
     * @param updateRequest 更新的用户信息
     * @param request HTTP请求对象，用于获取IP地址
     * @return 更新后的用户信息
     */
    @PutMapping("/{id}")
    public BaseResult<User> updateUser(@PathVariable Long id, @RequestBody @Validated UserUpdateRequest updateRequest, HttpServletRequest request) {
        User user = new User();
        BeanUtils.copyProperties(updateRequest, user);
        user.setId(id);
        User updatedUser = userDomainService.updateUser(user);

        // 记录操作日志
        OperationLog log = new OperationLog();
        log.setUserId(updatedUser.getId());
        log.setUsername(updatedUser.getUsername());
        log.setOperationType("UPDATE");
        log.setModule("USER");
        log.setTargetId(updatedUser.getId());
        log.setDescription("更新用户信息：" + updatedUser.getUsername());
        log.setIpAddress(request.getRemoteAddr());
        operationLogDomainService.createLog(log);

        return BaseResult.success(updatedUser);
    }

    /**
     * 锁定用户
     * @param id 要锁定的用户ID
     * @param request HTTP请求对象，用于获取IP地址
     * @return 操作结果
     */
    @PutMapping("/{id}/lock")
    public BaseResult<Void> lockUser(@PathVariable Long id, HttpServletRequest request) {
        User user = userDomainService.findById(id).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        userDomainService.lockUser(id);
        // 记录操作日志
        OperationLog log = new OperationLog();
        log.setUserId(user.getId());
        log.setUsername(user.getUsername());
        log.setOperationType("UPDATE");
        log.setModule("USER");
        log.setTargetId(user.getId());
        log.setDescription("锁定用户：" + user.getUsername());
        log.setIpAddress(request.getRemoteAddr());
        operationLogDomainService.createLog(log);

        return BaseResult.success();
    }

    /**
     * 解锁用户
     * @param id 要解锁的用户ID
     * @param request HTTP请求对象，用于获取IP地址
     * @return 操作结果
     */
    @PutMapping("/{id}/unlock")
    public BaseResult<Void> unlockUser(@PathVariable Long id, HttpServletRequest request) {
        User user = userDomainService.findById(id).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        userDomainService.unlockUser(id);

        // 记录操作日志
        OperationLog log = new OperationLog();
        log.setUserId(user.getId());
        log.setUsername(user.getUsername());
        log.setOperationType("UPDATE");
        log.setModule("USER");
        log.setTargetId(user.getId());
        log.setDescription("解锁用户：" + user.getUsername());
        log.setIpAddress(request.getRemoteAddr());
        operationLogDomainService.createLog(log);
        return BaseResult.success();
    }

    /**
     * 根据用户名获取用户信息
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/username/{username}")
    public BaseResult<User> getUserByUsername(@PathVariable String username) {
        return userDomainService.findByUsername(username)
                .map(BaseResult::success)
                .orElse(BaseResult.error(404, "用户不存在"));
    }

    /**
     * 根据邮箱获取用户信息
     * @param email 邮箱地址
     * @return 用户信息
     */
    @GetMapping("/email/{email}")
    public BaseResult<User> getUserByEmail(@PathVariable String email) {
        return userDomainService.findByEmail(email)
                .map(BaseResult::success)
                .orElse(BaseResult.error(404, "用户不存在"));
    }

    /**
     * 根据手机号获取用户信息
     * @param phone 手机号
     * @return 用户信息
     */
    @GetMapping("/phone/{phone}")
    public BaseResult<User> getUserByPhone(@PathVariable String phone) {
        return userDomainService.findByPhone(phone)
                .map(BaseResult::success)
                .orElse(BaseResult.error(404, "用户不存在"));
    }

    /**
     * 分页查询用户列表
     * @param query 分页查询参数
     * @return 分页用户数据
     */
    @PostMapping("/page")
    public BasePageResult<User> getUserPage(@RequestBody @Validated UserPageQuery query) {
        return userDomainService.findUserPage(query);
    }
}