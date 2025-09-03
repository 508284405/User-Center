package com.leyue.usercenter.infrastructure.groupbuy;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.usercenter.domain.groupbuy.GroupBuyActivity;
import com.leyue.usercenter.domain.groupbuy.gateway.GroupBuyActivityGateway;
import com.leyue.usercenter.infrastructure.common.dataobject.GroupBuyActivityDO;
import com.leyue.usercenter.infrastructure.common.mapper.GroupBuyActivityMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GroupBuyActivityGatewayImpl implements GroupBuyActivityGateway {

    @Resource
    private GroupBuyActivityMapper groupBuyActivityMapper;

    @Override
    public GroupBuyActivity save(GroupBuyActivity activity) {
        GroupBuyActivityDO activityDO = convertToActivityDO(activity);
        
        if (activity.getId() == null) {
            groupBuyActivityMapper.insert(activityDO);
            activity.setId(activityDO.getId());
        } else {
            groupBuyActivityMapper.updateById(activityDO);
        }
        
        return activity;
    }

    @Override
    public GroupBuyActivity getById(Long id) {
        GroupBuyActivityDO activityDO = groupBuyActivityMapper.selectById(id);
        return activityDO != null ? convertToGroupBuyActivity(activityDO) : null;
    }

    @Override
    public List<GroupBuyActivity> getByStatus(GroupBuyActivity.GroupBuyStatus status) {
        LambdaQueryWrapper<GroupBuyActivityDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupBuyActivityDO::getStatus, status)
               .orderByDesc(GroupBuyActivityDO::getCreatedAt);
        
        List<GroupBuyActivityDO> activityDOs = groupBuyActivityMapper.selectList(wrapper);
        return activityDOs.stream()
                .map(this::convertToGroupBuyActivity)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupBuyActivity> getActiveActivities() {
        LocalDateTime now = LocalDateTime.now();
        
        LambdaQueryWrapper<GroupBuyActivityDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupBuyActivityDO::getStatus, GroupBuyActivity.GroupBuyStatus.ONGOING)
               .le(GroupBuyActivityDO::getStartTime, now)
               .ge(GroupBuyActivityDO::getEndTime, now)
               .gt(GroupBuyActivityDO::getAvailableStock, 0)
               .orderByDesc(GroupBuyActivityDO::getCreatedAt);
        
        List<GroupBuyActivityDO> activityDOs = groupBuyActivityMapper.selectList(wrapper);
        return activityDOs.stream()
                .map(this::convertToGroupBuyActivity)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupBuyActivity> getByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<GroupBuyActivityDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(GroupBuyActivityDO::getStartTime, startTime)
               .le(GroupBuyActivityDO::getEndTime, endTime)
               .orderByDesc(GroupBuyActivityDO::getCreatedAt);
        
        List<GroupBuyActivityDO> activityDOs = groupBuyActivityMapper.selectList(wrapper);
        return activityDOs.stream()
                .map(this::convertToGroupBuyActivity)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupBuyActivity> getByProductId(Long productId) {
        LambdaQueryWrapper<GroupBuyActivityDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupBuyActivityDO::getProductId, productId)
               .orderByDesc(GroupBuyActivityDO::getCreatedAt);
        
        List<GroupBuyActivityDO> activityDOs = groupBuyActivityMapper.selectList(wrapper);
        return activityDOs.stream()
                .map(this::convertToGroupBuyActivity)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupBuyActivity> getByPage(int pageNum, int pageSize, String name,
                                           GroupBuyActivity.GroupBuyStatus status, Long productId) {
        Page<GroupBuyActivityDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<GroupBuyActivityDO> wrapper = new LambdaQueryWrapper<>();
        
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like(GroupBuyActivityDO::getName, name);
        }
        if (status != null) {
            wrapper.eq(GroupBuyActivityDO::getStatus, status);
        }
        if (productId != null) {
            wrapper.eq(GroupBuyActivityDO::getProductId, productId);
        }
        
        wrapper.orderByDesc(GroupBuyActivityDO::getCreatedAt);
        
        IPage<GroupBuyActivityDO> pageResult = groupBuyActivityMapper.selectPage(page, wrapper);
        return pageResult.getRecords().stream()
                .map(this::convertToGroupBuyActivity)
                .collect(Collectors.toList());
    }

    @Override
    public long countByPage(String name, GroupBuyActivity.GroupBuyStatus status, Long productId) {
        LambdaQueryWrapper<GroupBuyActivityDO> wrapper = new LambdaQueryWrapper<>();
        
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like(GroupBuyActivityDO::getName, name);
        }
        if (status != null) {
            wrapper.eq(GroupBuyActivityDO::getStatus, status);
        }
        if (productId != null) {
            wrapper.eq(GroupBuyActivityDO::getProductId, productId);
        }
        
        return groupBuyActivityMapper.selectCount(wrapper);
    }

    @Override
    public void delete(Long id) {
        groupBuyActivityMapper.deleteById(id);
    }

    @Override
    public List<GroupBuyActivity> getExpiredActivities() {
        LocalDateTime now = LocalDateTime.now();
        
        LambdaQueryWrapper<GroupBuyActivityDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupBuyActivityDO::getStatus, GroupBuyActivity.GroupBuyStatus.ONGOING)
               .lt(GroupBuyActivityDO::getEndTime, now);
        
        List<GroupBuyActivityDO> activityDOs = groupBuyActivityMapper.selectList(wrapper);
        return activityDOs.stream()
                .map(this::convertToGroupBuyActivity)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupBuyActivity> getActivitiesByCreator(String createdBy) {
        LambdaQueryWrapper<GroupBuyActivityDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupBuyActivityDO::getCreatedBy, createdBy)
               .orderByDesc(GroupBuyActivityDO::getCreatedAt);
        
        List<GroupBuyActivityDO> activityDOs = groupBuyActivityMapper.selectList(wrapper);
        return activityDOs.stream()
                .map(this::convertToGroupBuyActivity)
                .collect(Collectors.toList());
    }

    private GroupBuyActivityDO convertToActivityDO(GroupBuyActivity activity) {
        GroupBuyActivityDO activityDO = new GroupBuyActivityDO();
        BeanUtils.copyProperties(activity, activityDO);
        return activityDO;
    }

    private GroupBuyActivity convertToGroupBuyActivity(GroupBuyActivityDO activityDO) {
        GroupBuyActivity activity = new GroupBuyActivity();
        BeanUtils.copyProperties(activityDO, activity);
        return activity;
    }
}