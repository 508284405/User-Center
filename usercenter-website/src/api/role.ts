import request from './config';

// 角色接口返回类型
interface RoleResponse {
  id: number;
  roleName: string;
  roleCode: string;
  systemId: number;
  level: number;
}

// 创建角色参数类型
interface CreateRoleParams {
  roleName: string;
  roleCode: string;
  systemId: number;
  level: number;
}

// 更新角色参数类型
interface UpdateRoleParams {
  roleName: string;
  level: number;
}

// 分页查询参数类型
interface RolePageParams {
  pageNum: number;
  pageSize: number;
  roleName?: string;
  roleCode?: string;
  systemId?: number;
}

// 分页查询返回类型
interface RolePageResponse {
  total: number;
  data: RoleResponse[];
}

// 角色管理相关接口
export const roleApi = {
  // 创建角色
  create: (params: CreateRoleParams) => {
    return request.post<any, RoleResponse>('/roles', params);
  },

  // 更新角色
  update: (id: number, params: UpdateRoleParams) => {
    return request.put<any, RoleResponse>(`/roles/${id}`, params);
  },

  // 删除角色
  delete: (id: number) => {
    return request.delete<any, void>(`/roles/${id}`);
  },

  // 根据ID查询角色
  getById: (id: number) => {
    return request.get<any, RoleResponse>(`/roles/${id}`);
  },

  // 根据角色名称查询角色
  getByName: (roleName: string) => {
    return request.get<any, RoleResponse>(`/roles/name/${roleName}`);
  },

  // 查询系统下的所有角色
  getBySystem: (systemId: number) => {
    return request.get<any, RoleResponse[]>(`/roles/system/${systemId}`);
  },

  // 查询指定级别的角色
  getByLevel: (level: number) => {
    return request.get<any, RoleResponse[]>(`/roles/level/${level}`);
  },

  // 查询所有角色
  getAll: () => {
    return request.get<any, RoleResponse[]>('/roles');
  },

  // 分页查询角色
  page: (params: RolePageParams) => {
    return request.post<any, RolePageResponse>('/roles/page', params);
  }
};