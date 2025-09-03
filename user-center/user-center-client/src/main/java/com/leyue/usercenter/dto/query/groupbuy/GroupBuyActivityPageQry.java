package com.leyue.usercenter.dto.query.groupbuy;

import com.alibaba.cola.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class GroupBuyActivityPageQry extends PageQuery {
    
    private String name;
    
    private String status;
    
    private Long productId;
}