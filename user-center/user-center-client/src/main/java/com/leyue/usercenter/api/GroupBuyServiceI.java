package com.leyue.usercenter.api;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
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

public interface GroupBuyServiceI {
    
    SingleResponse<GroupBuyActivityDTO> createActivity(CreateGroupBuyActivityCmd cmd);
    
    SingleResponse<GroupBuyActivityDTO> updateActivity(UpdateGroupBuyActivityCmd cmd);
    
    SingleResponse<GroupBuyActivityDTO> getActivity(GroupBuyActivityQry qry);
    
    PageResponse<GroupBuyActivityDTO> getActivityPage(GroupBuyActivityPageQry qry);
    
    MultiResponse<GroupBuyActivityDTO> getActiveActivities();
    
    Response deleteActivity(Long id);
    
    Response startActivity(Long id);
    
    Response pauseActivity(Long id);
    
    Response endActivity(Long id);
    
    SingleResponse<GroupBuyGroupDTO> createGroup(CreateGroupCmd cmd);
    
    SingleResponse<GroupBuyGroupDTO> getGroup(GroupBuyGroupQry qry);
    
    PageResponse<GroupBuyGroupDTO> getGroupPage(GroupBuyGroupPageQry qry);
    
    MultiResponse<GroupBuyGroupDTO> getGroupsByActivity(Long activityId);
    
    MultiResponse<GroupBuyGroupDTO> getUserGroups(String userId);
    
    Response joinGroup(JoinGroupCmd cmd);
    
    Response leaveGroup(Long groupId, String userId);
    
    Response dissolveGroup(Long groupId, String operatorUserId);
}