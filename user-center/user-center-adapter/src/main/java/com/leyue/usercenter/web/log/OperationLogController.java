package com.leyue.usercenter.web.log;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.api.OperationLogService;
import com.leyue.usercenter.api.annotation.OperationLog;
import com.leyue.usercenter.dto.OperationLogDTO;
import com.leyue.usercenter.dto.OperationLogStatisticsDTO;
import com.leyue.usercenter.dto.query.OperationLogPageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/api/operation-logs")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 分页查询操作日志
     *
     * @param query 查询参数
     * @return 分页结果
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDIT')")
    @OperationLog(module = "操作日志", operationType = "查询", description = "分页查询操作日志")
    public PageResponse<OperationLogDTO> pageOperationLogs(OperationLogPageQuery query) {
        return operationLogService.pageOperationLogs(query);
    }

    /**
     * 获取操作日志详情
     *
     * @param id 日志ID
     * @return 操作日志详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDIT')")
    @OperationLog(module = "操作日志", operationType = "查询", description = "获取操作日志详情", targetId = "#id")
    public SingleResponse<OperationLogDTO> getOperationLog(@PathVariable Long id) {
        return operationLogService.getOperationLog(id);
    }

    /**
     * 获取操作链
     *
     * @param relatedOperationId 关联操作ID
     * @return 操作链列表
     */
    @GetMapping("/chain/{relatedOperationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDIT')")
    @OperationLog(module = "操作日志", operationType = "查询", description = "获取操作链", targetId = "#relatedOperationId")
    public SingleResponse<List<OperationLogDTO>> getOperationChain(@PathVariable Long relatedOperationId) {
        return operationLogService.getOperationChain(relatedOperationId);
    }

    /**
     * 获取用户操作统计
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @GetMapping("/statistics/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDIT')")
    @OperationLog(module = "操作日志", operationType = "统计", description = "获取用户操作统计", targetId = "#userId")
    public SingleResponse<Map<String, Object>> getUserOperationStatistics(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        return operationLogService.getUserOperationStatistics(userId, startTime, endTime);
    }

    /**
     * 获取模块操作统计
     *
     * @param modules 模块列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @GetMapping("/statistics/module")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDIT')")
    @OperationLog(module = "操作日志", operationType = "统计", description = "获取模块操作统计")
    public SingleResponse<List<OperationLogStatisticsDTO>> getModuleOperationStatistics(
            @RequestParam List<String> modules,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        return operationLogService.getModuleOperationStatistics(modules, startTime, endTime);
    }

    /**
     * 获取操作类型统计
     *
     * @param operationTypes 操作类型列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @GetMapping("/statistics/operation-type")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDIT')")
    @OperationLog(module = "操作日志", operationType = "统计", description = "获取操作类型统计")
    public SingleResponse<List<OperationLogStatisticsDTO>> getOperationTypeStatistics(
            @RequestParam List<String> operationTypes,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        return operationLogService.getOperationTypeStatistics(operationTypes, startTime, endTime);
    }

    /**
     * 导出操作日志
     *
     * @param query 查询参数
     * @param format 导出格式（CSV, EXCEL, PDF）
     * @return 导出文件路径
     */
    @PostMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDIT')")
    @OperationLog(module = "操作日志", operationType = "导出", description = "导出操作日志", saveParams = true)
    public SingleResponse<String> exportOperationLogs(
            @RequestBody OperationLogPageQuery query,
            @RequestParam(defaultValue = "CSV") String format) {
        return operationLogService.exportOperationLogs(query, format);
    }

    /**
     * 归档操作日志
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 归档结果
     */
    @PostMapping("/archive")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "操作日志", operationType = "归档", description = "归档操作日志", saveParams = true, severityLevel = "重要")
    public Response archiveOperationLogs(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        return operationLogService.archiveOperationLogs(startTime, endTime);
    }

    /**
     * 清理过期操作日志
     *
     * @param retentionDays 保留天数
     * @return 清理结果
     */
    @PostMapping("/clean")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "操作日志", operationType = "清理", description = "清理过期操作日志", saveParams = true, severityLevel = "重要")
    public Response cleanExpiredOperationLogs(@RequestParam Integer retentionDays) {
        return operationLogService.cleanExpiredOperationLogs(retentionDays);
    }

    /**
     * 获取资源操作历史
     *
     * @param module 模块
     * @param targetId 目标ID
     * @return 操作历史列表
     */
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDIT')")
    @OperationLog(module = "操作日志", operationType = "查询", description = "获取资源操作历史")
    public SingleResponse<List<OperationLogDTO>> getResourceOperationHistory(
            @RequestParam String module,
            @RequestParam Long targetId) {
        return operationLogService.getResourceOperationHistory(module, targetId);
    }

    /**
     * 检测异常操作行为
     *
     * @param userId 用户ID
     * @param timeRangeMinutes 检测时间范围（分钟）
     * @return 异常操作列表
     */
    @GetMapping("/abnormal/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDIT')")
    @OperationLog(module = "操作日志", operationType = "检测", description = "检测异常操作行为", targetId = "#userId", severityLevel = "重要")
    public SingleResponse<List<OperationLogDTO>> detectAbnormalOperations(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "60") Integer timeRangeMinutes) {
        return operationLogService.detectAbnormalOperations(userId, timeRangeMinutes);
    }
}
