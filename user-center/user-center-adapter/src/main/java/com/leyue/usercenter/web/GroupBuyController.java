package com.leyue.usercenter.web;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.api.GroupBuyServiceI;
import com.leyue.usercenter.dto.command.groupbuy.CreateGroupBuyActivityCmd;
import com.leyue.usercenter.dto.command.groupbuy.CreateGroupCmd;
import com.leyue.usercenter.dto.command.groupbuy.JoinGroupCmd;
import com.leyue.usercenter.dto.command.groupbuy.UpdateGroupBuyActivityCmd;
import com.leyue.usercenter.dto.data.groupbuy.GroupBuyActivityDTO;
import com.leyue.usercenter.dto.data.groupbuy.GroupBuyGroupDTO;
import com.leyue.usercenter.dto.query.groupbuy.GroupBuyActivityPageQry;
import com.leyue.usercenter.dto.query.groupbuy.GroupBuyActivityQry;
import com.leyue.usercenter.dto.query.groupbuy.GroupBuyGroupPageQry;
import com.leyue.usercenter.dto.query.groupbuy.GroupBuyGroupQry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/group-buy")
@Tag(name = "团购管理", description = "团购活动和团组管理API")
public class GroupBuyController {

    @Resource
    private GroupBuyServiceI groupBuyService;

    // =============== 团购活动管理 ===============

    @PostMapping("/activity")
    @Operation(summary = "创建团购活动", description = "创建新的团购活动")
    public SingleResponse<GroupBuyActivityDTO> createActivity(@Valid @RequestBody CreateGroupBuyActivityCmd cmd) {
        log.info("创建团购活动请求: {}", cmd.getName());
        return groupBuyService.createActivity(cmd);
    }

    @PutMapping("/activity/{id}")
    @Operation(summary = "更新团购活动", description = "更新指定的团购活动")
    public SingleResponse<GroupBuyActivityDTO> updateActivity(
            @Parameter(description = "活动ID") @PathVariable Long id,
            @Valid @RequestBody UpdateGroupBuyActivityCmd cmd) {
        log.info("更新团购活动请求: id={}", id);
        cmd.setId(id);
        return groupBuyService.updateActivity(cmd);
    }

    @GetMapping("/activity/{id}")
    @Operation(summary = "获取团购活动详情", description = "根据ID获取团购活动详情")
    public SingleResponse<GroupBuyActivityDTO> getActivity(@Parameter(description = "活动ID") @PathVariable Long id) {
        log.info("获取团购活动详情请求: id={}", id);
        GroupBuyActivityQry qry = new GroupBuyActivityQry();
        qry.setId(id);
        return groupBuyService.getActivity(qry);
    }

