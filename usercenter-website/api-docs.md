# 用户中心API文档

## 目录
- [用户管理](#用户管理)
- [角色管理](#角色管理)
- [菜单管理](#菜单管理)
- [系统管理](#系统管理)
- [操作日志管理](#操作日志管理)
- [认证管理](#认证管理)

## 用户管理

### 创建用户
- **接口地址**: `/api/users`
- **请求方式**: POST
- **请求参数**:
```json
{
    "username": "string",    // 用户名
    "password": "string",    // 密码
    "email": "string",      // 邮箱
    "phone": "string"       // 手机号
}
```
- **返回值**:
```json
{
    "code": 200,            // 状态码
    "message": "success",   // 状态信息
    "data": {              // 返回数据
        "id": "long",      // 用户ID
        "username": "string", // 用户名
        "email": "string",   // 邮箱
        "phone": "string"    // 手机号
    }
}
```

### 更新用户信息
- **接口地址**: `/api/users/{id}`
- **请求方式**: PUT
- **请求参数**:
```json
{
    "email": "string",      // 邮箱
    "phone": "string"       // 手机号
}
```
- **返回值**: 同创建用户

### 锁定用户
- **接口地址**: `/api/users/{id}/lock`
- **请求方式**: PUT
- **请求参数**: 无
- **返回值**:
```json
{
    "code": 200,
    "message": "success"
}
```

### 解锁用户
- **接口地址**: `/api/users/{id}/unlock`
- **请求方式**: PUT
- **请求参数**: 无
- **返回值**: 同锁定用户

### 根据用户名查询用户
- **接口地址**: `/api/users/username/{username}`
- **请求方式**: GET
- **请求参数**: 无
- **返回值**: 同创建用户

### 根据邮箱查询用户
- **接口地址**: `/api/users/email/{email}`
- **请求方式**: GET
- **请求参数**: 无
- **返回值**: 同创建用户

### 根据手机号查询用户
- **接口地址**: `/api/users/phone/{phone}`
- **请求方式**: GET
- **请求参数**: 无
- **返回值**: 同创建用户

### 分页查询用户列表
- **接口地址**: `/api/users/page`
- **请求方式**: POST
- **请求参数**:
```json
{
    "pageNum": "integer",    // 页码
    "pageSize": "integer",   // 每页数量
    "username": "string",    // 用户名（可选）
    "email": "string",      // 邮箱（可选）
    "phone": "string"       // 手机号（可选）
}
```
- **返回值**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "total": "long",        // 总记录数
        "list": [               // 数据列表
            {                    // 用户信息
                "id": "long",
                "username": "string",
                "email": "string",
                "phone": "string"
            }
        ]
    }
}
```

## 角色管理

### 创建角色
- **接口地址**: `/api/roles`
- **请求方式**: POST
- **请求参数**:
```json
{
    "roleName": "string",    // 角色名称
    "roleCode": "string",    // 角色编码
    "systemId": "long",     // 所属系统ID
    "level": "integer"      // 角色级别
}
```
- **返回值**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": "long",          // 角色ID
        "roleName": "string",   // 角色名称
        "roleCode": "string",   // 角色编码
        "systemId": "long",    // 所属系统ID
        "level": "integer"     // 角色级别
    }
}
```

### 更新角色
- **接口地址**: `/api/roles/{id}`
- **请求方式**: PUT
- **请求参数**:
```json
{
    "roleName": "string",    // 角色名称
    "level": "integer"      // 角色级别
}
```
- **返回值**: 同创建角色

### 删除角色
- **接口地址**: `/api/roles/{id}`
- **请求方式**: DELETE
- **请求参数**: 无
- **返回值**:
```json
{
    "code": 200,
    "message": "success"
}
```

### 根据ID查询角色
- **接口地址**: `/api/roles/{id}`
- **请求方式**: GET
- **请求参数**: 无
- **返回值**: 同创建角色

### 根据角色名称查询角色
- **接口地址**: `/api/roles/name/{roleName}`
- **请求方式**: GET
- **请求参数**: 无
- **返回值**: 同创建角色

### 查询系统下的所有角色
- **接口地址**: `/api/roles/system/{systemId}`
- **请求方式**: GET
- **请求参数**: 无
- **返回值**:
```json
{
    "code": 200,
    "message": "success",
    "data": [                // 角色列表
        {                     // 角色信息
            "id": "long",
            "roleName": "string",
            "roleCode": "string",
            "systemId": "long",
            "level": "integer"
        }
    ]
}
```

### 查询指定级别的角色
- **接口地址**: `/api/roles/level/{level}`
- **请求方式**: GET
- **请求参数**: 无
- **返回值**: 同查询系统下的所有角色

### 查询所有角色
- **接口地址**: `/api/roles`
- **请求方式**: GET
- **请求参数**: 无
- **返回值**: 同查询系统下的所有角色

### 分页查询角色
- **接口地址**: `/api/roles/page`
- **请求方式**: POST
- **请求参数**:
```json
{
    "pageNum": "integer",    // 页码
    "pageSize": "integer",   // 每页数量
    "roleName": "string",    // 角色名称（可选）
    "roleCode": "string",    // 角色编码（可选）
    "systemId": "long"      // 系统ID（可选）
}
```
- **返回值**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "total": "long",        // 总记录数
        "list": [               // 数据列表
            {                    // 角色信息
                "id": "long",
                "roleName": "string",
                "roleCode": "string",
                "systemId": "long",
                "level": "integer"
            }
        ]
    }
}
```

## 菜单管理

### 创建菜单
- **接口地址**: `/api/menus`
- **请求方式**: POST
- **请求参数**:
```json
{
    "menuName": "string",    // 菜单名称
    "menuCode": "string",    // 菜单编码
    "systemId": "long",     // 所属系统ID
    "parentId": "long",     // 父菜单ID
    "path": "string",       // 菜单路径
    "component": "string",  // 前端组件
    "sort": "integer"       // 排序号
}
```
- **返回值**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": "long",          // 菜单ID
        "menuName": "string",   // 菜单名称
        "menuCode": "string",   // 菜单编码
        "systemId": "long",    // 所属系统ID
        "parentId": "long",    // 父菜单ID
        "path": "string",      // 菜单路径
        "component": "string", // 前端组件
        "sort": "integer"      // 排序号
    }
}
```

### 更新菜单
- **接口地址**: `/api/menus/{id}`
- **请求方式**: PUT
- **请求参数**:
```json
{
    "menuName": "string",    // 菜单名称
    "path": "string",       // 菜单路径
    "component": "string",  // 前端组件
    "sort": "integer"       // 排序号
}
```
- **返回值**: 同创建菜单

### 删除菜单
- **接口地址**: `/api/menus/{id}`
- **请求方式**: DELETE
- **请求参数**: 无
- **返回值**:
```json
{
    "code": 200,
    "message": "success"
}
```

### 根据ID查询菜单
- **接口地址**: `/api/menus/{id}`
- **请求方式**: GET
- **请求参数**: 无
- **返回值**: 同创建菜单

### 查询系统下的所有菜单
- **接口地址**: `/api/menus/system/{systemId}`
- **请求方式**: GET
- **请求参数**: 无
- **返回值**:
```json
{
    "code": 200,
    "message": "success",
    "data": [                // 菜单列表
        {                     // 菜单信息
            "id": "long",
            "menuName": "string",
            "menuCode": "string",
            "systemId": "long",
            "parentId": "long",
            "path": "string",
            "component": "string",
            "sort": "integer"
        }
    ]
}
```

### 查询子菜单
- **接口地址**: `/api/menus/parent/{parentId}`
- **请求方式**: GET
- **请求参数**: 无
- **返回值**: 同查询系统下的所有菜单

### 查询所有菜单
- **接口地址**: `/api/menus`
- **请求方式**: GET
- **请求参数**: 无
- **返回值**: 同查询系统下的所有菜单

### 分页查询菜单
- **接口地址**: `/api/menus/page`
- **请求方式**: POST
- **请求参数**:
```json
{
    "pageNum": "integer",    // 页码
    "pageSize": "integer",   // 每页数量
    "menuName": "string",    // 菜单名称（可选）
    "menuCode": "string",    // 菜单编码（可选）
    "systemId": "long"      // 系统ID（可选）
}
```
- **返回值**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "total": "long",        // 总记录数
        "list": [               // 数据列表
            {                    // 菜单信息
                "id": "long",
                "menuName": "string",
                "menuCode": "string",
                "systemId": "long",
                "parentId": "long",
                "path": "string",
                "component": "string",
                "sort": "integer"
            }
        ]
    }
}
```

## 系统管理

### 创建系统
- **接口地址**: `/api/systems`
- **请求方式**: POST
- **请求参数**:
```json
{
    "systemName": "string",   // 系统名称
    "systemCode": "string",   // 系统编码
    "description": "string"   // 系统描述
}
```
- **返回值**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": "long",           // 系统ID
        "systemName": "string",  // 系统名称
        "systemCode": "string",  // 系统编码
        "description": "string"  // 系统描述
    }
}
```

