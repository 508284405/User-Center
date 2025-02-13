package com.yuwang.usercenter.interfaces.controller;

import com.yuwang.usercenter.domain.log.entity.OperationLog;
import com.yuwang.usercenter.domain.log.service.OperationLogDomainService;
import com.yuwang.usercenter.domain.user.dto.UserPageQuery;
import com.yuwang.usercenter.domain.user.entity.User;
import com.yuwang.usercenter.domain.user.service.UserDomainService;
import jakarta.servlet.http.HttpServletRequest;
import com.yuwang.usercenter.infrastructure.common.BaseResult;
import com.yuwang.usercenter.infrastructure.common.BasePageResult;
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

    @PostMapping
    public BaseResult<User> createUser(@RequestBody User user, HttpServletRequest request) {
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

    @PutMapping("/{id}")
    public BaseResult<User> updateUser(@PathVariable Long id, @RequestBody User user, HttpServletRequest request) {
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

    @GetMapping("/username/{username}")
    public BaseResult<User> getUserByUsername(@PathVariable String username) {
        return userDomainService.findByUsername(username)
                .map(BaseResult::success)
                .orElse(BaseResult.error(404, "用户不存在"));
    }

    @GetMapping("/email/{email}")
    public BaseResult<User> getUserByEmail(@PathVariable String email) {
        return userDomainService.findByEmail(email)
                .map(BaseResult::success)
                .orElse(BaseResult.error(404, "用户不存在"));
    }

    @GetMapping("/phone/{phone}")
    public BaseResult<User> getUserByPhone(@PathVariable String phone) {
        return userDomainService.findByPhone(phone)
                .map(BaseResult::success)
                .orElse(BaseResult.error(404, "用户不存在"));
    }

    @PostMapping("/page")
    public BasePageResult<User> getUserPage(@RequestBody UserPageQuery query) {
        return userDomainService.findUserPage(query);
    }
}