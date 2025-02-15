package com.yuwang.usercenter.domain.menu.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MenuUpdateRequest extends MenuCreateRequest {
    // 继承MenuCreateRequest的所有字段和验证规则
    // 如果将来需要添加仅更新操作特有的字段或覆盖验证规则，可以在这里添加
}