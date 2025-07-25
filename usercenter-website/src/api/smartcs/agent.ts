import request from '../config';
import { ElMessage } from 'element-plus';

// 客服基础接口
export interface AgentBase {
  agentId: number;
  agentName: string;
  status: 'ONLINE' | 'OFFLINE' | 'BUSY';
}

// 客服详情接口
export interface AgentVO extends AgentBase {
  activeSessions: number;
  totalSessions: number;
  lastActiveTime: Date;
  userId?: number;
  email?: string;
  phone?: string;
  avatar?: string;
}

// UserDTO接口，对应后端的UserDTO
export interface UserDTO {
  id: number;
  username: string;
  email?: string;
  phone?: string;
  nickname?: string;
  avatar?: string;
  status?: number;
}

// API响应接口
interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  success: boolean;
  errCode?: string;
  errMessage?: string;
}

// 客服API服务
export const agentApi = {
  // 获取所有客服列表
  getAllAgents: async (): Promise<AgentVO[]> => {
    try {
      const response = await request.get<ApiResponse<AgentVO[]>>('/smartcs/api/agents');
      return response.data.data || [];
    } catch (error) {
      ElMessage.error('获取客服列表失败');
      console.error('获取客服列表失败:', error);
      return [];
    }
  },

  // 获取在线客服列表
  getOnlineAgents: async (): Promise<AgentVO[]> => {
    try {
      const response = await request.get<ApiResponse<AgentVO[]>>('/smartcs/api/admin/agents/online');
      return response.data.data;
    } catch (error) {
      ElMessage.error('获取在线客服列表失败');
      console.error('获取在线客服列表失败:', error);
      return [];
    }
  },

  // 根据角色编码获取用户列表，适配为客服格式
  listUsersByRoleCode: async (roleCode: string): Promise<AgentVO[]> => {
    try {
      const response = await request.get<UserDTO[]>(`/users/by-role/${roleCode}`);
      
      // 将UserDTO适配为客服格式
      return response.data.map(user => ({
        agentId: user.id,
        agentName: user.nickname || user.username,
        status: 'ONLINE', // 默认设置为在线状态
        activeSessions: 0,
        totalSessions: 0,
        lastActiveTime: new Date(),
        userId: user.id,
        email: user.email,
        phone: user.phone,
        avatar: user.avatar
      }));
    } catch (error) {
      ElMessage.error('获取客服用户列表失败');
      console.error('获取客服用户列表失败:', error);
      return [];
    }
  },

  // 获取单个客服信息
  getAgentById: async (agentId: number): Promise<AgentVO | null> => {
    try {
      const response = await request.get<ApiResponse<AgentVO>>(`/smartcs/api/agents/${agentId}`);
      return response.data.data || null;
    } catch (error) {
      ElMessage.error('获取客服信息失败');
      console.error('获取客服信息失败:', error);
      return null;
    }
  },

  // 更新客服状态
  updateAgentStatus: async (agentId: number, status: 'ONLINE' | 'OFFLINE' | 'BUSY'): Promise<boolean> => {
    try {
      const response = await request.put<ApiResponse<boolean>>(`/smartcs/api/agents/${agentId}/status`, { status });
      if (response.data.success) {
        ElMessage.success(`客服状态已更新为${status === 'ONLINE' ? '在线' : status === 'OFFLINE' ? '离线' : '忙碌'}`);
      }
      return response.data.success;
    } catch (error) {
      ElMessage.error('更新客服状态失败');
      console.error('更新客服状态失败:', error);
      return false;
    }
  },

  // 获取客服的会话列表
  getAgentSessions: async (agentId: number): Promise<any[]> => {
    try {
      const response = await request.get<ApiResponse<any[]>>(`/smartcs/api/agents/${agentId}/sessions`);
      return response.data.data || [];
    } catch (error) {
      ElMessage.error('获取客服会话列表失败');
      console.error('获取客服会话列表失败:', error);
      return [];
    }
  },

  // 获取客服统计信息
  getAgentStats: async (agentId: number): Promise<any> => {
    try {
      const response = await request.get<ApiResponse<any>>(`/smartcs/api/agents/${agentId}/stats`);
      return response.data.data || {};
    } catch (error) {
      ElMessage.error('获取客服统计信息失败');
      console.error('获取客服统计信息失败:', error);
      return {};
    }
  }
}; 