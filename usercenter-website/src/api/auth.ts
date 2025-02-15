import request from './config';

// 登录参数类型
interface LoginParams {
  username: string;
  password: string;
  systemCode: string;
}

// 登录返回类型
interface LoginResponse {
  token: string;
  refreshToken: string;
  expireTime: number;
}

// 刷新令牌参数类型
interface RefreshTokenParams {
  refreshToken: string;
}

// 操作日志参数类型
interface OperationLogParams {
  userId: number;
  systemId: number;
  operation: string;
  description: string;
  ip: string;
}

// 操作日志返回类型
interface OperationLogResponse {
  id: number;
  userId: number;
  systemId: number;
  operation: string;
  description: string;
  ip: string;
  createTime: string;
}

// 日志分页查询参数类型
interface LogPageParams {
  pageNum: number;
  pageSize: number;
  userId?: number;
  systemId?: number;
  operation?: string;
  startTime?: string;
  endTime?: string;
}

// 日志分页查询返回类型
interface LogPageResponse {
  total: number;
  list: OperationLogResponse[];
}

// 认证相关接口
export const authApi = {
  // 用户登录
  login: (params: LoginParams) => {
    return request.post<any, LoginResponse>('/auth/login', params);
  },

  // 刷新令牌
  refreshToken: (params: RefreshTokenParams) => {
    return request.post<any, LoginResponse>('/auth/refresh-token', params);
  },

  // 退出登录
  logout: () => {
    return request.post<any, void>('/auth/logout');
  }
};

// 操作日志相关接口
export const logApi = {
  // 创建操作日志
  create: (params: OperationLogParams) => {
    return request.post<any, OperationLogResponse>('/operation-logs', params);
  },

  // 分页查询操作日志
  page: (params: LogPageParams) => {
    return request.post<any, LogPageResponse>('/operation-logs/page', params);
  }
};