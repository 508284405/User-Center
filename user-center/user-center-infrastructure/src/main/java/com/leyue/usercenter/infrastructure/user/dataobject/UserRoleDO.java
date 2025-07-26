package com.leyue.usercenter.infrastructure.user.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.usercenter.infrastructure.common.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_role")
public class UserRoleDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
}