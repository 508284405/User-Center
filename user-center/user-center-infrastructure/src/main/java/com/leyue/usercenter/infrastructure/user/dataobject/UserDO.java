package com.leyue.usercenter.infrastructure.user.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import com.leyue.usercenter.infrastructure.common.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class UserDO extends BaseDO {

    private String username;

    private String password;

    @TableField("google_id")
    private String googleId;

    @TableField("wechat_open_id")
    private String wechatOpenId;

    @TableField("alipay_id")
    private String alipayId;

    private String email;

    private String avatar;

    private Integer status;
}