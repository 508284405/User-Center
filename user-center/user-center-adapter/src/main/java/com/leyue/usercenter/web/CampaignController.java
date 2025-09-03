package com.leyue.usercenter.web;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.api.CampaignServiceI;
import com.leyue.usercenter.dto.command.promotion.CreateCampaignCmd;
import com.leyue.usercenter.dto.command.promotion.UpdateCampaignCmd;
import com.leyue.usercenter.dto.data.promotion.CampaignDTO;
import com.leyue.usercenter.dto.query.promotion.CampaignPageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/campaign")
@Tag(name = "促销活动管理", description = "促销活动管理API")
public class CampaignController {

    @Resource
    private CampaignServiceI campaignService;

    @PostMapping
    @Operation(summary = "创建促销活动", description = "创建新的促销活动")
    public SingleResponse<CampaignDTO> createCampaign(@Valid @RequestBody CreateCampaignCmd cmd) {
        log.info("创建促销活动请求: {}", cmd.getName());
        return campaignService.createCampaign(cmd);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新促销活动", description = "更新指定的促销活动")
    public SingleResponse<CampaignDTO> updateCampaign(
            @Parameter(description = "活动ID") @PathVariable Long id,
            @Valid @RequestBody UpdateCampaignCmd cmd) {
        log.info("更新促销活动请求: id={}", id);
        cmd.setId(id);
        return campaignService.updateCampaign(cmd);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取促销活动详情", description = "根据ID获取促销活动详情")
    public SingleResponse<CampaignDTO> getCampaign(@Parameter(description = "活动ID") @PathVariable Long id) {
        log.info("获取促销活动详情请求: id={}", id);
        return campaignService.getCampaignById(id);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询促销活动", description = "分页查询促销活动列表")
    public PageResponse<CampaignDTO> getCampaignPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "活动名称") @RequestParam(required = false) String name,
            @Parameter(description = "活动状态") @RequestParam(required = false) String status,
            @Parameter(description = "活动类型") @RequestParam(required = false) String type) {
        
        log.info("分页查询促销活动请求: pageNum={}, pageSize={}", pageNum, pageSize);
        CampaignPageQuery query = new CampaignPageQuery();
        query.setPageIndex(pageNum);
        query.setPageSize(pageSize);
        query.setName(name);
        query.setStatus(status);
        query.setType(type);
        
        return campaignService.getCampaignPage(query);
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "激活促销活动", description = "激活指定的促销活动")
    public SingleResponse<Void> activateCampaign(@Parameter(description = "活动ID") @PathVariable Long id) {
        log.info("激活促销活动请求: id={}", id);
        return campaignService.activateCampaign(id);
    }

    @PutMapping("/{id}/pause")
    @Operation(summary = "暂停促销活动", description = "暂停指定的促销活动")
    public SingleResponse<Void> pauseCampaign(@Parameter(description = "活动ID") @PathVariable Long id) {
        log.info("暂停促销活动请求: id={}", id);
        return campaignService.pauseCampaign(id);
    }

    @PutMapping("/{id}/resume")
    @Operation(summary = "恢复促销活动", description = "恢复暂停的促销活动")
    public SingleResponse<Void> resumeCampaign(@Parameter(description = "活动ID") @PathVariable Long id) {
        log.info("恢复促销活动请求: id={}", id);
        return campaignService.resumeCampaign(id);
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "完成促销活动", description = "完成指定的促销活动")
    public SingleResponse<Void> completeCampaign(@Parameter(description = "活动ID") @PathVariable Long id) {
        log.info("完成促销活动请求: id={}", id);
        return campaignService.completeCampaign(id);
    }
}