### 更新系统
- **接口地址**: `/api/systems/{id}`
- **请求方式**: PUT
- **请求参数**:
```json
{
    "systemName": "string",   // 系统名称
    "description": "string"   // 系统描述
}
```
- **返回值**: 同创建系统

### 删除系统
- **接口地址**: `/api/systems/{id}`
- **请求方式**: DELETE
- **请求参数**: 无
- **返回值**:
```json
{
    "code": 200,
    "message": "success"
}
```

### 根据ID查询系统
- **接口地址**: `/api/systems/{id}`
- **请求方式**: GET
- **请求参数**: 无
- **返回值**: 同创建系统

### 根据系统编码查询系统
- **接口地址**: `/api/systems/code/{systemCode}`
- **请求方式**: GET
- **请求参数**: 无
- **返回值**: 同创建系统

### 查询所有系统
- **接口地址**: `/api/systems`
- **请求方式**: GET
- **请求参数**: 无
- **返回值**:
```json
{
    "code": 200,
    "message": "success",
    "data": [                // 系统列表
        {                     // 系统信息
            "id": "long",
            "systemName": "string",
            "systemCode": "string",
            "description": "string"
        }
    ]
}
```

### 分页查询系统
- **接口地址**: `/api/systems/page`
- **请求方式**: POST
- **请求参数**:
```json
{
    "pageNum": "integer",     // 页码
    "pageSize": "integer",    // 每页数量
    "systemName": "string",   // 系统名称（可选）
    "systemCode": "string"    // 系统编码（可选）
}
```
- **返回值**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "total": "long",         // 总记录数
        "list": [                // 数据列表
            {                     // 系统信息
                "id": "long",
                "systemName": "string",
                "systemCode": "string",
                "description": "string"
            }
        ]
    }
}
```

## 操作日志管理

### 创建操作日志
- **接口地址**: `/api/operation-logs`
- **请求方式**: POST
- **请求参数**:
```json
{
    "userId": "long",        // 操作用户ID
    "systemId": "long",     // 所属系统ID
    "operation": "string",  // 操作类型
    "description": "string", // 操作描述
    "ip": "string"         // 操作IP地址
}
```
- **返回值**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": "long",           // 日志ID
        "userId": "long",       // 操作用户ID
        "systemId": "long",    // 所属系统ID
        "operation": "string", // 操作类型
        "description": "string",// 操作描述
        "ip": "string",        // 操作IP地址
        "createTime": "string"  // 创建时间
    }
}
```

