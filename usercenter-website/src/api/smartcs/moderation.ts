import request from '../config'

// 审核相关的类型定义
export interface ModerationCategory {
  id?: number
  parentId?: number
  name: string
  code: string
  description?: string
  severityLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  actionType: 'WARN' | 'REVIEW' | 'BLOCK' | 'ESCALATE'
  isActive: boolean
  sortOrder: number
  children?: ModerationCategory[]
  createdBy?: string
  updatedBy?: string
  createdAt?: number
  updatedAt?: number
}

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

export interface ModerationRecord {
  id?: number
  contentHash: string
  originalContent: string
  contentType: 'MESSAGE' | 'KNOWLEDGE' | 'DOCUMENT' | 'FAQ' | 'RAG_QUERY'
  sourceId?: string
  sourceType: 'CHAT' | 'KNOWLEDGE_BASE' | 'RAG_QUERY' | 'FILE_UPLOAD'
  userId?: string
  sessionId?: string
  moderationResult: 'APPROVED' | 'REJECTED' | 'NEEDS_REVIEW' | 'PENDING'
  riskLevel?: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  confidenceScore?: number
  isBlocked: boolean
  violationCategories?: ViolationCategory[]
  keywordMatches?: string[]
  moderationMethods?: string
  aiModelUsed?: string
  processingTimeMs?: number
  manualReviewStatus?: string
  manualReviewerId?: string
  manualReviewNotes?: string
  manualReviewedAt?: number
  actionTaken?: 'WARN' | 'REVIEW' | 'BLOCK' | 'ESCALATE'
  clientIp?: string
  requestId?: string
  createdAt?: number
  updatedAt?: number
}

export interface ViolationCategory {
  categoryId: number
  categoryName: string
  categoryCode: string
  confidence: number
  triggerRule?: string
}

export interface KeywordRule {
  id?: number
  ruleName: string
  keyword: string
  categoryId: number
  ruleType: 'EXACT' | 'FUZZY' | 'REGEX' | 'SUBSTRING'
  matchMode: 'FULL' | 'PARTIAL' | 'WORD_BOUNDARY'
  caseSensitive: boolean
  severityWeight: number
  similarityThreshold?: number
  contextWindow?: number
  isActive: boolean
  priority: number
  actionOverride?: 'WARN' | 'REVIEW' | 'BLOCK' | 'ESCALATE'
  hitCount: number
  lastHitAt?: number
  description?: string
  source: 'SYSTEM' | 'MANUAL' | 'IMPORT' | 'AI_GENERATED'
  language: string
  tags?: string
  effectiveFrom?: number
  effectiveUntil?: number
  createdBy?: string
  createdAt?: number
}

export interface ModerationConfig {
  id?: number
  configKey: string
  configName: string
  configValue: string
  configType: 'BOOLEAN' | 'INTEGER' | 'STRING' | 'JSON' | 'DECIMAL'
  description?: string
  category: 'GENERAL' | 'AI_MODEL' | 'KEYWORD_FILTER' | 'THRESHOLD' | 'CACHE'
  isActive: boolean
  isSystem: boolean
  validationRule?: string
  defaultValue?: string
}

export interface ModerationStatistics {
  totalRecords: number
  approvedCount: number
  rejectedCount: number
  pendingCount: number
  blockedCount: number
  averageProcessingTime: number
  todayRecords: number
  riskLevelDistribution: {
    lowCount: number
    mediumCount: number
    highCount: number
    criticalCount: number
  }
}

export interface ViolationTrend {
  timestamp: number
  date: string
  violationCount: number
  topViolationType?: string
}

// 查询参数接口
export interface CategoryQuery {
  parentId?: number
  isActive?: boolean
  severityLevel?: string
}

export interface RecordQuery {
  contentType?: string
  sourceType?: string
  moderationResult?: string
  riskLevel?: string
  userId?: string
  sessionId?: string
  isBlocked?: boolean
  startTime?: number
  endTime?: number
  pageNumber?: number
  pageSize?: number
  sortBy?: string
  sortOrder?: 'ASC' | 'DESC'
}

export interface RuleQuery {
  categoryId?: number
  ruleType?: string
  language?: string
  isActive?: boolean
  pageNumber?: number
  pageSize?: number
}

// API接口定义
export const moderationApi = {
  // ================ 违规分类管理 ================
  
  // 获取分类树
  getCategoryTree: () => {
    return request.get<ModerationCategory[]>('/api/admin/moderation/categories/tree')
  },

  // 获取顶级分类
  getTopCategories: () => {
    return request.get<ModerationCategory[]>('/api/admin/moderation/categories/top')
  },

  // 获取子分类
  getSubCategories: (parentId: number) => {
    return request.get<ModerationCategory[]>(`/api/admin/moderation/categories/${parentId}/children`)
  },

  // 创建分类
  createCategory: (data: Partial<ModerationCategory>) => {
    return request.post<ModerationCategory>('/api/admin/moderation/categories', data)
  },

  // 更新分类
  updateCategory: (id: number, data: Partial<ModerationCategory>) => {
    return request.put<ModerationCategory>(`/api/admin/moderation/categories/${id}`, data)
  },

  // 删除分类
  deleteCategory: (id: number) => {
    return request.delete(`/api/admin/moderation/categories/${id}`)
  },

  // 启用/禁用分类
  toggleCategoryStatus: (id: number, isActive: boolean) => {
    return request.patch(`/api/admin/moderation/categories/${id}/status`, { isActive })
  },

  // ================ 审核记录管理 ================

  // 分页查询审核记录
  getRecords: (query: RecordQuery = {}) => {
    return request.get('/api/admin/moderation/records', { params: query })
  },

  // 获取记录详情
  getRecordDetail: (id: number) => {
    return request.get<ModerationRecord>(`/api/admin/moderation/records/${id}`)
  },

  // 人工审核处理
  manualReview: (id: number, data: {
    result: 'APPROVED' | 'REJECTED'
    action: 'WARN' | 'REVIEW' | 'BLOCK' | 'ESCALATE'
    notes?: string
  }) => {
    return request.post(`/api/admin/moderation/records/${id}/review`, data)
  },

  // 批量处理记录
  batchProcess: (ids: number[], action: string) => {
    return request.post('/api/admin/moderation/records/batch', { ids, action })
  },

  // 获取需要人工审核的记录
  getPendingReviews: (query: RecordQuery = {}) => {
    return request.get('/api/admin/moderation/records/pending', { params: query })
  },

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
    return request.post<ModerationTestResult>('/api/admin/moderation/content/ai', data)
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
    return request.post<ModerationTestResult>('/api/admin/moderation/content/ai/quick', data)
  }
}

export default moderationApi