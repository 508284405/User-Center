import request from '../config'

// 审核相关的类型定义
export interface ModerationTestResult {
  content: string
  result: 'APPROVED' | 'REJECTED' | 'NEEDS_REVIEW' | 'PENDING'
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  confidence: number
  violationCategories?: string[]
  matchedKeywords?: string[]
  reason?: string
  suggestedAction?: string
  processingTime: number
  debugInfo?: Record<string, any>
}

// 策略相关类型定义
export interface ModerationPolicy {
  id?: number
  name: string
  code: string
  description?: string
  scenario: string
  policyType: string
  defaultRiskLevel?: string
  defaultAction?: string
  isActive?: boolean
  priority?: number
  configParams?: Record<string, any>
  templateId?: number
  template?: ModerationPolicyTemplate
  dimensions?: ModerationDimension[]
  createdBy?: string
  updatedBy?: string
  createdAt?: number
  updatedAt?: number
}

export interface ModerationDimension {
  id?: number
  name: string
  code: string
  description?: string
  checkGuideline?: string
  severityLevel?: string
  actionType?: string
  confidenceThreshold?: number
  isActive?: boolean
  sortOrder?: number
  category?: string
  categoryId?: number
  configParams?: Record<string, any>
  createdBy?: string
  updatedBy?: string
  createdAt?: number
  updatedAt?: number
}

export interface ModerationPolicyTemplate {
  id?: number
  name: string
  code: string
  description?: string
  templateType: string
  promptTemplate: string
  dimensionTemplate?: string
  responseTemplate?: string
  language?: string
  variables?: Record<string, any>
  defaultValues?: Record<string, any>
  isActive?: boolean
  version?: string
  createdBy?: string
  updatedBy?: string
  createdAt?: number
  updatedAt?: number
}

export interface PolicyDimensionConfig {
  dimensionId: number
  isActive: boolean
  weight?: number
  customThreshold?: number
}

export interface ModerationPolicyPageQuery {
  name?: string
  code?: string
  scenario?: string
  policyType?: string
  isActive?: boolean
  templateId?: number
  pageNum?: number
  pageSize?: number
  sortBy?: string
  sortOrder?: string
}

// 审核分类相关类型定义
export interface ModerationCategory {
  id?: number
  parentId?: number
  name: string
  code: string
  description?: string
  severityLevel: string
  actionType: string
  isActive?: boolean
  sortOrder?: number
  children?: ModerationCategory[]
  createdBy?: string
  updatedBy?: string
  createdAt?: number
  updatedAt?: number
}

export interface ModerationCategoryCreateCmd {
  name: string
  code: string
  description?: string
  parentId?: number
  severityLevel: string
  actionType: string
  sortOrder?: number
  isActive?: boolean
}

export interface ModerationCategoryUpdateCmd {
  id: number
  name: string
  description?: string
  severityLevel: string
  actionType: string
  sortOrder?: number
  isActive?: boolean
}

export interface ModerationCategoryPageQuery {
  name?: string
  code?: string
  parentId?: number
  severityLevel?: string
  actionType?: string
  isActive?: boolean
  pageIndex?: number
  pageSize?: number
}

