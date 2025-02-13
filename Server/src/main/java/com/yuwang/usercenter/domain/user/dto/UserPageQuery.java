package com.yuwang.usercenter.domain.user.dto;

import com.yuwang.usercenter.domain.BasePageQuery;
import lombok.Data;

@Data
public class UserPageQuery extends BasePageQuery {
    private String username;
    private String email;
    private String phone;
    private Integer status;
}