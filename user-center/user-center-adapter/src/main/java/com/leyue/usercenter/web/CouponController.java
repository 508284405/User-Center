package com.leyue.usercenter.web;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.api.CouponServiceI;
import com.leyue.usercenter.dto.command.coupon.CreateCouponTemplateCmd;
import com.leyue.usercenter.dto.command.coupon.IssueCouponCmd;
import com.leyue.usercenter.dto.command.coupon.ReserveCouponCmd;
import com.leyue.usercenter.dto.command.coupon.UpdateCouponTemplateCmd;
import com.leyue.usercenter.dto.command.coupon.UseCouponCmd;
import com.leyue.usercenter.dto.data.coupon.CouponDTO;
import com.leyue.usercenter.dto.data.coupon.CouponTemplateDTO;
import com.leyue.usercenter.dto.query.coupon.CouponPageQry;
import com.leyue.usercenter.dto.query.coupon.CouponTemplatePageQry;
import com.leyue.usercenter.dto.query.coupon.CouponTemplateQry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/coupon")
@Tag(name = "优惠券管理", description = "优惠券模板和券码管理API")
public class CouponController {

    @Resource
    private CouponServiceI couponService;

    // =============== 优惠券模板管理 ===============

    @PostMapping("/template")
    @Operation(summary = "创建优惠券模板", description = "创建新的优惠券模板")
    public SingleResponse<CouponTemplateDTO> createTemplate(@Valid @RequestBody CreateCouponTemplateCmd cmd) {
        log.info("创建优惠券模板请求: {}", cmd.getName());
        return couponService.createTemplate(cmd);
    }

    @PutMapping("/template/{id}")
    @Operation(summary = "更新优惠券模板", description = "更新指定的优惠券模板")
    public SingleResponse<CouponTemplateDTO> updateTemplate(
            @Parameter(description = "模板ID") @PathVariable Long id,
            @Valid @RequestBody UpdateCouponTemplateCmd cmd) {
        log.info("更新优惠券模板请求: id={}", id);
        cmd.setId(id);
        return couponService.updateTemplate(cmd);
    }

    @GetMapping("/template/{id}")
    @Operation(summary = "获取优惠券模板详情", description = "根据ID获取优惠券模板详情")
    public SingleResponse<CouponTemplateDTO> getTemplate(@Parameter(description = "模板ID") @PathVariable Long id) {
        log.info("获取优惠券模板详情请求: id={}", id);
        CouponTemplateQry qry = new CouponTemplateQry();
        qry.setId(id);
        return couponService.getTemplate(qry);
    }

