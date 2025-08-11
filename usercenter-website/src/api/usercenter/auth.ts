import request from '../config';

// 登录参数类型
interface LoginParams {
  username: string;
  password: string;
}

// 注册参数类型
interface RegisterParams {
  username: string;
  email: string;
  password: string;
}

// 注册返回数据类型
interface RegisterResponseData {
  id: number;
  username: string;
  email: string;
  createdAt: string;
}

// API 通用返回格式
interface ApiResponse<T> {
  success: boolean;
  errCode: string;
  errMessage: string;
  data: T;
}

// 用户信息类型
interface UserInfo {
  id: number;
  username: string;
  email: string;
  phone: string;
  nickname: string;
  avatar: string;
  status: number;
  roles: string[];
  permissions: string[];
}

// Token信息类型
interface TokenInfo {
  accessToken: string;
  refreshToken: string;
  accessTokenExpiresAt: number;
  refreshTokenExpiresAt: number;
  tokenType: string;
}

// 登录返回数据类型
interface LoginResponseData {
  user: UserInfo;
  token: TokenInfo;
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

// Google登录参数类型
interface GoogleLoginParams {
  authCode: string;
}

// Google登录URL返回类型
interface GoogleLoginUrlResponse {
  mapKey1: string; // 实际上是登录URL
  mapKey2: string;
}

// 认证相关接口
export const authApi = {
  // 用户登录
  login: (params: LoginParams) => {
    return request.post<any, ApiResponse<LoginResponseData>>('/user-center/api/users/login', params);
  },

  // 获取Google登录URL
  getGoogleLoginUrl: (redirectUri?: string) => {
    return request.get<any, ApiResponse<GoogleLoginUrlResponse>>(`/user-center/api/users/google-login-url${redirectUri ? `?redirectUri=${redirectUri}` : ''}`);
  },

  // Google登录
  googleLogin: (params: GoogleLoginParams) => {
    return request.post<any, ApiResponse<LoginResponseData>>('/user-center/api/users/google-login', params);
  },

  // 用户注册
  register: (params: RegisterParams) => {
    return request.post<any, ApiResponse<RegisterResponseData>>('/user-center/api/users/register', params);
  },

  // 刷新令牌
  refreshToken: (params: RefreshTokenParams) => {
    return request.post<any, ApiResponse<TokenInfo>>('/user-center/api/auth/refresh-token', params);
  },

  // 退出登录
  logout: () => {
    return request.post<any, void>('/user-center/api/tokens/logout');
  }
};

// 操作日志相关接口
export const logApi = {
  // 创建操作日志
  create: (params: OperationLogParams) => {
    return request.post<any, OperationLogResponse>('/user-center/api/operation-logs', params);
  },

  // 分页查询操作日志
  page: (params: LogPageParams) => {
    return request.post<any, LogPageResponse>('/user-center/api/operation-logs/page', params);
  }
};