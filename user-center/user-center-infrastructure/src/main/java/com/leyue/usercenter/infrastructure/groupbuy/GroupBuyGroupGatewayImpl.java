package com.leyue.usercenter.infrastructure.groupbuy;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.usercenter.domain.groupbuy.GroupBuyGroup;
import com.leyue.usercenter.domain.groupbuy.gateway.GroupBuyGroupGateway;
import com.leyue.usercenter.infrastructure.common.dataobject.GroupBuyGroupDO;
import com.leyue.usercenter.infrastructure.common.mapper.GroupBuyGroupMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GroupBuyGroupGatewayImpl implements GroupBuyGroupGateway {

    @Resource
    private GroupBuyGroupMapper groupBuyGroupMapper;

    @Override
    public GroupBuyGroup save(GroupBuyGroup group) {
        GroupBuyGroupDO groupDO = convertToGroupDO(group);
        
        if (group.getId() == null) {
            groupBuyGroupMapper.insert(groupDO);
            group.setId(groupDO.getId());
        } else {
            groupBuyGroupMapper.updateById(groupDO);
        }
        
        return group;
    }

    @Override
    public GroupBuyGroup getById(Long id) {
        GroupBuyGroupDO groupDO = groupBuyGroupMapper.selectById(id);
        return groupDO != null ? convertToGroupBuyGroup(groupDO) : null;
    }

    @Override
    public GroupBuyGroup getByGroupNumber(String groupNumber) {
        LambdaQueryWrapper<GroupBuyGroupDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupBuyGroupDO::getGroupNumber, groupNumber);
        
        GroupBuyGroupDO groupDO = groupBuyGroupMapper.selectOne(wrapper);
        return groupDO != null ? convertToGroupBuyGroup(groupDO) : null;
    }

    @Override
    public List<GroupBuyGroup> getByActivityId(Long activityId) {
        LambdaQueryWrapper<GroupBuyGroupDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupBuyGroupDO::getActivityId, activityId)
               .orderByDesc(GroupBuyGroupDO::getCreatedAt);
        
        List<GroupBuyGroupDO> groupDOs = groupBuyGroupMapper.selectList(wrapper);
        return groupDOs.stream()
                .map(this::convertToGroupBuyGroup)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupBuyGroup> getByStatus(GroupBuyGroup.GroupStatus status) {
        LambdaQueryWrapper<GroupBuyGroupDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupBuyGroupDO::getStatus, status)
               .orderByDesc(GroupBuyGroupDO::getCreatedAt);
        
        List<GroupBuyGroupDO> groupDOs = groupBuyGroupMapper.selectList(wrapper);
        return groupDOs.stream()
                .map(this::convertToGroupBuyGroup)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupBuyGroup> getByLeaderUserId(String leaderUserId) {
        LambdaQueryWrapper<GroupBuyGroupDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupBuyGroupDO::getLeaderUserId, leaderUserId)
               .orderByDesc(GroupBuyGroupDO::getCreatedAt);
        
        List<GroupBuyGroupDO> groupDOs = groupBuyGroupMapper.selectList(wrapper);
        return groupDOs.stream()
                .map(this::convertToGroupBuyGroup)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupBuyGroup> getByParticipantUserId(String userId) {
        // 这里需要关联查询参团记录表，暂时返回空列表
        return List.of();
    }

    @Override
    public List<GroupBuyGroup> getExpiredGroups() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<GroupBuyGroupDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupBuyGroupDO::getStatus, GroupBuyGroup.GroupStatus.FORMING)
               .lt(GroupBuyGroupDO::getExpireTime, now);
        
        List<GroupBuyGroupDO> groupDOs = groupBuyGroupMapper.selectList(wrapper);
        return groupDOs.stream()
                .map(this::convertToGroupBuyGroup)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupBuyGroup> getFormingGroups() {
        LambdaQueryWrapper<GroupBuyGroupDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupBuyGroupDO::getStatus, GroupBuyGroup.GroupStatus.FORMING)
               .orderByDesc(GroupBuyGroupDO::getCreatedAt);
        
        List<GroupBuyGroupDO> groupDOs = groupBuyGroupMapper.selectList(wrapper);
        return groupDOs.stream()
                .map(this::convertToGroupBuyGroup)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupBuyGroup> getByActivityAndStatus(Long activityId, GroupBuyGroup.GroupStatus status) {
        LambdaQueryWrapper<GroupBuyGroupDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupBuyGroupDO::getActivityId, activityId)
               .eq(GroupBuyGroupDO::getStatus, status)
               .orderByDesc(GroupBuyGroupDO::getCreatedAt);
        
        List<GroupBuyGroupDO> groupDOs = groupBuyGroupMapper.selectList(wrapper);
        return groupDOs.stream()
                .map(this::convertToGroupBuyGroup)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupBuyGroup> getByPage(int pageNum, int pageSize, Long activityId,
                                        GroupBuyGroup.GroupStatus status, String leaderUserId) {
        Page<GroupBuyGroupDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<GroupBuyGroupDO> wrapper = new LambdaQueryWrapper<>();
        
        if (activityId != null) {
            wrapper.eq(GroupBuyGroupDO::getActivityId, activityId);
        }
        if (status != null) {
            wrapper.eq(GroupBuyGroupDO::getStatus, status);
        }
        if (leaderUserId != null && !leaderUserId.trim().isEmpty()) {
            wrapper.eq(GroupBuyGroupDO::getLeaderUserId, leaderUserId);
        }
        
        wrapper.orderByDesc(GroupBuyGroupDO::getCreatedAt);
        
        IPage<GroupBuyGroupDO> pageResult = groupBuyGroupMapper.selectPage(page, wrapper);
        return pageResult.getRecords().stream()
                .map(this::convertToGroupBuyGroup)
                .collect(Collectors.toList());
    }

    @Override
    public long countByPage(Long activityId, GroupBuyGroup.GroupStatus status, String leaderUserId) {
        LambdaQueryWrapper<GroupBuyGroupDO> wrapper = new LambdaQueryWrapper<>();
        
        if (activityId != null) {
            wrapper.eq(GroupBuyGroupDO::getActivityId, activityId);
        }
        if (status != null) {
            wrapper.eq(GroupBuyGroupDO::getStatus, status);
        }
        if (leaderUserId != null && !leaderUserId.trim().isEmpty()) {
            wrapper.eq(GroupBuyGroupDO::getLeaderUserId, leaderUserId);
        }
        
        return groupBuyGroupMapper.selectCount(wrapper);
    }

    @Override
    public void delete(Long id) {
        groupBuyGroupMapper.deleteById(id);
    }

    @Override
    public int countSuccessfulGroupsByActivity(Long activityId) {
        return groupBuyGroupMapper.countSuccessfulGroupsByActivity(activityId);
    }

    @Override
    public int countTotalGroupsByActivity(Long activityId) {
        return groupBuyGroupMapper.countTotalGroupsByActivity(activityId);
    }

    @Override
    public List<GroupBuyGroup> getGroupsExpiredBefore(LocalDateTime expireTime) {
        List<GroupBuyGroupDO> groupDOs = groupBuyGroupMapper.selectExpiredFormingGroups(expireTime);
        return groupDOs.stream()
                .map(this::convertToGroupBuyGroup)
                .collect(Collectors.toList());
    }

    private GroupBuyGroupDO convertToGroupDO(GroupBuyGroup group) {
        GroupBuyGroupDO groupDO = new GroupBuyGroupDO();
        BeanUtils.copyProperties(group, groupDO);
        return groupDO;
    }

    private GroupBuyGroup convertToGroupBuyGroup(GroupBuyGroupDO groupDO) {
        GroupBuyGroup group = new GroupBuyGroup();
        BeanUtils.copyProperties(groupDO, group);
        return group;
    }
}