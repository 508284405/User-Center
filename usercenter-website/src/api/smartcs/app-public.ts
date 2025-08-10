import request from '../config'
import { AiAppDTO, AppChatRequest, ApiResponse } from './app'

/**
 * 获取公开应用信息（无需认证）
 */
export const getPublicApp = (id: number): Promise<ApiResponse<AiAppDTO>> => {
  return request.get(`/smartcs/api/app/${id}`)
}

/**
 * 公开应用聊天（SSE流式响应） - 无需认证
 */
export const chatWithPublicAppStream = (): string => {
  // 返回公开SSE端点URL，由AppRunner组件直接使用
  return '/smartcs/api/app/chat'
}

// 导出公共类型
export type { AiAppDTO, AppChatRequest, ApiResponse } from './app'