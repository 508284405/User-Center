package com.leyue.usercenter.web;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.api.FullReductionServiceI;
import com.leyue.usercenter.dto.command.promotion.CreateFullReductionActivityCmd;
import com.leyue.usercenter.dto.command.promotion.UpdateFullReductionActivityCmd;
import com.leyue.usercenter.dto.data.promotion.FullReductionActivityDTO;
import com.leyue.usercenter.dto.data.promotion.FullReductionCalculationDTO;
import com.leyue.usercenter.dto.query.promotion.FullReductionActivityPageQry;
import com.leyue.usercenter.dto.query.promotion.FullReductionActivityQry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/full-reduction")
@Tag(name = "满减活动管理", description = "满减活动管理API")
public class FullReductionController {

    @Resource
    private FullReductionServiceI fullReductionService;

    @PostMapping
    @Operation(summary = "创建满减活动", description = "创建新的满减活动")
    public SingleResponse<FullReductionActivityDTO> createActivity(@Valid @RequestBody CreateFullReductionActivityCmd cmd) {
        log.info("创建满减活动请求: {}", cmd.getName());
        return fullReductionService.createActivity(cmd);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新满减活动", description = "更新指定的满减活动")
    public SingleResponse<FullReductionActivityDTO> updateActivity(
            @Parameter(description = "活动ID") @PathVariable Long id,
            @Valid @RequestBody UpdateFullReductionActivityCmd cmd) {
        log.info("更新满减活动请求: id={}", id);
        cmd.setId(id);
        return fullReductionService.updateActivity(cmd);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取满减活动详情", description = "根据ID获取满减活动详情")
    public SingleResponse<FullReductionActivityDTO> getActivity(@Parameter(description = "活动ID") @PathVariable Long id) {
        log.info("获取满减活动详情请求: id={}", id);
        FullReductionActivityQry qry = new FullReductionActivityQry();
        qry.setId(id);
        return fullReductionService.getActivity(qry);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询满减活动", description = "分页查询满减活动列表")
    public PageResponse<FullReductionActivityDTO> getActivityPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "活动名称") @RequestParam(required = false) String name,
            @Parameter(description = "活动状态") @RequestParam(required = false) String status,
            @Parameter(description = "创建者") @RequestParam(required = false) String createdBy) {
        
        log.info("分页查询满减活动请求: pageNum={}, pageSize={}", pageNum, pageSize);
        FullReductionActivityPageQry qry = new FullReductionActivityPageQry();
        qry.setPageIndex(pageNum);
        qry.setPageSize(pageSize);
        qry.setName(name);
        qry.setStatus(status);
        qry.setCreatedBy(createdBy);
        
        return fullReductionService.getActivityPage(qry);
    }

    @GetMapping("/active")
    @Operation(summary = "获取进行中的满减活动", description = "获取当前进行中的所有满减活动")
    public MultiResponse<FullReductionActivityDTO> getActiveActivities() {
        log.info("获取进行中的满减活动请求");
        return fullReductionService.getActiveActivities();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除满减活动", description = "删除指定的满减活动")
    public Response deleteActivity(@Parameter(description = "活动ID") @PathVariable Long id) {
        log.info("删除满减活动请求: id={}", id);
        return fullReductionService.deleteActivity(id);
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "激活满减活动", description = "激活指定的满减活动")
    public Response activateActivity(@Parameter(description = "活动ID") @PathVariable Long id) {
        log.info("激活满减活动请求: id={}", id);
        return fullReductionService.activateActivity(id);
    }

    @PutMapping("/{id}/pause")
    @Operation(summary = "暂停满减活动", description = "暂停指定的满减活动")
    public Response pauseActivity(@Parameter(description = "活动ID") @PathVariable Long id) {
        log.info("暂停满减活动请求: id={}", id);
        return fullReductionService.pauseActivity(id);
    }

    @PutMapping("/{id}/end")
    @Operation(summary = "结束满减活动", description = "结束指定的满减活动")
    public Response endActivity(@Parameter(description = "活动ID") @PathVariable Long id) {
        log.info("结束满减活动请求: id={}", id);
        return fullReductionService.endActivity(id);
    }

    @GetMapping("/calculate")
    @Operation(summary = "计算满减金额", description = "根据订单信息计算满减金额")
    public SingleResponse<FullReductionCalculationDTO> calculateReduction(
            @Parameter(description = "用户ID") @RequestParam String userId,
            @Parameter(description = "订单金额(分)") @RequestParam BigDecimal orderAmount,
            @Parameter(description = "商品ID列表") @RequestParam(required = false) List<Long> productIds,
            @Parameter(description = "分类ID列表") @RequestParam(required = false) List<Long> categoryIds) {
        
        log.info("计算满减金额请求: userId={}, orderAmount={}", userId, orderAmount);
        return fullReductionService.calculateReduction(userId, orderAmount, productIds, categoryIds);
    }

    @PostMapping("/{activityId}/use")
    @Operation(summary = "使用满减活动", description = "使用指定的满减活动")
    public Response useActivity(
            @Parameter(description = "活动ID") @PathVariable Long activityId,
            @Parameter(description = "用户ID") @RequestParam String userId,
            @Parameter(description = "订单ID") @RequestParam String orderId,
            @Parameter(description = "订单金额(分)") @RequestParam BigDecimal orderAmount,
            @Parameter(description = "减免金额(分)") @RequestParam BigDecimal reductionAmount) {
        
        log.info("使用满减活动请求: activityId={}, userId={}, orderId={}", activityId, userId, orderId);
        return fullReductionService.useActivity(activityId, userId, orderId, orderAmount, reductionAmount);
    }

    @GetMapping("/user-usable")
    @Operation(summary = "获取用户可用的满减活动", description = "获取指定用户可以使用的满减活动")
    public MultiResponse<FullReductionActivityDTO> getUserUsableActivities(
            @Parameter(description = "用户ID") @RequestParam String userId,
            @Parameter(description = "订单金额(分)") @RequestParam BigDecimal orderAmount) {
        
        log.info("获取用户可用满减活动请求: userId={}, orderAmount={}", userId, orderAmount);
        return fullReductionService.getUserUsableActivities(userId, orderAmount);
    }
}