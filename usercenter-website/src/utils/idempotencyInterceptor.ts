import axios, { AxiosRequestConfig, AxiosResponse } from 'axios';
import { IdempotencyManager } from '@/api/idempotency';

// 需要幂等处理的接口配置
interface IdempotencyConfig {
  scope: string;           // 作用域
  context?: string;        // 上下文
  autoGenerate?: boolean;  // 是否自动生成Token
  skipIfExists?: boolean;  // 如果已有Token是否跳过
}

// 幂等性拦截器配置
const IDEMPOTENCY_CONFIG: Record<string, IdempotencyConfig> = {
  // 订单创建
  'POST:/api/orders': {
    scope: 'create-order',
    autoGenerate: true
  },
  
  // 支付相关
  'POST:/api/payments': {
    scope: 'payment-process',
    autoGenerate: true
  },
  
  // 库存操作
  'POST:/api/inventory/deduct': {
    scope: 'inventory-deduct',
    autoGenerate: true
  },
  
  // 用户注册
  'POST:/api/users/register': {
    scope: 'user-register',
    autoGenerate: true
  }
};

const IDEMPOTENCY_HEADER = 'Idempotency-Key';
const REQUEST_ID_HEADER = 'X-Request-Id';

/**
 * 生成请求ID
 */
function generateRequestId(): string {
  return Date.now().toString(36) + Math.random().toString(36).substring(2);
}

/**
 * 获取接口配置键
 */
function getConfigKey(method: string, url: string): string {
  // 移除查询参数
  const cleanUrl = url.split('?')[0];
  return `${method.toUpperCase()}:${cleanUrl}`;
}

/**
 * 检查是否需要幂等处理
 */
function needsIdempotency(method: string, url: string): IdempotencyConfig | null {
  // 只对POST/PUT/PATCH请求进行幂等处理
  if (!['POST', 'PUT', 'PATCH'].includes(method.toUpperCase())) {
    return null;
  }
  
  const configKey = getConfigKey(method, url);
  return IDEMPOTENCY_CONFIG[configKey] || null;
}

/**
 * 安装幂等性拦截器
 */
export function setupIdempotencyInterceptor(axiosInstance: typeof axios): void {
  const idempotencyManager = IdempotencyManager.getInstance();
  
  // 请求拦截器 - 添加幂等头
  axiosInstance.interceptors.request.use(
    async (config: AxiosRequestConfig) => {
      const method = config.method?.toUpperCase() || 'GET';
      const url = config.url || '';
      
      // 检查是否需要幂等处理
      const idempotencyConfig = needsIdempotency(method, url);
      if (!idempotencyConfig) {
        return config;
      }
      
      // 添加请求ID头
      if (!config.headers) {
        config.headers = {};
      }
      
      if (!config.headers[REQUEST_ID_HEADER]) {
        config.headers[REQUEST_ID_HEADER] = generateRequestId();
      }
      
      // 如果已有幂等头，根据配置决定是否跳过
      if (config.headers[IDEMPOTENCY_HEADER]) {
        if (idempotencyConfig.skipIfExists) {
          return config;
        }
      }
      
      // 自动生成幂等Token
      if (idempotencyConfig.autoGenerate) {
        try {
          // 先尝试从sessionStorage恢复
          let token = idempotencyManager.restoreTokenFromStorage(
            idempotencyConfig.scope, 
            idempotencyConfig.context
          );
          
          // 如果没有有效Token，生成新的
          if (!token) {
            token = await idempotencyManager.getOrGenerateToken(
              idempotencyConfig.scope,
              idempotencyConfig.context
            );
          }
          
          config.headers[IDEMPOTENCY_HEADER] = token;
          
          console.log(`自动添加幂等头: ${method} ${url} -> ${token}`);
          
        } catch (error) {
          console.error('生成幂等Token失败:', error);
          // 不阻断请求，但记录错误
        }
      }
      
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
  );
  
  // 响应拦截器 - 处理幂等相关响应
  axiosInstance.interceptors.response.use(
    (response: AxiosResponse) => {
      // 正常响应，直接返回
      return response;
    },
    async (error) => {
      // 处理202 Accepted响应
      if (error.response?.status === 202) {
        const idempotencyKey = error.config?.headers?.[IDEMPOTENCY_HEADER];
        
        if (idempotencyKey) {
          console.log('请求处理中，将进行轮询:', idempotencyKey);
          
          try {
            // 轮询结果
            const result = await idempotencyManager.pollResult(idempotencyKey);
            
            // 模拟成功响应
            return {
              ...error.response,
              status: 200,
              data: {
                success: true,
                data: result.resultRef,
                message: '操作成功（轮询获取）'
              }
            };
          } catch (pollError) {
            console.error('轮询幂等结果失败:', pollError);
            return Promise.reject(error);
          }
        }
      }
      
      // 处理409 Conflict响应
      if (error.response?.status === 409) {
        const errorData = error.response.data;
        if (errorData?.errCode === 'DUPLICATE_REQUEST') {
          console.warn('检测到重复请求');
          // 可以选择显示特定的提示或者返回缓存的结果
        }
      }
      
      return Promise.reject(error);
    }
  );
  
  // 定时清理过期Token
  setInterval(() => {
    idempotencyManager.cleanupExpiredTokens();
  }, 5 * 60 * 1000); // 5分钟清理一次
}

/**
 * 手动获取幂等Token
 */
export async function getIdempotencyTokenForScope(scope: string, context?: string): Promise<string> {
  const manager = IdempotencyManager.getInstance();
  return manager.getOrGenerateToken(scope, context);
}

/**
 * 清理所有幂等Token
 */
export function clearAllIdempotencyTokens(): void {
  const manager = IdempotencyManager.getInstance();
  manager.cleanupExpiredTokens();
  
  // 清理sessionStorage中的所有幂等Token
  const keysToRemove: string[] = [];
  for (let i = 0; i < sessionStorage.length; i++) {
    const key = sessionStorage.key(i);
    if (key?.startsWith('idempotency_token_')) {
      keysToRemove.push(key);
    }
  }
  
  keysToRemove.forEach(key => sessionStorage.removeItem(key));
}