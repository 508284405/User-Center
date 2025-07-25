import { MessageType } from '@/types/chat';
import request from '../config';
import { ElMessage } from 'element-plus';

// 消息基础接口
export interface MessageBase {
  msgId?: string;
  sessionId: string;
  content: string;
  chatType: MessageType;
  createdAt?: string; // 毫秒时间戳字符串
}

// 发送消息请求
export interface SendMessageRequest {
  sessionId: string;
  content: string;
  chatType: string;
}

// 消息查询参数
export interface GetMessagesParams {
  sessionId: string;
  beforeMessageId?: string;
  limit?: number;
}

// 消息详情接口
export interface MessageVO extends MessageBase {
  msgId: string;
}

// API响应接口
interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

// 消息API服务
export const chatMessageApi = {
  // 发送消息
  sendMessage: async (messageRequest: SendMessageRequest): Promise<MessageVO | null> => {
    try {
      const response = await request.post<ApiResponse<MessageVO>>('/smartcs/api/admin/chat/messages', messageRequest);
      if (response.data.data) {
        response.data.data.sessionId = String(response.data.data.sessionId);
      }
      return response.data.data;
    } catch (error) {
      ElMessage.error('发送消息失败');
      console.error('发送消息失败:', error);
      return null;
    }
  },

  // 获取会话消息历史
  getSessionMessages: async (sessionIdOrParams: string | GetMessagesParams): Promise<MessageVO[]> => {
    try {
      let url: string;
      
      if (typeof sessionIdOrParams === 'string') {
        // 传入的是会话ID字符串，获取最新消息
        url = `/smartcs/api/admin/chat/messages/session/${sessionIdOrParams}`;
      } else {
        // 传入的是参数对象
        const params = sessionIdOrParams;
        url = `/smartcs/api/admin/chat/messages/session/${params.sessionId}`;
        
        // 添加查询参数
        const queryParams = new URLSearchParams();
        if (params.beforeMessageId) {
          queryParams.append('beforeMessageId', params.beforeMessageId);
        }
        if (params.limit) {
          queryParams.append('limit', params.limit.toString());
        }
        
        if (queryParams.toString()) {
          url += `?${queryParams.toString()}`;
        }
      }
      
      const response = await request.get<ApiResponse<MessageVO[]>>(url);
      if (response.success) {
        return response.data || [];
      } else {
        ElMessage.error(`获取会话消息历史失败: ${response.errorMessage}`);
        return [];
      }
    } catch (error) {
      ElMessage.error('获取会话消息历史失败');
      console.error('获取会话消息历史失败:', error);
      return [];
    }
  },

  // 分页获取会话消息历史
  getSessionMessagesWithPagination: async (
    sessionId: string, 
    offset: number = 0, 
    limit: number = 20
  ): Promise<MessageVO[]> => {
    try {
      const response = await request.get<ApiResponse<MessageVO[]>>(
        `/smartcs/api/admin/chat/messages/session/${sessionId}/page?offset=${offset}&limit=${limit}`
      );
      if (response.data.data) {
        response.data.data.forEach(message => {
          message.sessionId = String(message.sessionId);
        });
      }
      return response.data.data;
    } catch (error) {
      ElMessage.error('分页获取会话消息历史失败');
      console.error('分页获取会话消息历史失败:', error);
      return [];
    }
  }
};
