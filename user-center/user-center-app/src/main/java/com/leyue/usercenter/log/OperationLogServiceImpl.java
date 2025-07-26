package com.leyue.usercenter.log;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.api.OperationLogService;
import com.leyue.usercenter.domain.log.OperationLog;
import com.leyue.usercenter.domain.log.OperationLogStatistics;
import com.leyue.usercenter.domain.log.gateway.OperationLogGateway;
import com.leyue.usercenter.dto.OperationLogDTO;
import com.leyue.usercenter.dto.OperationLogStatisticsDTO;
import com.leyue.usercenter.dto.query.OperationLogPageQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 操作日志服务实现类
 */
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogGateway operationLogGateway;

    @Override
    public PageResponse<OperationLogDTO> pageOperationLogs(OperationLogPageQuery query) {
        PageResponse<OperationLog> pageResponse = operationLogGateway.pageQuery(query);

        List<OperationLogDTO> operationLogDTOs = pageResponse.getData().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.of(operationLogDTOs, pageResponse.getTotalCount(),
                pageResponse.getPageSize(), pageResponse.getPageIndex());
    }

    @Override
    public SingleResponse<OperationLogDTO> getOperationLog(Long id) {
        OperationLog operationLog = operationLogGateway.findById(id);
        if (operationLog == null) {
            return SingleResponse.buildFailure("404", "操作日志不存在");
        }

        OperationLogDTO operationLogDTO = convertToDTO(operationLog);
        return SingleResponse.of(operationLogDTO);
    }

    @Override
    public SingleResponse<List<OperationLogDTO>> getOperationChain(Long relatedOperationId) {
        List<OperationLog> operationLogs = operationLogGateway.findByRelatedOperationId(relatedOperationId);
        if (CollectionUtils.isEmpty(operationLogs)) {
            return SingleResponse.of(Collections.emptyList());
        }

        List<OperationLogDTO> operationLogDTOs = operationLogs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return SingleResponse.of(operationLogDTOs);
    }

    @Override
    public SingleResponse<Map<String, Object>> getUserOperationStatistics(Long userId, Date startTime, Date endTime) {
        Map<String, Object> statistics = operationLogGateway.getUserOperationStatistics(userId, startTime, endTime);
        return SingleResponse.of(Objects.requireNonNullElseGet(statistics, HashMap::new));

    }

    @Override
    public SingleResponse<List<OperationLogStatisticsDTO>> getModuleOperationStatistics(List<String> modules, Date startTime, Date endTime) {
        List<OperationLogStatistics> statistics = operationLogGateway.getModuleStatistics(modules, startTime, endTime);

        if (CollectionUtils.isEmpty(statistics)) {
            return SingleResponse.of(Collections.emptyList());
        }

        List<OperationLogStatisticsDTO> statisticsDTOs = statistics.stream()
                .map(this::convertToStatisticsDTO)
                .collect(Collectors.toList());

        return SingleResponse.of(statisticsDTOs);
    }

    @Override
    public SingleResponse<List<OperationLogStatisticsDTO>> getOperationTypeStatistics(List<String> operationTypes, Date startTime, Date endTime) {
        List<OperationLogStatistics> statistics = operationLogGateway.getOperationTypeStatistics(operationTypes, startTime, endTime);

        if (CollectionUtils.isEmpty(statistics)) {
            return SingleResponse.of(Collections.emptyList());
        }

        List<OperationLogStatisticsDTO> statisticsDTOs = statistics.stream()
                .map(this::convertToStatisticsDTO)
                .collect(Collectors.toList());

        return SingleResponse.of(statisticsDTOs);
    }

    @Override
    public SingleResponse<String> exportOperationLogs(OperationLogPageQuery query, String format) {
        // 获取符合条件的操作日志
        query.setPageSize(10000); // 设置较大的页码，避免分页
        query.setPageIndex(1);
        PageResponse<OperationLog> pageResponse = operationLogGateway.pageQuery(query);
        List<OperationLog> operationLogs = pageResponse.getData();

        if (CollectionUtils.isEmpty(operationLogs)) {
            return SingleResponse.buildFailure("404", "未找到符合条件的操作日志");
        }

        try {
            // 创建导出文件目录
            String exportDir = System.getProperty("java.io.tmpdir") + File.separator + "operation_logs_export";
            File dir = new File(exportDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成文件名
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String fileName = "operation_logs_" + dateFormat.format(new Date()) + "." + format.toLowerCase();
            String filePath = exportDir + File.separator + fileName;

            // 根据格式导出
            if ("CSV".equalsIgnoreCase(format)) {
                exportToCSV(operationLogs, filePath);
            } else if ("EXCEL".equalsIgnoreCase(format)) {
                exportToExcel(operationLogs, filePath);
            } else if ("PDF".equalsIgnoreCase(format)) {
                exportToPDF(operationLogs, filePath);
            } else {
                return SingleResponse.buildFailure("400", "不支持的导出格式: " + format);
            }

            return SingleResponse.of(filePath);
        } catch (Exception e) {
            return SingleResponse.buildFailure("500", "导出失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response archiveOperationLogs(Date startTime, Date endTime) {
        if (startTime == null || endTime == null) {
            return Response.buildFailure("400", "开始时间和结束时间不能为空");
        }

        if (startTime.after(endTime)) {
            return Response.buildFailure("400", "开始时间不能晚于结束时间");
        }

        int archivedCount = operationLogGateway.archiveLogs(startTime, endTime);

        if (archivedCount > 0) {
            return Response.buildSuccess();
        } else {
            return Response.buildSuccess();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response cleanExpiredOperationLogs(Integer retentionDays) {
        if (retentionDays == null || retentionDays <= 0) {
            return Response.buildFailure("400", "保留天数必须大于0");
        }

        // 计算过期日期
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -retentionDays);
        Date expiryDate = calendar.getTime();

        int deletedCount = operationLogGateway.deleteExpiredLogs(expiryDate);

        if (deletedCount > 0) {
            return Response.buildSuccess();
        } else {
            return Response.buildSuccess();
        }
    }

    @Override
    public SingleResponse<List<OperationLogDTO>> getResourceOperationHistory(String module, Long targetId) {
        if (module == null || targetId == null) {
            return SingleResponse.buildFailure("400", "模块和目标ID不能为空");
        }

        List<OperationLog> operationLogs = operationLogGateway.findByModuleAndTargetId(module, targetId);

        if (CollectionUtils.isEmpty(operationLogs)) {
            return SingleResponse.of(Collections.emptyList());
        }

        List<OperationLogDTO> operationLogDTOs = operationLogs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return SingleResponse.of(operationLogDTOs);
    }

    @Override
    public SingleResponse<List<OperationLogDTO>> detectAbnormalOperations(Long userId, Integer timeRangeMinutes) {
        if (userId == null || timeRangeMinutes == null || timeRangeMinutes <= 0) {
            return SingleResponse.buildFailure("400", "用户ID和时间范围参数无效");
        }

        // 计算时间范围
        Calendar calendar = Calendar.getInstance();
        Date endTime = calendar.getTime();
        calendar.add(Calendar.MINUTE, -timeRangeMinutes);
        Date startTime = calendar.getTime();

        // 默认阈值：每分钟30次操作为异常
        int threshold = 30;

        List<OperationLog> abnormalOperations = operationLogGateway.findAbnormalOperations(userId, startTime, endTime, threshold);

        if (CollectionUtils.isEmpty(abnormalOperations)) {
            return SingleResponse.of(Collections.emptyList());
        }

        List<OperationLogDTO> operationLogDTOs = abnormalOperations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return SingleResponse.of(operationLogDTOs);
    }

    /**
     * 将领域对象转换为DTO对象
     *
     * @param operationLog 操作日志领域对象
     * @return 操作日志DTO对象
     */
    private OperationLogDTO convertToDTO(OperationLog operationLog) {
        if (operationLog == null) {
            return null;
        }

        OperationLogDTO operationLogDTO = new OperationLogDTO();
        BeanUtils.copyProperties(operationLog, operationLogDTO);
        return operationLogDTO;
    }

    /**
     * 将统计领域对象转换为统计DTO对象
     *
     * @param statistics 操作日志统计领域对象
     * @return 操作日志统计DTO对象
     */
    private OperationLogStatisticsDTO convertToStatisticsDTO(OperationLogStatistics statistics) {
        if (statistics == null) {
            return null;
        }

        OperationLogStatisticsDTO statisticsDTO = new OperationLogStatisticsDTO();
        BeanUtils.copyProperties(statistics, statisticsDTO);
        return statisticsDTO;
    }

    /**
     * 导出为CSV格式
     *
     * @param operationLogs 操作日志列表
     * @param filePath      文件路径
     * @throws Exception 异常
     */
    private void exportToCSV(List<OperationLog> operationLogs, String filePath) throws Exception {
        StringBuilder sb = new StringBuilder();
        // 添加CSV表头
        sb.append("ID,用户ID,用户名,操作类型,模块,目标ID,描述,操作结果,严重级别,是否敏感操作,关联操作ID,创建时间,IP地址\n");

        // 添加数据
        for (OperationLog log : operationLogs) {
            sb.append(log.getId()).append(",")
                    .append(log.getUserId()).append(",")
                    .append(log.getUsername()).append(",")
                    .append(log.getOperationType()).append(",")
                    .append(log.getModule()).append(",")
                    .append(log.getTargetId()).append(",")
                    .append(escapeCSV(log.getDescription())).append(",")
                    .append(log.getOperationResult()).append(",")
                    .append(log.getSeverityLevel()).append(",")
                    .append(log.getIsSensitive()).append(",")
                    .append(log.getRelatedOperationId()).append(",")
                    .append(formatDate(log.getCreatedAt())).append(",")
                    .append(log.getIpAddress()).append("\n");
        }

        // 写入文件
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * 导出为Excel格式
     *
     * @param operationLogs 操作日志列表
     * @param filePath      文件路径
     * @throws Exception 异常
     */
    private void exportToExcel(List<OperationLog> operationLogs, String filePath) throws Exception {
        // 实际项目中，这里应该使用Apache POI或类似库创建Excel文件
        // 此处为简化，仅提供一个示例实现的框架
        throw new UnsupportedOperationException("Excel导出功能尚未实现，请使用CSV格式");
    }

    /**
     * 导出为PDF格式
     *
     * @param operationLogs 操作日志列表
     * @param filePath      文件路径
     * @throws Exception 异常
     */
    private void exportToPDF(List<OperationLog> operationLogs, String filePath) throws Exception {
        // 实际项目中，这里应该使用iText或类似库创建PDF文件
        // 此处为简化，仅提供一个示例实现的框架
        throw new UnsupportedOperationException("PDF导出功能尚未实现，请使用CSV格式");
    }

    /**
     * 转义CSV字符串
     *
     * @param str 原始字符串
     * @return 转义后的字符串
     */
    private String escapeCSV(String str) {
        if (str == null) {
            return "";
        }
        // 如果字符串包含逗号、引号或换行符，需要用引号包围并转义内部引号
        if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }

    /**
     * 格式化日期
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    private String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }
}