### 分页查询操作日志
- **接口地址**: `/api/operation-logs/page`
- **请求方式**: POST
- **请求参数**:
```json
{
    "pageNum": "integer",     // 页码
    "pageSize": "integer",    // 每页数量
    "userId": "long",        // 用户ID（可选）
    "systemId": "long",     // 系统ID（可选）
    "operation": "string",  // 操作类型（可选）
    "startTime": "string",  // 开始时间（可选）
    "endTime": "string"     // 结束时间（可选）
}
```
- **返回值**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "total": "long",         // 总记录数
        "list": [                // 数据列表
            {                     // 日志信息
                "id": "long",
                "userId": "long",
                "systemId": "long",
                "operation": "string",
                "description": "string",
                "ip": "string",
                "createTime": "string"
            }
        ]
    }
}
```

## 认证管理

### 用户登录
- **接口地址**: `/api/auth/login`
- **请求方式**: POST
- **请求参数**:
```json
{
    "username": "string",    // 用户名
    "password": "string",    // 密码
    "systemCode": "string"   // 系统编码
}
```
- **返回值**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "token": "string",       // 访问令牌
        "refreshToken": "string", // 刷新令牌
        "expireTime": "long"     // 过期时间
    }
}
```

### 刷新令牌
- **接口地址**: `/api/auth/refresh-token`
- **请求方式**: POST
- **请求参数**:
```json
{
    "refreshToken": "string"   // 刷新令牌
}
```
- **返回值**: 同登录接口

### 退出登录
- **接口地址**: `/api/auth/logout`
- **请求方式**: POST
- **请求参数**: 无
- **返回值**:
```json
{
    "code": 200,
    "message": "success"
}