package com.leyue.usercenter.app.groupbuy;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.api.GroupBuyServiceI;
import com.leyue.usercenter.domain.groupbuy.GroupBuyActivity;
import com.leyue.usercenter.domain.groupbuy.GroupBuyGroup;
import com.leyue.usercenter.domain.groupbuy.gateway.GroupBuyActivityGateway;
import com.leyue.usercenter.domain.groupbuy.gateway.GroupBuyGroupGateway;
import com.leyue.usercenter.dto.command.groupbuy.CreateGroupBuyActivityCmd;
import com.leyue.usercenter.dto.command.groupbuy.CreateGroupCmd;
import com.leyue.usercenter.dto.command.groupbuy.JoinGroupCmd;
import com.leyue.usercenter.dto.command.groupbuy.UpdateGroupBuyActivityCmd;
import com.leyue.usercenter.dto.data.groupbuy.GroupBuyActivityDTO;
import com.leyue.usercenter.dto.data.groupbuy.GroupBuyGroupDTO;
import com.leyue.usercenter.dto.query.groupbuy.GroupBuyActivityPageQry;
import com.leyue.usercenter.dto.query.groupbuy.GroupBuyActivityQry;
import com.leyue.usercenter.dto.query.groupbuy.GroupBuyGroupPageQry;
import com.leyue.usercenter.dto.query.groupbuy.GroupBuyGroupQry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GroupBuyServiceImpl implements GroupBuyServiceI {

    @Resource
    private GroupBuyActivityGateway groupBuyActivityGateway;

    @Resource
    private GroupBuyGroupGateway groupBuyGroupGateway;

    @Override
    @Transactional
    public SingleResponse<GroupBuyActivityDTO> createActivity(CreateGroupBuyActivityCmd cmd) {
        log.info("创建团购活动: {}", cmd.getName());
        
        try {
            GroupBuyActivity activity = new GroupBuyActivity(
                cmd.getName(),
                cmd.getDescription(),
                cmd.getStartTime(),
                cmd.getEndTime(),
                cmd.getProductId(),
                cmd.getProductName(),
                cmd.getOriginalPrice(),
                cmd.getGroupPrice(),
                cmd.getRequiredParticipants(),
                cmd.getTotalStock(),
                cmd.getCreatedBy()
            );
            
            // 设置可选参数
            if (cmd.getMaxParticipants() != null) {
                activity.setMaxParticipants(cmd.getMaxParticipants());
            }
            if (cmd.getLimitPerUser() != null) {
                activity.setLimitPerUser(cmd.getLimitPerUser());
            }
            if (cmd.getGroupTimeoutHours() != null) {
                activity.setGroupTimeoutHours(cmd.getGroupTimeoutHours());
            }
            
            GroupBuyActivity savedActivity = groupBuyActivityGateway.save(activity);
            return SingleResponse.of(convertToActivityDTO(savedActivity));
            
        } catch (Exception e) {
            log.error("创建团购活动失败", e);
            return SingleResponse.buildFailure("创建团购活动失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public SingleResponse<GroupBuyActivityDTO> updateActivity(UpdateGroupBuyActivityCmd cmd) {
        log.info("更新团购活动: {}", cmd.getId());
        
        GroupBuyActivity activity = groupBuyActivityGateway.getById(cmd.getId());
        if (activity == null) {
            return SingleResponse.buildFailure("团购活动不存在");
        }
        
        if (activity.getStatus() != GroupBuyActivity.GroupBuyStatus.NOT_STARTED) {
            return SingleResponse.buildFailure("只有未开始的活动才能编辑");
        }
        
        try {
            // 更新基础信息
            if (cmd.getName() != null) {
                activity.setName(cmd.getName());
            }
            if (cmd.getDescription() != null) {
                activity.setDescription(cmd.getDescription());
            }
            if (cmd.getStartTime() != null) {
                activity.setStartTime(cmd.getStartTime());
            }
            if (cmd.getEndTime() != null) {
                activity.setEndTime(cmd.getEndTime());
            }
            if (cmd.getUpdatedBy() != null) {
                activity.setUpdatedBy(cmd.getUpdatedBy());
                activity.setUpdatedAt(LocalDateTime.now());
            }
            
            GroupBuyActivity updatedActivity = groupBuyActivityGateway.save(activity);
            return SingleResponse.of(convertToActivityDTO(updatedActivity));
            
        } catch (Exception e) {
            log.error("更新团购活动失败", e);
            return SingleResponse.buildFailure("更新团购活动失败: " + e.getMessage());
        }
    }

    @Override
    public SingleResponse<GroupBuyActivityDTO> getActivity(GroupBuyActivityQry qry) {
        log.info("查询团购活动: {}", qry.getId());
        
        GroupBuyActivity activity = groupBuyActivityGateway.getById(qry.getId());
        if (activity == null) {
            return SingleResponse.buildFailure("团购活动不存在");
        }
        
        return SingleResponse.of(convertToActivityDTO(activity));
    }

    @Override
    public PageResponse<GroupBuyActivityDTO> getActivityPage(GroupBuyActivityPageQry qry) {
        log.info("分页查询团购活动, pageNum: {}, pageSize: {}", qry.getPageIndex(), qry.getPageSize());
        
        List<GroupBuyActivity> activities = groupBuyActivityGateway.getByPage(
                qry.getPageIndex(),
                qry.getPageSize(),
                qry.getName(),
                qry.getStatus() != null ? GroupBuyActivity.GroupBuyStatus.valueOf(qry.getStatus()) : null,
                qry.getProductId()
        );
        
        long totalCount = groupBuyActivityGateway.countByPage(
                qry.getName(),
                qry.getStatus() != null ? GroupBuyActivity.GroupBuyStatus.valueOf(qry.getStatus()) : null,
                qry.getProductId()
        );
        
        List<GroupBuyActivityDTO> dtoList = activities.stream()
                .map(this::convertToActivityDTO)
                .collect(Collectors.toList());
        
        return PageResponse.of(dtoList, (int) totalCount, qry.getPageIndex(), qry.getPageSize());
    }

    @Override
    public MultiResponse<GroupBuyActivityDTO> getActiveActivities() {
        log.info("查询进行中的团购活动");
        
        List<GroupBuyActivity> activities = groupBuyActivityGateway.getActiveActivities();
        List<GroupBuyActivityDTO> dtoList = activities.stream()
                .map(this::convertToActivityDTO)
                .collect(Collectors.toList());
        
        return MultiResponse.of(dtoList);
    }

    @Override
    @Transactional
    public Response deleteActivity(Long id) {
        log.info("删除团购活动: {}", id);
        
        GroupBuyActivity activity = groupBuyActivityGateway.getById(id);
        if (activity == null) {
            return Response.buildFailure("团购活动不存在");
        }
        
        if (activity.getStatus() != GroupBuyActivity.GroupBuyStatus.NOT_STARTED) {
            return Response.buildFailure("只有未开始的活动才能删除");
        }
        
        groupBuyActivityGateway.delete(id);
        return Response.buildSuccess();
    }

    @Override
    @Transactional
    public Response startActivity(Long id) {
        log.info("启动团购活动: {}", id);
        
        GroupBuyActivity activity = groupBuyActivityGateway.getById(id);
        if (activity == null) {
            return Response.buildFailure("团购活动不存在");
        }
        
        try {
            activity.start();
            groupBuyActivityGateway.save(activity);
            return Response.buildSuccess();
        } catch (Exception e) {
            log.error("启动团购活动失败", e);
            return Response.buildFailure("启动失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Response pauseActivity(Long id) {
        log.info("暂停团购活动: {}", id);
        
        GroupBuyActivity activity = groupBuyActivityGateway.getById(id);
        if (activity == null) {
            return Response.buildFailure("团购活动不存在");
        }
        
        try {
            activity.pause();
            groupBuyActivityGateway.save(activity);
            return Response.buildSuccess();
        } catch (Exception e) {
            log.error("暂停团购活动失败", e);
            return Response.buildFailure("暂停失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Response endActivity(Long id) {
        log.info("结束团购活动: {}", id);
        
        GroupBuyActivity activity = groupBuyActivityGateway.getById(id);
        if (activity == null) {
            return Response.buildFailure("团购活动不存在");
        }
        
        activity.end();
        groupBuyActivityGateway.save(activity);
        return Response.buildSuccess();
    }

    @Override
    @Transactional
    public SingleResponse<GroupBuyGroupDTO> createGroup(CreateGroupCmd cmd) {
        log.info("创建团购团组, activityId: {}, leaderUserId: {}", cmd.getActivityId(), cmd.getLeaderUserId());
        
        GroupBuyActivity activity = groupBuyActivityGateway.getById(cmd.getActivityId());
        if (activity == null) {
            return SingleResponse.buildFailure("团购活动不存在");
        }
        
        try {
            GroupBuyGroup group = activity.createGroup(cmd.getLeaderUserId(), cmd.getQuantity());
            GroupBuyGroup savedGroup = groupBuyGroupGateway.save(group);
            
            // 更新活动统计
            activity.increaseGroupCount();
            groupBuyActivityGateway.save(activity);
            
            return SingleResponse.of(convertToGroupDTO(savedGroup));
            
        } catch (Exception e) {
            log.error("创建团购团组失败", e);
            return SingleResponse.buildFailure("创建团组失败: " + e.getMessage());
        }
    }

    @Override
    public SingleResponse<GroupBuyGroupDTO> getGroup(GroupBuyGroupQry qry) {
        log.info("查询团购团组: {}", qry.getId());
        
        GroupBuyGroup group = null;
        
        if (qry.getId() != null) {
            group = groupBuyGroupGateway.getById(qry.getId());
        } else if (qry.getGroupNumber() != null) {
            group = groupBuyGroupGateway.getByGroupNumber(qry.getGroupNumber());
        }
        
        if (group == null) {
            return SingleResponse.buildFailure("团组不存在");
        }
        
        return SingleResponse.of(convertToGroupDTO(group));
    }

    @Override
    public PageResponse<GroupBuyGroupDTO> getGroupPage(GroupBuyGroupPageQry qry) {
        log.info("分页查询团购团组, pageNum: {}, pageSize: {}", qry.getPageIndex(), qry.getPageSize());
        
        List<GroupBuyGroup> groups = groupBuyGroupGateway.getByPage(
                qry.getPageIndex(),
                qry.getPageSize(),
                qry.getActivityId(),
                qry.getStatus() != null ? GroupBuyGroup.GroupStatus.valueOf(qry.getStatus()) : null,
                qry.getLeaderUserId()
        );
        
        long totalCount = groupBuyGroupGateway.countByPage(
                qry.getActivityId(),
                qry.getStatus() != null ? GroupBuyGroup.GroupStatus.valueOf(qry.getStatus()) : null,
                qry.getLeaderUserId()
        );
        
        List<GroupBuyGroupDTO> dtoList = groups.stream()
                .map(this::convertToGroupDTO)
                .collect(Collectors.toList());
        
        return PageResponse.of(dtoList, (int) totalCount, qry.getPageIndex(), qry.getPageSize());
    }

    @Override
    public MultiResponse<GroupBuyGroupDTO> getGroupsByActivity(Long activityId) {
        log.info("查询活动的团组列表, activityId: {}", activityId);
        
        List<GroupBuyGroup> groups = groupBuyGroupGateway.getByActivityId(activityId);
        List<GroupBuyGroupDTO> dtoList = groups.stream()
                .map(this::convertToGroupDTO)
                .collect(Collectors.toList());
        
        return MultiResponse.of(dtoList);
    }

    @Override
    public MultiResponse<GroupBuyGroupDTO> getUserGroups(String userId) {
        log.info("查询用户的团组列表, userId: {}", userId);
        
        List<GroupBuyGroup> groups = groupBuyGroupGateway.getByLeaderUserId(userId);
        List<GroupBuyGroupDTO> dtoList = groups.stream()
                .map(this::convertToGroupDTO)
                .collect(Collectors.toList());
        
        return MultiResponse.of(dtoList);
    }

    @Override
    @Transactional
    public Response joinGroup(JoinGroupCmd cmd) {
        log.info("参团请求, groupId: {}, userId: {}", cmd.getGroupId(), cmd.getUserId());
        
        GroupBuyGroup group = groupBuyGroupGateway.getById(cmd.getGroupId());
        if (group == null) {
            return Response.buildFailure("团组不存在");
        }
        
        try {
            boolean success = group.joinGroup(cmd.getUserId(), cmd.getQuantity());
            if (success) {
                groupBuyGroupGateway.save(group);
                return Response.buildSuccess();
            } else {
                return Response.buildFailure("参团失败，团组可能已满或已过期");
            }
        } catch (Exception e) {
            log.error("参团失败", e);
            return Response.buildFailure("参团失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Response leaveGroup(Long groupId, String userId) {
        log.info("退团请求, groupId: {}, userId: {}", groupId, userId);
        
        GroupBuyGroup group = groupBuyGroupGateway.getById(groupId);
        if (group == null) {
            return Response.buildFailure("团组不存在");
        }
        
        try {
            boolean success = group.leaveGroup(userId);
            if (success) {
                groupBuyGroupGateway.save(group);
                return Response.buildSuccess();
            } else {
                return Response.buildFailure("退团失败，可能您不在该团组中");
            }
        } catch (Exception e) {
            log.error("退团失败", e);
            return Response.buildFailure("退团失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Response dissolveGroup(Long groupId, String operatorUserId) {
        log.info("解散团组请求, groupId: {}, operatorUserId: {}", groupId, operatorUserId);
        
        GroupBuyGroup group = groupBuyGroupGateway.getById(groupId);
        if (group == null) {
            return Response.buildFailure("团组不存在");
        }
        
        // 检查操作权限（团长或管理员）
        if (!group.getLeaderUserId().equals(operatorUserId)) {
            return Response.buildFailure("只有团长才能解散团组");
        }
        
        try {
            group.dissolve();
            groupBuyGroupGateway.save(group);
            return Response.buildSuccess();
        } catch (Exception e) {
            log.error("解散团组失败", e);
            return Response.buildFailure("解散失败: " + e.getMessage());
        }
    }

    private GroupBuyActivityDTO convertToActivityDTO(GroupBuyActivity activity) {
        GroupBuyActivityDTO dto = new GroupBuyActivityDTO();
        BeanUtils.copyProperties(activity, dto);
        
        if (activity.getStatus() != null) {
            dto.setStatus(activity.getStatus().name());
            dto.setStatusDesc(activity.getStatus().getDescription());
        }
        
        return dto;
    }

    private GroupBuyGroupDTO convertToGroupDTO(GroupBuyGroup group) {
        GroupBuyGroupDTO dto = new GroupBuyGroupDTO();
        BeanUtils.copyProperties(group, dto);
        
        if (group.getStatus() != null) {
            dto.setStatus(group.getStatus().name());
            dto.setStatusDesc(group.getStatus().getDescription());
        }
        
        return dto;
    }
}