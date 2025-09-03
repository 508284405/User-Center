import request from './config';
import { BotChatSSERequestWithRag, RagComponentConfig, RagDebugInfo } from '@/types/chat';

// 扩展现有的聊天请求接口以支持RAG配置
export interface EnhancedChatRequest {
  appId: string; // 改为string类型，避免数字精度问题
  modelId: string; // 改为string类型，避免数字精度问题
  message: string;
  variables?: Record<string, any>;
  sessionId?: string;
  ragConfig?: RagComponentConfig; // 添加RAG配置支持
}

// 聊天响应接口（包含调试信息）
export interface EnhancedChatResponse {
  content: string;
  sessionId: string;
  messageId: string;
  timestamp: number;
  ragDebugInfo?: RagDebugInfo; // 可选的RAG调试信息
}

// SSE流式响应数据
export interface ChatStreamChunk {
  type: 'start' | 'content' | 'debug' | 'error' | 'end';
  data?: any;
  content?: string;
  ragDebugInfo?: RagDebugInfo;
  error?: string;
}

// API响应接口
export interface ApiResponse<T = any> {
  data?: T;
  success: boolean;
  errCode?: string;
  errMessage?: string;
}

/**
 * 增强的应用聊天接口 - 支持RAG配置
 */
export const chatWithApp = async (data: EnhancedChatRequest): Promise<ApiResponse<EnhancedChatResponse>> => {
  try {
    // 验证RAG配置
    if (data.ragConfig) {
      const validation = await import('@/utils/ragConfigValidator');
      const result = validation.validateRagConfig(data.ragConfig);
      
      if (!result.isValid && result.correctedConfig) {
        console.warn('RAG配置验证失败，使用修正后的配置:', result.errors);
        data.ragConfig = result.correctedConfig;
      } else if (!result.isValid) {
        throw new Error(`RAG配置验证失败: ${result.errors.map(e => e.message).join(', ')}`);
      }
    }

    // 验证必填参数
    if (!data.appId || !data.modelId || !data.message) {
      throw new Error('缺少必填参数：appId、modelId、message');
    }

    const response = await request.post(
      '/smartcs/api/admin/app/chat',
      data,
      {
        timeout: 30000, // 30秒超时
        headers: {
          'Content-Type': 'application/json',
        }
      }
    ) as ApiResponse<EnhancedChatResponse>;

    return response;
  } catch (error: any) {
    console.error('聊天请求失败:', error);
    
    // 根据错误类型返回不同的错误信息
    let errCode = 'CHAT_ERROR';
    let errMessage = '聊天请求失败，请稍后重试';
    
    if (error.response?.status === 400) {
      errCode = 'VALIDATION_ERROR';
      errMessage = '请求参数错误，请检查输入';
    } else if (error.response?.status === 500) {
      errCode = 'SERVER_ERROR';
      errMessage = '服务器内部错误，请稍后重试';
    } else if (error.code === 'ECONNABORTED') {
      errCode = 'TIMEOUT_ERROR';
      errMessage = '请求超时，请稍后重试';
    }
    
    return {
      success: false,
      errCode: error.code || errCode,
      errMessage: error.message || errMessage
    };
  }
};

/**
 * SSE流式聊天接口 - 支持RAG配置
 */
export const chatWithAppStream = (
  data: EnhancedChatRequest,
  onMessage: (chunk: ChatStreamChunk) => void,
  onError?: (error: Error) => void,
  onComplete?: () => void
): EventSource => {
  // 构建SSE URL
  const url = new URL('/smartcs/api/admin/app/chat/stream', window.location.origin);
  
  // 添加查询参数
  url.searchParams.append('appId', data.appId.toString());
  url.searchParams.append('modelId', data.modelId.toString());
  url.searchParams.append('message', data.message);
  
  if (data.sessionId) {
    url.searchParams.append('sessionId', data.sessionId);
  }
  
  if (data.variables) {
    url.searchParams.append('variables', JSON.stringify(data.variables));
  }
  
  if (data.ragConfig) {
    url.searchParams.append('ragConfig', JSON.stringify(data.ragConfig));
  }

  // 创建SSE连接
  const eventSource = new EventSource(url.toString());

  eventSource.onmessage = (event) => {
    try {
      const chunk: ChatStreamChunk = JSON.parse(event.data);
      onMessage(chunk);
      
      if (chunk.type === 'end') {
        eventSource.close();
        onComplete?.();
      }
    } catch (error) {
      console.error('解析SSE消息失败:', error);
      onError?.(new Error('消息格式错误'));
    }
  };

  eventSource.onerror = (event) => {
    console.error('SSE连接错误:', event);
    eventSource.close();
    onError?.(new Error('连接中断'));
  };

  return eventSource;
};

