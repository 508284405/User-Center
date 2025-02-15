import request from './config';

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
  total: number;
  list: UserResponse[];
}

// 用户管理相关接口
export const userApi = {
  // 创建用户
  create: (params: CreateUserParams) => {
    return request.post<any, UserResponse>('/users', params);
  },

  // 更新用户信息
  update: (id: number, params: UpdateUserParams) => {
    return request.put<any, UserResponse>(`/users/${id}`, params);
  },

  // 锁定用户
  lock: (id: number) => {
    return request.put<any, void>(`/users/${id}/lock`);
  },

  // 解锁用户
  unlock: (id: number) => {
    return request.put<any, void>(`/users/${id}/unlock`);
  },

  // 根据用户名查询用户
  getByUsername: (username: string) => {
    return request.get<any, UserResponse>(`/users/username/${username}`);
  },

  // 根据邮箱查询用户
  getByEmail: (email: string) => {
    return request.get<any, UserResponse>(`/users/email/${email}`);
  },

  // 根据手机号查询用户
  getByPhone: (phone: string) => {
    return request.get<any, UserResponse>(`/users/phone/${phone}`);
  },

  // 分页查询用户列表
  page: (params: UserPageParams) => {
    return request.post<any, UserPageResponse>('/users/page', params);
  }
};