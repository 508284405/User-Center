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
  MODELSCOPE = 'MODELSCOPE',
  OLLAMA = 'OLLAMA'
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
  { label: '摩登社区', value: ProviderType.MODELSCOPE },
  { label: 'Ollama', value: ProviderType.OLLAMA }
]

// 检查提供商是否需要API Key
export const requiresApiKey = (providerType: string): boolean => {
  return providerType !== ProviderType.OLLAMA
}

// 获取提供商配置提示信息
export const getProviderConfigHint = (providerType: string): string => {
  switch (providerType) {
    case ProviderType.OLLAMA:
      return '无需API Key，确保Ollama服务已启动并可访问'
    default:
      return '请提供有效的API Key'
  }
}

// 获取提供商类型显示名称
export const getProviderTypeLabel = (providerType: string): string => {
  const option = providerTypeOptions.find(opt => opt.value === providerType)
  return option ? option.label : providerType
}

export interface Provider {
  id?: number
  providerType: string
  iconSmall?: string
  iconLarge?: string
  hasApiKey?: boolean
  apiKeyMasked?: string
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

export interface UpdateProviderRequest {
  id: number
  providerType: string
  iconSmall?: string
  iconLarge?: string
  apiKey?: string  // 可选，为空表示不修改
  endpoint?: string
  supportedModelTypes?: string
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

  // 获取所有提供商（用于下拉选择和名称映射）
  getAll: () => 
    request.get<PageResponse<Provider>>('/smartcs/api/admin/model/provider/page', { 
      params: { pageSize: 100 } 
    }).then(res => res as unknown as PageResponse<Provider>),

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