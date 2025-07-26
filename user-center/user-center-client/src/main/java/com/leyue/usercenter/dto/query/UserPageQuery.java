package com.leyue.usercenter.dto.query;

import lombok.Data;

@Data
public class UserPageQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String username;
    private String email;
    private Integer status;
    private Boolean isGoogleUser;
} 