// API接口定义
export const moderationApi = {
  // ================ AI模型审核接口 ================

  // 使用指定AI模型进行内容审核
  moderateContentWithModel: (data: {
    content: string
    modelId: number
    contentType?: string
    sourceType?: string
    sourceId?: string
    userId?: string
    sessionId?: string
  }) => {
    return request.post<ModerationTestResult>('/smartcs/api/admin/moderation/content/ai', data)
  },

  // 使用指定AI模型进行快速内容预检
  quickModerateWithModel: (data: {
    content: string
    modelId: number
    contentType?: string
    sourceType?: string
    sourceId?: string
    userId?: string
    sessionId?: string
  }) => {
    return request.post<ModerationTestResult>('/smartcs/api/admin/moderation/content/ai/quick', data)
  },

  // ================ 策略管理接口 ================

  // 创建策略
  createPolicy: (data: Omit<ModerationPolicy, 'id'>) => {
    return request.post<{ id: number }>('/smartcs/api/admin/moderation/policies', data)
  },

  // 更新策略
  updatePolicy: (id: number, data: Partial<ModerationPolicy>) => {
    return request.put(`/smartcs/api/admin/moderation/policies/${id}`, data)
  },

  // 启用策略
  enablePolicy: (id: number) => {
    return request.post(`/smartcs/api/admin/moderation/policies/${id}/enable`)
  },

  // 禁用策略
  disablePolicy: (id: number) => {
    return request.post(`/smartcs/api/admin/moderation/policies/${id}/disable`)
  },

  // 删除策略
  deletePolicy: (id: number) => {
    return request.delete(`/smartcs/api/admin/moderation/policies/${id}`)
  },

  // 获取策略详情
  getPolicyById: (id: number) => {
    return request.get<ModerationPolicy>(`/smartcs/api/admin/moderation/policies/${id}`)
  },

  // 根据编码获取策略
  getPolicyByCode: (code: string) => {
    return request.get<ModerationPolicy>(`/smartcs/api/admin/moderation/policies/code/${code}`)
  },

  // 分页查询策略列表
  queryPolicies: (params: ModerationPolicyPageQuery) => {
    return request.get<{
      data: ModerationPolicy[]
      totalCount: number
      pageSize: number
      pageIndex: number
      success: boolean
    }>('/smartcs/api/admin/moderation/policies', { params })
  },

  // 查询场景策略
  getPoliciesByScenario: (scenario: string) => {
    return request.get<ModerationPolicy[]>(`/smartcs/api/admin/moderation/policies/scenario/${scenario}`)
  },

  // 获取策略维度
  getPolicyDimensions: (id: number) => {
    return request.get<ModerationDimension[]>(`/smartcs/api/admin/moderation/policies/${id}/dimensions`)
  },

  // 配置策略维度
  configurePolicyDimensions: (id: number, configs: PolicyDimensionConfig[]) => {
    return request.post(`/smartcs/api/admin/moderation/policies/${id}/dimensions`, configs)
  },

  // 获取所有维度
  getAllActiveDimensions: () => {
    return request.get<ModerationDimension[]>('/smartcs/api/admin/moderation/policies/dimensions')
  },

  // 获取所有模板
  getAllActiveTemplates: () => {
    return request.get<ModerationPolicyTemplate[]>('/smartcs/api/admin/moderation/policies/templates')
  },

  // ================ 审核分类管理接口 ================

  // 创建审核分类
  createCategory: (data: ModerationCategoryCreateCmd) => {
    return request.post<number>('/smartcs/api/admin/moderation/categories', data)
  },

  // 更新审核分类
  updateCategory: (id: number, data: ModerationCategoryUpdateCmd) => {
    return request.put(`/smartcs/api/admin/moderation/categories/${id}`, { ...data, id })
  },

  // 删除审核分类
  deleteCategory: (id: number) => {
    return request.delete(`/smartcs/api/admin/moderation/categories/${id}`)
  },

  // 根据ID查询审核分类
  getCategoryById: (id: number) => {
    return request.get<ModerationCategory>(`/smartcs/api/admin/moderation/categories/${id}`)
  },

  // 分页查询审核分类
  listCategories: (params: ModerationCategoryPageQuery) => {
    return request.get<{
      data: ModerationCategory[]
      totalCount: number
      pageSize: number
      pageIndex: number
      success: boolean
    }>('/smartcs/api/admin/moderation/categories', { params })
  },

  // 获取审核分类树形结构
  getCategoryTree: () => {
    return request.get<ModerationCategory[]>('/smartcs/api/admin/moderation/categories/tree')
  },

  // ================ 模板管理接口 ================

  // 创建模板
  createTemplate: (data: Omit<ModerationPolicyTemplate, 'id'>) => {
    return request.post<number>('/smartcs/api/admin/moderation/templates', data)
  },

  // 更新模板
  updateTemplate: (id: number, data: Partial<ModerationPolicyTemplate>) => {
    return request.put(`/smartcs/api/admin/moderation/templates/${id}`, { ...data, id })
  },

  // 删除模板
  deleteTemplate: (id: number) => {
    return request.delete(`/smartcs/api/admin/moderation/templates/${id}`)
  },

  // 获取模板详情
  getTemplateById: (id: number) => {
    return request.get<ModerationPolicyTemplate>(`/smartcs/api/admin/moderation/templates/${id}`)
  }
}

export default moderationApi