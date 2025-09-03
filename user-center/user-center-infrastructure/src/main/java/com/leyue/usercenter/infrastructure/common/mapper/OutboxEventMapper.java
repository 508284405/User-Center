package com.leyue.usercenter.infrastructure.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.usercenter.infrastructure.common.dataobject.OutboxEventDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Outbox事件Mapper
 * 
 * @author Claude Code
 */
@Mapper
public interface OutboxEventMapper extends BaseMapper<OutboxEventDO> {
    
    @Select("SELECT * FROM uc_outbox_event " +
            "WHERE status = 'NEW' OR (status = 'FAILED' AND next_retry_at <= NOW()) " +
            "AND is_deleted = 0 " +
            "ORDER BY created_at ASC LIMIT #{limit}")
    List<OutboxEventDO> findPendingEvents(@Param("limit") int limit);
}