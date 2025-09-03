package com.leyue.usercenter.dto.query.groupbuy;

import com.alibaba.cola.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class GroupBuyGroupPageQry extends PageQuery {
    
    private Long activityId;
    
    private String status;
    
    private String leaderUserId;
}