/**
 * 验证RAG配置接口
 */
export const validateRagConfig = async (config: RagComponentConfig): Promise<ApiResponse<{ isValid: boolean; errors?: string[] }>> => {
  try {
    const validation = await import('@/utils/ragConfigValidator');
    const result = validation.validateRagConfig(config);
    
    return {
      success: true,
      data: {
        isValid: result.isValid,
        errors: result.errors?.map(e => e.message)
      }
    };
  } catch (error: any) {
    return {
      success: false,
      errCode: 'VALIDATION_ERROR',
      errMessage: error.message || '配置验证失败'
    };
  }
};

/**
 * 获取默认RAG配置
 */
export const getDefaultRagConfig = async (): Promise<ApiResponse<RagComponentConfig>> => {
  try {
    const validation = await import('@/utils/ragConfigValidator');
    const defaultConfig = validation.getDefaultRagConfig();
    
    return {
      success: true,
      data: defaultConfig
    };
  } catch (error: any) {
    return {
      success: false,
      errCode: 'CONFIG_ERROR',
      errMessage: error.message || '获取默认配置失败'
    };
  }
};

/**
 * 测试RAG组件连接
 */
export const testRagComponents = async (config: RagComponentConfig): Promise<ApiResponse<{ 
  webSearchAvailable: boolean;
  knowledgeBaseAvailable: boolean;
  estimatedLatency: number;
}>> => {
  try {
    const response = await request.post('/smartcs/api/admin/rag/test', config) as any;
    
    return {
      success: response.success || false,
      data: {
        webSearchAvailable: response.data?.webSearchAvailable || false,
        knowledgeBaseAvailable: response.data?.knowledgeBaseAvailable || false,
        estimatedLatency: response.data?.estimatedLatency || 0
      }
    };
  } catch (error: any) {
    console.error('RAG组件测试失败:', error);
    
    return {
      success: false,
      errCode: error.code || 'RAG_TEST_ERROR',
      errMessage: error.message || 'RAG组件测试失败，请稍后重试'
    };
  }
};

/**
 * 获取RAG性能统计
 */
export const getRagPerformanceStats = async (timeRange: '1h' | '24h' | '7d' = '24h'): Promise<ApiResponse<{
  avgResponseTime: number;
  avgRetrievalTime: number;
  avgAggregationTime: number;
  successRate: number;
  totalRequests: number;
}>> => {
  try {
    const response = await request.get(`/smartcs/api/admin/rag/stats?timeRange=${timeRange}`) as any;
    
    return {
      success: response.success || false,
      data: {
        avgResponseTime: response.data?.avgResponseTime || 0,
        avgRetrievalTime: response.data?.avgRetrievalTime || 0,
        avgAggregationTime: response.data?.avgAggregationTime || 0,
        successRate: response.data?.successRate || 0,
        totalRequests: response.data?.totalRequests || 0
      }
    };
  } catch (error: any) {
    console.error('获取RAG性能统计失败:', error);
    
    return {
      success: false,
      errCode: error.code || 'RAG_STATS_ERROR',
      errMessage: error.message || '获取RAG性能统计失败，请稍后重试'
    };
  }
};

/**
 * 获取消息表情反应
 */
export const getMessageReactions = async (msgId: string): Promise<ApiResponse<any[]>> => {
  try {
    const response = await request.get(`/api/chat/messages/${msgId}/reactions`) as any;
    
    return {
      success: response.success || true,
      data: response.data || []
    };
  } catch (error: any) {
    console.error('获取消息反应失败:', error);
    
    return {
      success: false,
      errCode: error.code || 'GET_REACTIONS_ERROR',
      errMessage: error.message || '获取消息反应失败'
    };
  }
};

/**
 * 添加/移除消息表情反应
 */
export const addMessageReaction = async (data: {
  msgId: string;
  sessionId: string;
  emoji: string;
  name: string;
  action: 'add' | 'remove';
}): Promise<ApiResponse<any>> => {
  try {
    const response = await request.post(`/api/chat/messages/${data.msgId}/reactions`, {
      sessionId: data.sessionId,
      emoji: data.emoji,
      name: data.name,
      action: data.action
    }) as any;
    
    return {
      success: response.success !== false,
      data: response.data
    };
  } catch (error: any) {
    console.error('操作消息反应失败:', error);
    
    return {
      success: false,
      errCode: error.code || 'REACTION_ERROR',
      errMessage: error.message || '操作失败'
    };
  }
};

// 导出便捷函数
export { validateRagConfig as validateConfig } from '@/utils/ragConfigValidator';
export { getDefaultRagConfig as getDefaultConfig } from '@/utils/ragConfigValidator';