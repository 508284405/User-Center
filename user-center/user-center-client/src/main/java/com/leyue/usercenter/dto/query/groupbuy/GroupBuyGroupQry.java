package com.leyue.usercenter.dto.query.groupbuy;

import com.alibaba.cola.dto.Query;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class GroupBuyGroupQry extends Query {
    
    private Long id;
    
    private String groupNumber;
}