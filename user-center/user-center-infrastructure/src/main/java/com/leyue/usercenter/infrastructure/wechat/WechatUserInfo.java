package com.leyue.usercenter.infrastructure.wechat;

import lombok.Data;

@Data
public class WechatUserInfo {
    private String openId;
    private String nickname;
    private String avatar;
} 