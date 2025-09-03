package com.leyue.usercenter.infrastructure.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.usercenter.infrastructure.common.dataobject.GroupBuyGroupDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface GroupBuyGroupMapper extends BaseMapper<GroupBuyGroupDO> {
    
    @Select("SELECT * FROM groupbuy_groups WHERE expire_time < #{expireTime} AND status = 'FORMING'")
    List<GroupBuyGroupDO> selectExpiredFormingGroups(LocalDateTime expireTime);
    
    @Select("SELECT COUNT(*) FROM groupbuy_groups WHERE activity_id = #{activityId} AND status = 'SUCCESS'")
    int countSuccessfulGroupsByActivity(Long activityId);
    
    @Select("SELECT COUNT(*) FROM groupbuy_groups WHERE activity_id = #{activityId}")
    int countTotalGroupsByActivity(Long activityId);
}