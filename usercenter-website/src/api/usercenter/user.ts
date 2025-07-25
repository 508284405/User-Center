import request from '../config';

// 菜单类型
interface Menu {
  id: number;
  menuId: string;
  menuName: string;
  menuCode: string;
  path: string;
  component: string;
  type: number;
  parentId: number;
  sort: number;
  icon: string;
  createTime: string;
  updateTime: string;
  permissions: string[];
  children: Menu[];
}

// 角色类型
interface Role {
  id: number;
  roleName: string;
  roleCode: string;
  description: string;
  status: number;
  createTime: string;
  updateTime: string;
  menus: Menu[];
}

// 当前用户信息返回类型
interface CurrentUserInfo {
  id: number;
  username: string;
  email: string;
  phone: string;
  nickname: string;
  avatar: string;
  status: number;
  roles: Role[];
}

// API响应类型
interface ApiResponse<T> {
  success: boolean;
  errCode: string;
  errMessage: string;
  data: T;
}

// 用户接口返回类型
interface UserResponse {
  id: number;
  username: string;
  email: string;
  phone: string;
}

// 创建用户参数类型
interface CreateUserParams {
  username: string;
  password: string;
  email: string;
  phone: string;
}

// 更新用户参数类型
interface UpdateUserParams {
  email: string;
  phone: string;
}

// 分页查询参数类型
interface UserPageParams {
  pageNum: number;
  pageSize: number;
  username?: string;
  email?: string;
  phone?: string;
}

// 分页查询返回类型
interface UserPageResponse {
  totalCount: number;
  data: UserResponse[];
}

// 用户管理相关接口
export const userApi = {
  // 获取当前登录用户信息
  getCurrentUserInfo: () => {
    return request.get<any, ApiResponse<CurrentUserInfo>>('/user-center/api/users/current/info');
  },

  // 创建用户
  create: (params: CreateUserParams) => {
    return request.post<any, UserResponse>('/user-center/api/users', params);
  },

  // 更新用户信息
  update: (id: number, params: UpdateUserParams) => {
    return request.put<any, UserResponse>(`/user-center/api/users/${id}`, params);
  },

  // 锁定用户
  lock: (id: number) => {
    return request.put<any, void>(`/user-center/api/users/${id}/lock`);
  },

  delete: (id: number) => {
    return request.delete<any, void>(`/user-center/api/users/${id}`);
  },

  // 解锁用户
  unlock: (id: number) => {
    return request.put<any, void>(`/user-center/api/users/${id}/unlock`);
  },

  // 根据用户名查询用户
  getByUsername: (username: string) => {
    return request.get<any, UserResponse>(`/user-center/api/users/username/${username}`);
  },

  // 根据邮箱查询用户
  getByEmail: (email: string) => {
    return request.get<any, UserResponse>(`/user-center/api/users/email/${email}`);
  },

  // 根据手机号查询用户
  getByPhone: (phone: string) => {
    return request.get<any, UserResponse>(`/user-center/api/users/phone/${phone}`);
  },

  // 分页查询用户列表
  page: (params: UserPageParams) => {
    return request.post<any, UserPageResponse>('/user-center/api/users/page', params);
  },
  // 用户ID查询用户名-批量
  getUsernameByIds: (ids: number[]) => {
    return request.post<any, UserResponse[]>(`/user-center/api/users/username/ids`, ids);
  }
};