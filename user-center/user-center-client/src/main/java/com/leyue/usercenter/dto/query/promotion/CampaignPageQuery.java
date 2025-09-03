package com.leyue.usercenter.dto.query.promotion;

import com.alibaba.cola.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CampaignPageQuery extends PageQuery {
    
    private String name;
    private String status;
    private String type;
    private String createdBy;
}