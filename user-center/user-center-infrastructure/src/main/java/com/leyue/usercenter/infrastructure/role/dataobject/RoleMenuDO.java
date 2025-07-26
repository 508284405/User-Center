package com.leyue.usercenter.infrastructure.role.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.usercenter.infrastructure.common.BaseDO;
import lombok.Data;

@Data
@TableName("role_menu")
public class RoleMenuDO extends BaseDO {
    private Long roleId;
    private Long menuId;
}