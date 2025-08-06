import request from './config';
import { BotChatSSERequestWithRag, RagComponentConfig, RagDebugInfo } from '@/types/chat';

// 扩展现有的聊天请求接口以支持RAG配置
export interface EnhancedChatRequest {
  appId: number;
  modelId: number;
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

    const response = await request.post<ApiResponse<EnhancedChatResponse>>(
      '/smartcs/api/admin/app/chat',
      data,
      {
        timeout: 30000, // 30秒超时
        headers: {
          'Content-Type': 'application/json',
        }
      }
    );

    return response;
  } catch (error: any) {
    console.error('聊天请求失败:', error);
    
    return {
      success: false,
      errCode: error.code || 'CHAT_ERROR',
      errMessage: error.message || '聊天请求失败，请稍后重试'
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
    const response = await request.post('/smartcs/api/admin/rag/test', config, {
      timeout: 10000 // 10秒超时
    });
    
    return response;
  } catch (error: any) {
    return {
      success: false,
      errCode: 'TEST_ERROR',
      errMessage: error.message || 'RAG组件测试失败'
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
    const response = await request.get(`/smartcs/api/admin/rag/stats?timeRange=${timeRange}`);
    return response;
  } catch (error: any) {
    return {
      success: false,
      errCode: 'STATS_ERROR',
      errMessage: error.message || '获取性能统计失败'
    };
  }
};

// 导出便捷函数
export { validateRagConfig as validateConfig } from '@/utils/ragConfigValidator';
export { getDefaultRagConfig as getDefaultConfig } from '@/utils/ragConfigValidator';