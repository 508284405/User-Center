package com.leyue.usercenter.domain.menu;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuPermission {
    private Long id;
    private Long menuId;
    private String permission;
    private String description;
    private Integer status; // 0-禁用 1-启用
    private Date createTime;
    private Date updateTime;
    
    public boolean isEnabled() {
        return status != null && status == 1;
    }
}