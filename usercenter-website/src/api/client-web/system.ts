import request from '../config';

// 系统接口返回类型
interface SystemResponse {
  id: number;
  systemName: string;
  systemCode: string;
  description: string;
}

// 创建系统参数类型
interface CreateSystemParams {
  systemName: string;
  systemCode: string;
  description: string;
}

// 更新系统参数类型
interface UpdateSystemParams {
  systemName: string;
  description: string;
}

// 分页查询参数类型
interface SystemPageParams {
  pageNum: number;
  pageSize: number;
  systemName?: string;
  systemCode?: string;
}

// 分页查询返回类型
interface SystemPageResponse {
  total: number;
  data: SystemResponse[];
}

// 系统管理相关接口
export const systemApi = {
  // 创建系统
  create: (params: CreateSystemParams) => {
    return request.post<any, SystemResponse>('/client-web/api/systems', params);
  },

  // 更新系统
  update: (id: number, params: UpdateSystemParams) => {
    return request.put<any, SystemResponse>(`/client-web/api/systems/${id}`, params);
  },

  // 删除系统
  delete: (id: number) => {
    return request.delete<any, void>(`/client-web/api/systems/${id}`);
  },

  // 根据ID查询系统
  getById: (id: number) => {
    return request.get<any, SystemResponse>(`/client-web/api/systems/${id}`);
  },

  // 根据系统编码查询系统
  getByCode: (systemCode: string) => {
    return request.get<any, SystemResponse>(`/client-web/api/systems/code/${systemCode}`);
  },

  // 查询所有系统
  getAll: () => {
    return request.get<any, SystemResponse[]>('/client-web/api/systems');
  },

  // 分页查询系统
  page: (params: SystemPageParams) => {
    return request.post<any, SystemPageResponse>('/client-web/api/systems/page', params);
  }
};