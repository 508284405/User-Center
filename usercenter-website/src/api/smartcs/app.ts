import request from '../config'

export interface AiAppDTO {
  id?: number
  name: string
  code: string
  description?: string
  type: string
  typeName?: string
  typeDescription?: string
  config?: Record<string, any>
  status?: string
  statusName?: string
  icon?: string
  tags?: string[]
  creatorId?: number
  createdBy?: string
  updatedBy?: string
  createdAt?: number
  updatedAt?: number
  usable?: boolean
  editable?: boolean
}

export interface AiAppCreateRequest {
  name: string
  code: string
  description?: string
  type: string
  config?: Record<string, any>
  icon?: string
  tags?: string[]
}

export interface AiAppUpdateRequest {
  id: number
  name: string
  description?: string
  config?: Record<string, any>
  icon?: string
  tags?: string[]
}

export interface AiAppStatusUpdateRequest {
  id: number
  status: string
}

export interface AiAppListQuery {
  creatorId?: number
  type?: string
  status?: string
  keyword?: string
  pageIndex?: number
  pageSize?: number
}

export interface AppChatRequest {
  appId: number
  modelId: number
  message: string
  variables?: Record<string, any>
  sessionId?: string
  ragConfig?: import('@/types/chat').RagComponentConfig // 添加RAG配置支持
}

export interface AppChatResponse {
  content: string
  sessionId: string
  messageId: string
  timestamp: number
  ragDebugInfo?: import('@/types/chat').RagDebugInfo // 可选的RAG调试信息
}

export interface PageResponse<T> {
  data: T[]
  total: number
  pageSize: number
  pageIndex: number
  totalPages: number
  hasNext: boolean
  hasPrevious: boolean
  success: boolean
  errCode?: string
  errMessage?: string
}

export interface ApiResponse<T = any> {
  data?: T
  success: boolean
  errCode?: string
  errMessage?: string
}

// AI应用类型选项
export const APP_TYPES = [
  {
    value: 'WORKFLOW',
    label: '工作流',
    description: '面向单次运行的任务编排工作流',
    icon: '🔄'
  },
  {
    value: 'CHATFLOW', 
    label: 'Chatflow',
    description: '支持记忆的复杂对话工作流',
    icon: '💬'
  },
  {
    value: 'CHAT_ASSISTANT',
    label: '聊天助手', 
    description: '简单配置的智能助手',
    icon: '🤖'
  },
  {
    value: 'AGENT',
    label: 'Agent',
    description: '具备推理能力和自主工具调用的智能助手',
    icon: '🧠'
  }
]

// AI应用状态选项
export const APP_STATUS = [
  {
    value: 'DRAFT',
    label: '草稿',
    color: '#999'
  },
  {
    value: 'PUBLISHED',
    label: '已发布',
    color: '#52c41a'
  },
  {
    value: 'DISABLED',
    label: '已停用',
    color: '#ff4d4f'
  }
]

/**
 * 创建AI应用
 */
export const createApp = (data: AiAppCreateRequest): Promise<ApiResponse<AiAppDTO>> => {
  return request.post('/smartcs/api/admin/app', data)
}

/**
 * 更新AI应用
 */
export const updateApp = (data: AiAppUpdateRequest): Promise<ApiResponse> => {
  return request.put('/smartcs/api/admin/app', data)
}

/**
 * 更新AI应用状态
 */
export const updateAppStatus = (data: AiAppStatusUpdateRequest): Promise<ApiResponse> => {
  return request.put('/smartcs/api/admin/app/status', data)
}

/**
 * 获取AI应用详情
 */
export const getApp = (id: number): Promise<ApiResponse<AiAppDTO>> => {
  return request.get(`/smartcs/api/admin/app/${id}`)
}

/**
 * 删除AI应用
 */
export const deleteApp = (id: number): Promise<ApiResponse> => {
  return request.delete(`/smartcs/api/admin/app/${id}`)
}

/**
 * 分页查询AI应用列表
 */
export const listApps = (params: AiAppListQuery): Promise<PageResponse<AiAppDTO>> => {
  return request.get('/smartcs/api/admin/app', { params })
}

/**
 * 根据类型获取应用类型信息
 */
export const getAppTypeInfo = (type: string) => {
  return APP_TYPES.find(item => item.value === type)
}

/**
 * 根据状态获取应用状态信息
 */
export const getAppStatusInfo = (status: string) => {
  return APP_STATUS.find(item => item.value === status)
}

/**
 * 应用聊天测试
 */
export const chatWithApp = async (data: AppChatRequest): Promise<ApiResponse<AppChatResponse>> => {
  try {
    // 验证RAG配置（如果提供）
    if (data.ragConfig) {
      const { validateRagConfig } = await import('@/utils/ragConfigValidator');
      const result = validateRagConfig(data.ragConfig);
      
      if (!result.isValid && result.correctedConfig) {
        console.warn('RAG配置验证失败，使用修正后的配置:', result.errors);
        data.ragConfig = result.correctedConfig;
      } else if (!result.isValid) {
        return {
          success: false,
          errCode: 'RAG_CONFIG_ERROR',
          errMessage: `RAG配置验证失败: ${result.errors.map(e => e.message).join(', ')}`
        };
      }
    }

    return await request.post('/smartcs/api/admin/app/chat', data, {
      timeout: 30000 // 30秒超时，RAG处理可能需要更长时间
    });
  } catch (error: any) {
    console.error('聊天请求失败:', error);
    
    return {
      success: false,
      errCode: error.code || 'CHAT_ERROR',
      errMessage: error.message || '聊天请求失败，请稍后重试'
    };
  }
}

// ==================== 新增接口类型定义 ====================

export interface AiAppPromptOptimizeRequest {
  appId: number
  originalPrompt: string
  optimizeGoal?: string
  modelId?: number
}

export interface AiAppPromptOptimizeResponse {
  originalPrompt: string
  optimizedPrompt: string
  optimizeExplanation: string
  modelName: string
  taskId: string
}

export interface AiAppFunctionConfigRequest {
  appId: number
  functionConfig?: Record<string, any>
  conversationOpenerEnabled?: boolean
  nextQuestionSuggestionEnabled?: boolean
  textToSpeechEnabled?: boolean
  speechToTextEnabled?: boolean
  citationEnabled?: boolean
  contentModerationEnabled?: boolean
  standardReplyEnabled?: boolean
}

export interface AiAppFunctionConfigResponse {
  appId: number
  functionConfig: Record<string, any>
  conversationOpenerEnabled: boolean
  nextQuestionSuggestionEnabled: boolean
  textToSpeechEnabled: boolean
  speechToTextEnabled: boolean
  citationEnabled: boolean
  contentModerationEnabled: boolean
  standardReplyEnabled: boolean
}

// ==================== 新增API接口方法 ====================

/**
 * 优化Prompt
 */
export const optimizePrompt = (data: AiAppPromptOptimizeRequest): Promise<ApiResponse<AiAppPromptOptimizeResponse>> => {
  return request.post('/smartcs/api/admin/app/optimize-prompt', data, {
    timeout: 60000 // 设置60秒超时，因为LLM推理可能需要较长时间
  })
}

/**
 * 更新功能配置
 */
export const updateFunctionConfig = (appId: number, data: AiAppFunctionConfigRequest): Promise<ApiResponse> => {
  return request.put(`/smartcs/api/admin/app/${appId}/function-config`, data)
}

/**
 * 获取功能配置
 */
export const getFunctionConfig = (appId: number): Promise<ApiResponse<AiAppFunctionConfigResponse>> => {
  return request.get(`/smartcs/api/admin/app/${appId}/function-config`)
}