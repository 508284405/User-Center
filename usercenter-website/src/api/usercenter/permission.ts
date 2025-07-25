import request from '../config';

// 权限相关接口（基于角色和菜单API）
export const permissionApi = {
  // 获取角色拥有的菜单权限
  getRoleMenus: (roleId: number) => {
    return request.get<any, any>(`/usercenter/api/menus/role/${roleId}`);
  },

  // 分配角色的菜单权限
  assignRoleMenus: (roleId: number, menuIds: number[]) => {
    return request.post<any, void>(`/usercenter/api/roles/${roleId}/menus`, { menuIds });
  },

  // 获取用户的角色列表
  getUserRoles: (userId: number) => {
    return request.get<any, any>(`/usercenter/api/roles/user/${userId}`);
  },

  // 分配用户角色
  assignUserRoles: (userId: number, roleIds: number[]) => {
    return request.put<any, void>(`/usercenter/api/roles/user/${userId}`, { roleIds });
  },

  // 获取用户的所有菜单权限
  getUserMenus: () => {
    return request.get<any, any[]>('/usercenter/api/users/current/menus');
  },

  // 验证用户是否拥有指定菜单权限
  checkMenuPermission: (menuCode: string) => {
    return request.get<any, boolean>(`/usercenter/api/users/permissions/check/${menuCode}`);
  }
};
