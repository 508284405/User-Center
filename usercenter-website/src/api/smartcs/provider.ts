import request from '../config'

export interface Provider {
  id?: number
  providerKey: string
  label: string
  iconSmall?: string
  iconLarge?: string
  apiKey: string
  endpoint?: string
  supportedModelTypes?: string
  createdBy?: string
  updatedBy?: string
  createdAt?: number
  updatedAt?: number
}

export interface CreateProviderRequest {
  providerKey: string
  label: string
  iconSmall?: string
  iconLarge?: string
  apiKey: string
  endpoint?: string
  supportedModelTypes?: string
}

export interface UpdateProviderRequest extends CreateProviderRequest {
  id: number
}

export interface PageQuery {
  pageSize?: number
  pageIndex?: number
  orderBy?: string
  orderDirection?: string
  groupBy?: string
  needTotalCount?: boolean
  label?: string
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

export const providerApi = {
  // 创建模型提供商
  create: (data: CreateProviderRequest) => 
    request.post<ApiResponse<Provider>>('/smartcs/api/admin/model/provider', data),

  // 分页查询模型提供商列表
  getPage: (params: PageQuery) => 
    request.get<PageResponse<Provider>>('/smartcs/api/admin/model/provider/page', { params }),

  // 获取模型提供商详情
  getDetail: (id: number) => 
    request.get<ApiResponse<Provider>>(`/smartcs/api/admin/model/provider/${id}`),

  // 更新模型提供商
  update: (data: UpdateProviderRequest) => 
    request.put<ApiResponse<Provider>>('/smartcs/api/admin/model/provider', data),

  // 删除模型提供商
  delete: (id: number) => 
    request.delete<ApiResponse<boolean>>(`/smartcs/api/admin/model/provider/${id}`)
}