    @GetMapping("/activity/page")
    @Operation(summary = "分页查询团购活动", description = "分页查询团购活动列表")
    public PageResponse<GroupBuyActivityDTO> getActivityPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "活动名称") @RequestParam(required = false) String name,
            @Parameter(description = "活动状态") @RequestParam(required = false) String status,
            @Parameter(description = "商品ID") @RequestParam(required = false) Long productId) {
        
        log.info("分页查询团购活动请求: pageNum={}, pageSize={}", pageNum, pageSize);
        GroupBuyActivityPageQry qry = new GroupBuyActivityPageQry();
        qry.setPageIndex(pageNum);
        qry.setPageSize(pageSize);
        qry.setName(name);
        qry.setStatus(status);
        qry.setProductId(productId);
        
        return groupBuyService.getActivityPage(qry);
    }

    @GetMapping("/activity/active")
    @Operation(summary = "获取进行中的团购活动", description = "获取当前进行中的所有团购活动")
    public MultiResponse<GroupBuyActivityDTO> getActiveActivities() {
        log.info("获取进行中的团购活动请求");
        return groupBuyService.getActiveActivities();
    }

    @DeleteMapping("/activity/{id}")
    @Operation(summary = "删除团购活动", description = "删除指定的团购活动")
    public Response deleteActivity(@Parameter(description = "活动ID") @PathVariable Long id) {
        log.info("删除团购活动请求: id={}", id);
        return groupBuyService.deleteActivity(id);
    }

    @PutMapping("/activity/{id}/start")
    @Operation(summary = "启动团购活动", description = "启动指定的团购活动")
    public Response startActivity(@Parameter(description = "活动ID") @PathVariable Long id) {
        log.info("启动团购活动请求: id={}", id);
        return groupBuyService.startActivity(id);
    }

    @PutMapping("/activity/{id}/pause")
    @Operation(summary = "暂停团购活动", description = "暂停指定的团购活动")
    public Response pauseActivity(@Parameter(description = "活动ID") @PathVariable Long id) {
        log.info("暂停团购活动请求: id={}", id);
        return groupBuyService.pauseActivity(id);
    }

    @PutMapping("/activity/{id}/end")
    @Operation(summary = "结束团购活动", description = "结束指定的团购活动")
    public Response endActivity(@Parameter(description = "活动ID") @PathVariable Long id) {
        log.info("结束团购活动请求: id={}", id);
        return groupBuyService.endActivity(id);
    }

    // =============== 团组管理 ===============

    @PostMapping("/group")
    @Operation(summary = "创建团组", description = "为团购活动创建新的团组")
    public SingleResponse<GroupBuyGroupDTO> createGroup(@Valid @RequestBody CreateGroupCmd cmd) {
        log.info("创建团组请求: activityId={}, leaderUserId={}", cmd.getActivityId(), cmd.getLeaderUserId());
        return groupBuyService.createGroup(cmd);
    }

    @GetMapping("/group/{id}")
    @Operation(summary = "获取团组详情", description = "根据ID获取团组详情")
    public SingleResponse<GroupBuyGroupDTO> getGroup(@Parameter(description = "团组ID") @PathVariable Long id) {
        log.info("获取团组详情请求: id={}", id);
        GroupBuyGroupQry qry = new GroupBuyGroupQry();
        qry.setId(id);
        return groupBuyService.getGroup(qry);
    }

    @GetMapping("/group/number/{groupNumber}")
    @Operation(summary = "根据团号获取团组", description = "根据团号获取团组详情")
    public SingleResponse<GroupBuyGroupDTO> getGroupByNumber(@Parameter(description = "团号") @PathVariable String groupNumber) {
        log.info("根据团号获取团组请求: groupNumber={}", groupNumber);
        GroupBuyGroupQry qry = new GroupBuyGroupQry();
        qry.setGroupNumber(groupNumber);
        return groupBuyService.getGroup(qry);
    }

    @GetMapping("/group/page")
    @Operation(summary = "分页查询团组", description = "分页查询团组列表")
    public PageResponse<GroupBuyGroupDTO> getGroupPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "活动ID") @RequestParam(required = false) Long activityId,
            @Parameter(description = "团组状态") @RequestParam(required = false) String status,
            @Parameter(description = "团长用户ID") @RequestParam(required = false) String leaderUserId) {
        
        log.info("分页查询团组请求: pageNum={}, pageSize={}", pageNum, pageSize);
        GroupBuyGroupPageQry qry = new GroupBuyGroupPageQry();
        qry.setPageIndex(pageNum);
        qry.setPageSize(pageSize);
        qry.setActivityId(activityId);
        qry.setStatus(status);
        qry.setLeaderUserId(leaderUserId);
        
        return groupBuyService.getGroupPage(qry);
    }

    @GetMapping("/group/activity/{activityId}")
    @Operation(summary = "获取活动的团组列表", description = "获取指定团购活动的所有团组")
    public MultiResponse<GroupBuyGroupDTO> getGroupsByActivity(@Parameter(description = "活动ID") @PathVariable Long activityId) {
        log.info("获取活动团组列表请求: activityId={}", activityId);
        return groupBuyService.getGroupsByActivity(activityId);
    }

    @GetMapping("/group/user/{userId}")
    @Operation(summary = "获取用户的团组列表", description = "获取指定用户作为团长的团组列表")
    public MultiResponse<GroupBuyGroupDTO> getUserGroups(@Parameter(description = "用户ID") @PathVariable String userId) {
        log.info("获取用户团组列表请求: userId={}", userId);
        return groupBuyService.getUserGroups(userId);
    }

    // =============== 参团操作 ===============

    @PostMapping("/group/join")
    @Operation(summary = "参团", description = "用户参加指定的团组")
    public Response joinGroup(@Valid @RequestBody JoinGroupCmd cmd) {
        log.info("参团请求: groupId={}, userId={}", cmd.getGroupId(), cmd.getUserId());
        return groupBuyService.joinGroup(cmd);
    }

    @PostMapping("/group/{groupId}/leave")
    @Operation(summary = "退团", description = "用户退出指定的团组")
    public Response leaveGroup(
            @Parameter(description = "团组ID") @PathVariable Long groupId,
            @Parameter(description = "用户ID") @RequestParam String userId) {
        log.info("退团请求: groupId={}, userId={}", groupId, userId);
        return groupBuyService.leaveGroup(groupId, userId);
    }

    @PostMapping("/group/{groupId}/dissolve")
    @Operation(summary = "解散团组", description = "团长解散指定的团组")
    public Response dissolveGroup(
            @Parameter(description = "团组ID") @PathVariable Long groupId,
            @Parameter(description = "操作用户ID") @RequestParam String operatorUserId) {
        log.info("解散团组请求: groupId={}, operatorUserId={}", groupId, operatorUserId);
        return groupBuyService.dissolveGroup(groupId, operatorUserId);
    }
}