    @GetMapping("/template/page")
    @Operation(summary = "分页查询优惠券模板", description = "分页查询优惠券模板列表")
    public PageResponse<CouponTemplateDTO> getTemplatePage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "模板名称") @RequestParam(required = false) String name,
            @Parameter(description = "优惠券类型") @RequestParam(required = false) String type,
            @Parameter(description = "模板状态") @RequestParam(required = false) String status) {
        
        log.info("分页查询优惠券模板请求: pageNum={}, pageSize={}", pageNum, pageSize);
        CouponTemplatePageQry qry = new CouponTemplatePageQry();
        qry.setPageIndex(pageNum);
        qry.setPageSize(pageSize);
        qry.setName(name);
        qry.setType(type);
        qry.setStatus(status);
        
        return couponService.getTemplatePage(qry);
    }

    @GetMapping("/template/available")
    @Operation(summary = "获取可用的优惠券模板", description = "获取当前可以发放的优惠券模板")
    public MultiResponse<CouponTemplateDTO> getAvailableTemplates() {
        log.info("获取可用优惠券模板请求");
        return couponService.getAvailableTemplates();
    }

    @PutMapping("/template/{id}/activate")
    @Operation(summary = "激活优惠券模板", description = "激活指定的优惠券模板")
    public Response activateTemplate(@Parameter(description = "模板ID") @PathVariable Long id) {
        log.info("激活优惠券模板请求: id={}", id);
        return couponService.activateTemplate(id);
    }

    @PutMapping("/template/{id}/deactivate")
    @Operation(summary = "停用优惠券模板", description = "停用指定的优惠券模板")
    public Response deactivateTemplate(@Parameter(description = "模板ID") @PathVariable Long id) {
        log.info("停用优惠券模板请求: id={}", id);
        return couponService.deactivateTemplate(id);
    }

    @DeleteMapping("/template/{id}")
    @Operation(summary = "删除优惠券模板", description = "删除指定的优惠券模板")
    public Response deleteTemplate(@Parameter(description = "模板ID") @PathVariable Long id) {
        log.info("删除优惠券模板请求: id={}", id);
        return couponService.deleteTemplate(id);
    }

    // =============== 优惠券发放管理 ===============

    @PostMapping("/issue")
    @Operation(summary = "发放优惠券", description = "为用户发放优惠券")
    public SingleResponse<CouponDTO> issueCoupon(@Valid @RequestBody IssueCouponCmd cmd) {
        log.info("发放优惠券请求: templateId={}, userId={}", cmd.getTemplateId(), cmd.getUserId());
        return couponService.issueCoupon(cmd);
    }

    @PostMapping("/issue/batch")
    @Operation(summary = "批量发放优惠券", description = "为用户批量发放优惠券")
    public MultiResponse<CouponDTO> batchIssueCoupon(
            @Valid @RequestBody IssueCouponCmd cmd,
            @Parameter(description = "发放数量") @RequestParam Integer count) {
        log.info("批量发放优惠券请求: templateId={}, userId={}, count={}", 
                cmd.getTemplateId(), cmd.getUserId(), count);
        return couponService.batchIssueCoupon(cmd, count);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户优惠券", description = "分页获取用户的优惠券列表")
    public PageResponse<CouponDTO> getUserCoupons(
            @Parameter(description = "用户ID") @PathVariable String userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "优惠券状态") @RequestParam(required = false) String status) {
        
        log.info("获取用户优惠券请求: userId={}, pageNum={}, pageSize={}", userId, pageNum, pageSize);
        CouponPageQry qry = new CouponPageQry();
        qry.setUserId(userId);
        qry.setPageIndex(pageNum);
        qry.setPageSize(pageSize);
        qry.setStatus(status);
        
        return couponService.getUserCoupons(qry);
    }

    @GetMapping("/user/{userId}/available")
    @Operation(summary = "获取用户可用优惠券", description = "获取用户当前可以使用的优惠券")
    public MultiResponse<CouponDTO> getAvailableCoupons(@Parameter(description = "用户ID") @PathVariable String userId) {
        log.info("获取用户可用优惠券请求: userId={}", userId);
        return couponService.getAvailableCoupons(userId);
    }

    @GetMapping("/user/{userId}/usable")
    @Operation(summary = "获取用户可用于订单的优惠券", description = "获取用户可以用于指定订单金额的优惠券")
    public MultiResponse<CouponDTO> getUsableCoupons(
            @Parameter(description = "用户ID") @PathVariable String userId,
            @Parameter(description = "订单金额") @RequestParam String orderAmount) {
        log.info("获取用户可用于订单的优惠券请求: userId={}, orderAmount={}", userId, orderAmount);
        return couponService.getUsableCoupons(userId, orderAmount);
    }

    // =============== 优惠券使用管理 ===============

    @PostMapping("/reserve")
    @Operation(summary = "预占优惠券", description = "为订单预占优惠券")
    public Response reserveCoupon(@Valid @RequestBody ReserveCouponCmd cmd) {
        log.info("预占优惠券请求: couponCode={}, orderId={}", cmd.getCouponCode(), cmd.getOrderId());
        return couponService.reserveCoupon(cmd);
    }

    @PostMapping("/confirm")
    @Operation(summary = "确认使用优惠券", description = "确认使用已预占的优惠券")
    public Response confirmCoupon(
            @Parameter(description = "优惠券码") @RequestParam String couponCode,
            @Parameter(description = "订单ID") @RequestParam String orderId) {
        log.info("确认使用优惠券请求: couponCode={}, orderId={}", couponCode, orderId);
        return couponService.confirmCoupon(couponCode, orderId);
    }

    @PostMapping("/cancel-reservation")
    @Operation(summary = "取消优惠券预占", description = "取消优惠券的预占状态")
    public Response cancelReservation(
            @Parameter(description = "优惠券码") @RequestParam String couponCode,
            @Parameter(description = "订单ID") @RequestParam String orderId) {
        log.info("取消优惠券预占请求: couponCode={}, orderId={}", couponCode, orderId);
        return couponService.cancelReservation(couponCode, orderId);
    }

    @PostMapping("/use")
    @Operation(summary = "使用优惠券", description = "直接使用优惠券（一步到位）")
    public Response useCoupon(@Valid @RequestBody UseCouponCmd cmd) {
        log.info("使用优惠券请求: couponCode={}, orderId={}", cmd.getCouponCode(), cmd.getOrderId());
        return couponService.useCoupon(cmd);
    }

    @PostMapping("/{couponCode}/expire")
    @Operation(summary = "过期优惠券", description = "手动过期指定的优惠券")
    public Response expireCoupon(@Parameter(description = "优惠券码") @PathVariable String couponCode) {
        log.info("过期优惠券请求: couponCode={}", couponCode);
        return couponService.expireCoupon(couponCode);
    }

    // =============== 优惠券状态查询 ===============

    @GetMapping("/{couponCode}")
    @Operation(summary = "获取优惠券详情", description = "根据优惠券码获取详情")
    public SingleResponse<CouponDTO> getCoupon(@Parameter(description = "优惠券码") @PathVariable String couponCode) {
        log.info("获取优惠券详情请求: couponCode={}", couponCode);
        return couponService.getCoupon(couponCode);
    }

    @GetMapping("/{couponCode}/status")
    @Operation(summary = "获取优惠券状态", description = "获取优惠券的当前状态")
    public SingleResponse<String> getCouponStatus(@Parameter(description = "优惠券码") @PathVariable String couponCode) {
        log.info("获取优惠券状态请求: couponCode={}", couponCode);
        return couponService.getCouponStatus(couponCode);
    }

    @GetMapping("/{couponCode}/validate")
    @Operation(summary = "验证优惠券", description = "验证优惠券是否可以被指定用户使用")
    public Response validateCoupon(
            @Parameter(description = "优惠券码") @PathVariable String couponCode,
            @Parameter(description = "用户ID") @RequestParam String userId) {
        log.info("验证优惠券请求: couponCode={}, userId={}", couponCode, userId);
        return couponService.validateCoupon(couponCode, userId);
    }
}