import request from './config';

// 幂等Token请求接口
export interface IdempotencyTokenRequest {
  scope: string;
  context?: string;
}

// 幂等Token响应接口
export interface IdempotencyTokenResponse {
  token: string;
  expiresAt: number;
  scope: string;
}

// API响应通用接口
export interface ApiResponse<T> {
  success: boolean;
  errCode?: string;
  errMessage?: string;
  data: T;
}

// 幂等状态枚举
export enum IdempotentStatus {
  FIRST_TIME = 'FIRST_TIME',
  PROCESSING = 'PROCESSING', 
  SUCCEEDED = 'SUCCEEDED',
  FAILED = 'FAILED'
}

// 幂等状态接口
export interface IdempotentState {
  status: IdempotentStatus;
  resultRef?: string;
  error?: string;
  createdAt?: number;
}

/**
 * 生成幂等Token (POST)
 */
export const generateIdempotencyToken = (params: IdempotencyTokenRequest): Promise<ApiResponse<IdempotencyTokenResponse>> => {
  return request.post('/idempotency/token', params);
};

/**
 * 生成幂等Token (GET)
 */
export const getIdempotencyToken = (scope: string, context?: string): Promise<ApiResponse<IdempotencyTokenResponse>> => {
  return request.get('/idempotency/token', {
    params: { scope, context }
  });
};

/**
 * 查询幂等结果
 */
export const getIdempotencyResult = (key: string): Promise<ApiResponse<IdempotentState>> => {
  return request.get(`/idempotency/result/${key}`);
};

/**
 * 幂等性管理器类
 */
export class IdempotencyManager {
  private static instance: IdempotencyManager;
  private tokenCache = new Map<string, IdempotencyTokenResponse>();
  
  static getInstance(): IdempotencyManager {
    if (!this.instance) {
      this.instance = new IdempotencyManager();
    }
    return this.instance;
  }
  
  /**
   * 获取或生成幂等Token
   */
  async getOrGenerateToken(scope: string, context?: string): Promise<string> {
    const cacheKey = `${scope}:${context || ''}`;
    
    // 检查缓存
    const cached = this.tokenCache.get(cacheKey);
    if (cached && cached.expiresAt > Date.now()) {
      return cached.token;
    }
    
    // 生成新Token
    const response = await getIdempotencyToken(scope, context);
    if (response.success && response.data) {
      this.tokenCache.set(cacheKey, response.data);
      
      // 保存到sessionStorage以支持页面刷新
      const storageKey = `idempotency_token_${cacheKey}`;
      sessionStorage.setItem(storageKey, JSON.stringify({
        token: response.data.token,
        expiresAt: response.data.expiresAt,
        scope: response.data.scope
      }));
      
      return response.data.token;
    }
    
    throw new Error('Failed to generate idempotency token');
  }
  
  /**
   * 从localStorage/sessionStorage恢复Token
   */
  restoreTokenFromStorage(scope: string, context?: string): string | null {
    const cacheKey = `${scope}:${context || ''}`;
    const storageKey = `idempotency_token_${cacheKey}`;
    
    try {
      const stored = sessionStorage.getItem(storageKey);
      if (stored) {
        const tokenData = JSON.parse(stored);
        if (tokenData.expiresAt > Date.now()) {
          return tokenData.token;
        }
      }
    } catch (error) {
      console.warn('Failed to restore idempotency token from storage:', error);
    }
    
    return null;
  }
  
  /**
   * 清理过期Token
   */
  cleanupExpiredTokens(): void {
    const now = Date.now();
    
    // 清理内存缓存
    for (const [key, token] of this.tokenCache.entries()) {
      if (token.expiresAt <= now) {
        this.tokenCache.delete(key);
      }
    }
    
    // 清理sessionStorage
    const keysToRemove: string[] = [];
    for (let i = 0; i < sessionStorage.length; i++) {
      const key = sessionStorage.key(i);
      if (key?.startsWith('idempotency_token_')) {
        try {
          const stored = sessionStorage.getItem(key);
          if (stored) {
            const tokenData = JSON.parse(stored);
            if (tokenData.expiresAt <= now) {
              keysToRemove.push(key);
            }
          }
        } catch (error) {
          keysToRemove.push(key);
        }
      }
    }
    
    keysToRemove.forEach(key => sessionStorage.removeItem(key));
  }
  
  /**
   * 轮询幂等结果
   */
  async pollResult(token: string, maxAttempts: number = 10, intervalMs: number = 1000): Promise<IdempotentState> {
    for (let i = 0; i < maxAttempts; i++) {
      try {
        const response = await getIdempotencyResult(token);
        if (response.success && response.data) {
          const state = response.data;
          
          // 如果已完成（成功或失败），返回结果
          if (state.status === IdempotentStatus.SUCCEEDED || state.status === IdempotentStatus.FAILED) {
            return state;
          }
        }
      } catch (error) {
        console.warn(`Polling attempt ${i + 1} failed:`, error);
      }
      
      // 等待后继续轮询
      if (i < maxAttempts - 1) {
        await new Promise(resolve => setTimeout(resolve, intervalMs));
      }
    }
    
    throw new Error('Polling timeout: request still processing');
  }
}