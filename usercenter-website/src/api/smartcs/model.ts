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
  ACTIVE = 'active',
  INACTIVE = 'inactive',
  DISABLED = 'disabled'
}

// 模型来源枚举
export enum FetchFrom {
  PREDEFINED_MODEL = 'predefined-model',
  CUSTOM_MODEL = 'custom-model'
}

export const modelApi = {
  // 创建模型实例
  create: (data: CreateModelRequest) => 
    request.post<ApiResponse<Model>>('/smartcs/api/admin/model', data),

  // 分页查询模型实例列表
  getPage: (params: ModelPageQuery) => 
    request.get<PageResponse<Model>>('/smartcs/api/admin/model/page', { params }),

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