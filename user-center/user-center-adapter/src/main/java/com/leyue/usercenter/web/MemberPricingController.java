package com.leyue.usercenter.web;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.api.MemberPricingServiceI;
import com.leyue.usercenter.dto.command.member.CreateMemberPricingCmd;
import com.leyue.usercenter.dto.command.member.UpdateMemberPricingCmd;
import com.leyue.usercenter.dto.data.member.MemberPricingDTO;
import com.leyue.usercenter.dto.query.member.MemberPricingPageQry;
import com.leyue.usercenter.dto.query.member.MemberPricingQry;
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
@RequestMapping("/api/member-pricing")
@Tag(name = "会员价格管理", description = "会员价格配置管理API")
public class MemberPricingController {

    @Resource
    private MemberPricingServiceI memberPricingService;

    @PostMapping
    @Operation(summary = "创建会员价配置", description = "为指定商品和会员等级创建价格配置")
    public SingleResponse<MemberPricingDTO> createMemberPricing(@Valid @RequestBody CreateMemberPricingCmd cmd) {
        log.info("创建会员价配置请求: productId={}, memberLevel={}", cmd.getProductId(), cmd.getMemberLevel());
        return memberPricingService.createMemberPricing(cmd);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新会员价配置", description = "更新指定的会员价格配置")
    public SingleResponse<MemberPricingDTO> updateMemberPricing(
            @Parameter(description = "配置ID") @PathVariable Long id,
            @Valid @RequestBody UpdateMemberPricingCmd cmd) {
        log.info("更新会员价配置请求: id={}", id);
        cmd.setId(id);
        return memberPricingService.updateMemberPricing(cmd);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取会员价配置", description = "根据ID获取会员价格配置详情")
    public SingleResponse<MemberPricingDTO> getMemberPricing(@Parameter(description = "配置ID") @PathVariable Long id) {
        log.info("获取会员价配置请求: id={}", id);
        MemberPricingQry qry = new MemberPricingQry();
        qry.setId(id);
        return memberPricingService.getMemberPricing(qry);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "获取商品会员价配置", description = "获取指定商品的所有会员价格配置")
    public MultiResponse<MemberPricingDTO> getMemberPricingByProduct(
            @Parameter(description = "商品ID") @PathVariable Long productId) {
        log.info("获取商品会员价配置请求: productId={}", productId);
        return memberPricingService.getMemberPricingByProduct(productId);
    }

    @GetMapping("/level/{memberLevel}")
    @Operation(summary = "获取会员等级价配置", description = "获取指定会员等级的所有价格配置")
    public MultiResponse<MemberPricingDTO> getMemberPricingByLevel(
            @Parameter(description = "会员等级") @PathVariable String memberLevel) {
        log.info("获取会员等级价配置请求: memberLevel={}", memberLevel);
        return memberPricingService.getMemberPricingByLevel(memberLevel);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询会员价配置", description = "分页查询会员价格配置列表")
    public PageResponse<MemberPricingDTO> getMemberPricingPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "商品ID") @RequestParam(required = false) Long productId,
            @Parameter(description = "会员等级") @RequestParam(required = false) String memberLevel,
            @Parameter(description = "是否激活") @RequestParam(required = false) Boolean isActive) {
        
        log.info("分页查询会员价配置请求: pageNum={}, pageSize={}", pageNum, pageSize);
        MemberPricingPageQry qry = new MemberPricingPageQry();
        qry.setPageIndex(pageNum);
        qry.setPageSize(pageSize);
        qry.setProductId(productId);
        qry.setMemberLevel(memberLevel);
        qry.setIsActive(isActive);
        
        return memberPricingService.getMemberPricingPage(qry);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除会员价配置", description = "删除指定的会员价格配置")
    public Response deleteMemberPricing(@Parameter(description = "配置ID") @PathVariable Long id) {
        log.info("删除会员价配置请求: id={}", id);
        return memberPricingService.deleteMemberPricing(id);
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "激活会员价配置", description = "激活指定的会员价格配置")
    public Response activateMemberPricing(@Parameter(description = "配置ID") @PathVariable Long id) {
        log.info("激活会员价配置请求: id={}", id);
        return memberPricingService.activateMemberPricing(id);
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "停用会员价配置", description = "停用指定的会员价格配置")
    public Response deactivateMemberPricing(@Parameter(description = "配置ID") @PathVariable Long id) {
        log.info("停用会员价配置请求: id={}", id);
        return memberPricingService.deactivateMemberPricing(id);
    }

    @GetMapping("/calculate")
    @Operation(summary = "计算会员价格", description = "根据商品、会员等级计算会员价格")
    public SingleResponse<BigDecimal> calculateMemberPrice(
            @Parameter(description = "商品ID") @RequestParam Long productId,
            @Parameter(description = "会员等级") @RequestParam String memberLevel,
            @Parameter(description = "原价(分)") @RequestParam BigDecimal originalPrice,
            @Parameter(description = "数量") @RequestParam Integer quantity) {
        
        log.info("计算会员价格请求: productId={}, memberLevel={}, originalPrice={}, quantity={}", 
                productId, memberLevel, originalPrice, quantity);
        return memberPricingService.calculateMemberPrice(productId, memberLevel, originalPrice, quantity);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量获取会员价配置", description = "批量获取多个商品的会员价格配置")
    public MultiResponse<MemberPricingDTO> getBatchMemberPricing(
            @Parameter(description = "商品ID列表") @RequestBody List<Long> productIds,
            @Parameter(description = "会员等级") @RequestParam String memberLevel) {
        
        log.info("批量获取会员价配置请求: productIds size={}, memberLevel={}", 
                productIds != null ? productIds.size() : 0, memberLevel);
        return memberPricingService.getBatchMemberPricing(productIds, memberLevel);
    }
}