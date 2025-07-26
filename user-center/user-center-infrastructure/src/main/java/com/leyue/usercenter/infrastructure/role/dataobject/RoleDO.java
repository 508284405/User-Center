package com.leyue.usercenter.infrastructure.role.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import com.leyue.usercenter.infrastructure.common.BaseDO;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("role")
public class RoleDO extends BaseDO {
    private String roleName;
    private String roleCode;
    private String description;
    private Integer status;
}