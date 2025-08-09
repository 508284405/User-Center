import request from '../config'

// 模型提供商类型枚举
export enum ProviderType {
  OPENAI = 'OPENAI',
  DEEPSEEK = 'DEEPSEEK',
  CLAUDE = 'CLAUDE',
  GEMINI = 'GEMINI',
  QWEN = 'QWEN',
  BAIDU = 'BAIDU',
  TENCENT = 'TENCENT',
  ZHIPU = 'ZHIPU',
  MODELSCOPE = 'MODELSCOPE'
}

// 提供商类型选项
export const providerTypeOptions = [
  { label: 'OpenAI', value: ProviderType.OPENAI },
  { label: 'DeepSeek', value: ProviderType.DEEPSEEK },
  { label: 'Claude', value: ProviderType.CLAUDE },
  { label: 'Gemini', value: ProviderType.GEMINI },
  { label: '通义千问', value: ProviderType.QWEN },
  { label: '百度文心', value: ProviderType.BAIDU },
  { label: '腾讯混元', value: ProviderType.TENCENT },
  { label: '智谱AI', value: ProviderType.ZHIPU },
  { label: '摩登社区', value: ProviderType.MODELSCOPE }
]

export interface Provider {
  id?: number
  providerType: string
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
  providerType: string
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
  providerType?: string
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
    request.post<ApiResponse<Provider>>('/smartcs/api/admin/model/provider', data).then(res => res as unknown as ApiResponse<Provider>),

  // 分页查询模型提供商列表
  getPage: (params: PageQuery) => 
    request.get<PageResponse<Provider>>('/smartcs/api/admin/model/provider/page', { params }).then(res => res as unknown as PageResponse<Provider>),

  // 获取模型提供商详情
  getDetail: (id: number) => 
    request.get<ApiResponse<Provider>>(`/smartcs/api/admin/model/provider/${id}`).then(res => res as unknown as ApiResponse<Provider>),

  // 更新模型提供商
  update: (data: UpdateProviderRequest) => 
    request.put<ApiResponse<Provider>>('/smartcs/api/admin/model/provider', data).then(res => res as unknown as ApiResponse<Provider>),

  // 删除模型提供商
  delete: (id: number) => 
    request.delete<ApiResponse<boolean>>(`/smartcs/api/admin/model/provider/${id}`).then(res => res as unknown as ApiResponse<boolean>)
}