import request from '../config'

export interface Model {
  id?: number
  providerId: number
  label: string
  modelType: string[]
  features?: string
  fetchFrom?: string
  modelProperties?: string
  deprecated?: boolean
  status?: string
  loadBalancingEnabled?: boolean
  createdBy?: string
  updatedBy?: string
  createdAt?: number
  updatedAt?: number
}

export interface CreateModelRequest {
  providerId: number
  label: string
  modelType: string[]
  features?: string
  fetchFrom?: string
  modelProperties?: string
  deprecated?: boolean
  status?: string
  loadBalancingEnabled?: boolean
}

export interface UpdateModelRequest extends CreateModelRequest {
  id: number
}

export interface ModelPageQuery {
  pageSize?: number
  pageIndex?: number
  orderBy?: string
  orderDirection?: string
  groupBy?: string
  needTotalCount?: boolean
  providerId?: number
  modelType?: string[]
  status?: string
}

export interface EnableModelRequest {
  id: number
  status: string
}

export interface PageResponse<T> {
  success: boolean
  errCode?: string
  errMessage?: string
  totalCount: number
  pageSize: number
  pageIndex: number
  data: T[]
}

export interface ApiResponse<T> {
  success: boolean
  errCode?: string
  errMessage?: string
  data?: T
}

// 模型类型枚举
export enum ModelType {
  LLM = 'LLM',
  TTS = 'TTS',
  TEXT_EMBEDDING = 'TEXT_EMBEDDING',
  RERANK = 'RERANK',
  SPEECH2TEXT = 'SPEECH2TEXT'
}

// 模型状态枚举
export enum ModelStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  DISABLED = 'DISABLED'
}

// 模型来源枚举
export enum FetchFrom {
  PREDEFINED_MODEL = 'PREDEFINED_MODEL',
  CUSTOM_MODEL = 'CUSTOM_MODEL'
}

export const modelApi = {
  // 创建模型实例
  create: (data: CreateModelRequest) => 
    request.post<ApiResponse<Model>>('/smartcs/api/admin/model', data),

  // 分页查询模型实例列表
  getPage: (params: ModelPageQuery) => {
    // 使用URLSearchParams手动构建查询参数以确保正确的URL编码
    const searchParams = new URLSearchParams();
    
    // 过滤并添加非数组参数
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        if (Array.isArray(value)) {
          // 对数组参数进行特殊处理
          if (key === 'modelType') {
            value.forEach(item => {
              searchParams.append('modelType', item);
            });
          } else {
            // 其他数组参数的通用处理
            value.forEach(item => {
              searchParams.append(key, String(item));
            });
          }
        } else {
          // 非数组参数直接添加
          searchParams.append(key, String(value));
        }
      }
    });
    
    const queryString = searchParams.toString();
    const url = queryString ? `/smartcs/api/admin/model/page?${queryString}` : '/smartcs/api/admin/model/page';
    
    return request.get<PageResponse<Model>>(url);
  },

  // 获取模型实例详情
  getDetail: (id: number) => 
    request.get<ApiResponse<Model>>(`/smartcs/api/admin/model/${id}`),

  // 更新模型实例
  update: (data: UpdateModelRequest) => 
    request.put<ApiResponse<Model>>('/smartcs/api/admin/model', data),

  // 删除模型实例
  delete: (id: number) => 
    request.delete<ApiResponse<boolean>>(`/smartcs/api/admin/model/${id}`),

  // 启用/禁用模型实例
  updateStatus: (id: number, data: EnableModelRequest) => 
    request.patch<ApiResponse<boolean>>(`/smartcs/api/admin/model/${id}/enable`, data)
}