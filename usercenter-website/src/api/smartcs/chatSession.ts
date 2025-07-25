import request from '../config';
import { ElMessage } from 'element-plus';

// 会话基础接口
interface SessionBase {
  sessionId?: string;
  sessionName?: string;
  customerId: number;
  status?: string;
  lastMessage?: string;
  lastMsgTime?: Date;
  createdAt?: Date;
}

// 创建会话请求
export interface CreateSessionRequest {
  customerId: number;
}

// 会话详情接口
export interface SessionVO extends SessionBase {
  sessionId: string;
  agentId: number;
  agentName: string;
  customerId: number;
  sessionState: string;
  lastMsgTime: Date;
}

// 会话分页查询参数
export interface SessionPageQuery {
  sessionId?: string;
  customerId?: number;
  agentId?: number;
  agentName?: string;
  status?: string;
  pageIndex?: number;
  pageSize?: number;
}

// 分页响应接口
export interface PageResponse<T> {
  data: T[];
  total: number;
  pageSize: number;
  pageIndex: number;
}

// API响应接口
interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  success: boolean;
}

// 会话API服务
export const chatSessionApi = {
  // 创建会话
  createSession: async (sessionRequest: CreateSessionRequest): Promise<SessionVO | null> => {
    try {
      const response = await request.post<ApiResponse<SessionVO>>('/smartcs/api/admin/chat/sessions', sessionRequest);
      // response 示例：{
    // "success": true,
    // "data": {
    //     "sessionId": "60002361711673344",
    //     "customerId": "1",
    //     "sessionState": "WAITING",
    //     "lastMsgTime": "1749995277704"
    // }
      if (response.success) {
        return response.data;
      }
      return null;
    } catch (error) {
      console.error('创建会话失败:', error);
      return null;
    }
  },

  // 分配客服
  assignAgent: async (sessionId: string, agentId: number, agentName?: string): Promise<SessionVO | null> => {
    try {
      let url = `/smartcs/api/admin/chat/sessions/${sessionId}/assign?agentId=${agentId}`;
      if (agentName) {
        url += `&agentName=${encodeURIComponent(agentName)}`;
      }
      const response = await request.post<ApiResponse<SessionVO>>(url);
      if (response.success) {
        response.data.sessionId = String(response.data.sessionId);
        return response.data;
      }else{
        console.error('分配客服失败:', response.data.message);
        return null;
      }
    } catch (error) {
      console.error('分配客服失败:', error);
      return null;
    }
  },

  // 关闭会话
  closeSession: async (sessionId: string): Promise<SessionVO | null> => {
    try {
      const response = await request.post<ApiResponse<SessionVO>>(`/smartcs/api/admin/chat/sessions/${sessionId}/close`);
      return response.data;
    } catch (error) {
      console.error('关闭会话失败:', error);
      return null;
    }
  },

  // 获取会话详情
  getSessionDetail: async (sessionId: string): Promise<SessionVO | null> => {
    try {
      const response = await request.get<ApiResponse<SessionVO>>(`/smartcs/api/admin/chat/sessions/${sessionId}`);
      if (response.data.data) {
        response.data.data.sessionId = String(response.data.data.sessionId);
      }
      return response.data.data;
    } catch (error) {
      console.error('获取会话详情失败:', error);
      return null;
    }
  },

  // 获取客户的会话列表
  getCustomerSessions: async (customerId: number, limit: number = 10): Promise<SessionVO[]> => {
    try {
      const response = await request.get<ApiResponse<SessionVO[]>>(`/smartcs/api/admin/chat/sessions/customer/${customerId}?limit=${limit}`);
      console.log(response);
      if (response.success) {
        response.data.forEach(session => {
          session.sessionId = String(session.sessionId);
        });
      }
      return response.data;
    } catch (error) {
      ElMessage.error('获取客户会话列表失败');
      console.error('获取客户会话列表失败:', error);
      return [];
    }
  },

  // 获取客服的活跃会话列表
  getAgentActiveSessions: async (agentId: number): Promise<SessionVO[]> => {
    try {
      const response = await request.get<ApiResponse<SessionVO[]>>(`/smartcs/api/admin/chat/sessions/agent/${agentId}`);
      if (response.success) {
        return response.data;
      }else{
        ElMessage.error('获取客服活跃会话列表失败',response.errMessage);
        return [];
      }
    } catch (error) {
      ElMessage.error('获取客服活跃会话列表失败');
      console.error('获取客服活跃会话列表失败:', error);
      return [];
    }
  },

  // 获取客户最新一条处理中的会话
  getCustomerActiveSession: async (customerId: number): Promise<SessionVO | null> => {
    try {
      const response = await request.get<ApiResponse<SessionVO>>(`/smartcs/api/admin/chat/sessions/customer/${customerId}/active`);
      if (response.data.data) {
        response.data.data.sessionId = String(response.data.data.sessionId);
      }
      return response.data.data;
    } catch (error) {
      ElMessage.error('获取客户活跃会话失败');
      console.error('获取客户活跃会话失败:', error);
      return null;
    }
  },
  
  // 分页查询会话列表
  getSessionsPage: async (query: SessionPageQuery): Promise<PageResponse<SessionVO>> => {
    try {
      const params = new URLSearchParams();
      if (query.sessionId) params.append('sessionId', query.sessionId);
      if (query.customerId) params.append('customerId', query.customerId.toString());
      if (query.agentId) params.append('agentId', query.agentId.toString());
      if (query.agentName) params.append('agentName', query.agentName);
      if (query.status) params.append('status', query.status);
      params.append('pageIndex', (query.pageIndex || 1).toString());
      params.append('pageSize', (query.pageSize || 10).toString());
      
      const response = await request.get<ApiResponse<PageResponse<SessionVO>>>(`/smartcs/api/admin/chat/sessions/page?${params.toString()}`);
      if (response.success) {
        response.data.forEach(session => {
          session.sessionId = String(session.sessionId);
        });
      }
      console.log(response);
      return response;
    } catch (error) {
      ElMessage.error('获取会话列表失败');
      console.error('获取会话列表失败:', error);
      return { data: [], total: 0, pageSize: query.pageSize || 10, pageIndex: query.pageIndex || 1 };
    }
  },

  // 更新会话名称
  updateSessionName: async (sessionId: string, sessionName: string): Promise<SessionVO | null> => {
    try {
      const response = await request.put<ApiResponse<SessionVO>>('/smartcs/api/admin/chat/sessions/name', {
        sessionId,
        sessionName
      });
      if (response.success) {
        return response.data;
      }else{
        ElMessage.error('更新会话名称失败');
        console.error('更新会话名称失败:', response.data.message);
        return null;
      }
    } catch (error) {
      ElMessage.error('更新会话名称失败');
      console.error('更新会话名称失败:', error);
      return null;
    }
  }
};
