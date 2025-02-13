package com.yuwang.usercenter.domain.log.repository;

import com.yuwang.usercenter.domain.log.entity.OperationLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
    @Select("SELECT * FROM operation_logs WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<OperationLog> findByUserId(Long userId);

    @Select("SELECT * FROM operation_logs WHERE operation_type = #{operationType} ORDER BY created_at DESC")
    List<OperationLog> findByOperationType(String operationType);

    @Select("SELECT * FROM operation_logs WHERE created_at BETWEEN #{startTime} AND #{endTime} ORDER BY created_at DESC")
    List<OperationLog> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    @Select("SELECT * FROM operation_logs WHERE system_id = #{systemId} ORDER BY created_at DESC")
    List<OperationLog> findBySystemId(Long systemId);

    @Select("SELECT * FROM operation_logs WHERE ip_address = #{ipAddress} ORDER BY created_at DESC")
    List<OperationLog> findByIpAddress(String ipAddress);

    @Select("SELECT * FROM operation_logs WHERE status = #{status} ORDER BY created_at DESC")
    List<OperationLog> findByStatus(String status);
}