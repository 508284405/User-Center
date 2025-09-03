import request from '../config';

// 表情反应相关接口类型定义
export interface ReactionDTO {
  emoji: string;
  name: string;
  count: number;
  userIds: string[];
  hasReacted: boolean;
}

export interface AddReactionRequest {
  msgId: string;
  sessionId: number;
  emoji: string;
  name: string;
  action: 'add' | 'remove' | 'toggle';
}

export interface MessageReactionData {
  msgId: string;
  reactions: ReactionDTO[];
}

// API响应类型
export interface ApiResponse<T = any> {
  data?: T;
  success: boolean;
  errCode?: string;
  errMessage?: string;
}

/**
 * 添加消息表情反应
 */
export const addMessageReaction = async (data: AddReactionRequest): Promise<ApiResponse> => {
  try {
    const response = await request.post('/api/chat/messages/reactions', data);
    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('添加消息表情反应失败:', error);
    return {
      success: false,
      errCode: error.code || 'ADD_REACTION_ERROR',
      errMessage: error.message || '添加表情反应失败'
    };
  }
};

/**
 * 移除消息表情反应
 */
export const removeMessageReaction = async (msgId: string, emoji: string): Promise<ApiResponse> => {
  try {
    const response = await request.delete('/api/chat/messages/reactions', {
      data: { msgId, emoji }
    });
    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('移除消息表情反应失败:', error);
    return {
      success: false,
      errCode: error.code || 'REMOVE_REACTION_ERROR',
      errMessage: error.message || '移除表情反应失败'
    };
  }
};

/**
 * 获取消息的所有表情反应
 */
export const getMessageReactions = async (msgId: string): Promise<ApiResponse<ReactionDTO[]>> => {
  try {
    const response = await request.get(`/api/chat/messages/${msgId}/reactions`);
    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('获取消息表情反应失败:', error);
    return {
      success: false,
      errCode: error.code || 'GET_REACTIONS_ERROR',
      errMessage: error.message || '获取表情反应失败'
    };
  }
};

/**
 * 切换消息表情反应
 */
export const toggleMessageReaction = async (msgId: string, sessionId: number, emoji: string, name: string): Promise<ApiResponse> => {
  return addMessageReaction({
    msgId,
    sessionId,
    emoji,
    name,
    action: 'toggle'
  });
};

/**
 * 获取用户在消息上的表情反应
 */
export const getUserMessageReaction = async (msgId: string, userId: string): Promise<ApiResponse<ReactionDTO[]>> => {
  try {
    const response = await request.get(`/api/chat/messages/${msgId}/reactions/user/${userId}`);
    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('获取用户消息表情反应失败:', error);
    return {
      success: false,
      errCode: error.code || 'GET_USER_REACTIONS_ERROR',
      errMessage: error.message || '获取用户表情反应失败'
    };
  }
};

/**
 * 批量获取多个消息的表情反应
 */
export const getBatchMessageReactions = async (msgIds: string[]): Promise<ApiResponse<MessageReactionData[]>> => {
  try {
    const response = await request.post('/api/chat/messages/reactions/batch', {
      msgIds
    });
    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('批量获取消息表情反应失败:', error);
    return {
      success: false,
      errCode: error.code || 'GET_BATCH_REACTIONS_ERROR',
      errMessage: error.message || '批量获取表情反应失败'
    };
  }
};

/**
 * 获取表情反应统计信息
 */
export const getReactionStats = async (msgId: string): Promise<ApiResponse<{
  totalReactions: number;
  uniqueUsers: number;
  topReactions: Array<{ emoji: string; name: string; count: number }>;
}>> => {
  try {
    const response = await request.get(`/api/chat/messages/${msgId}/reactions/stats`);
    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('获取表情反应统计失败:', error);
    return {
      success: false,
      errCode: error.code || 'GET_REACTION_STATS_ERROR',
      errMessage: error.message || '获取表情反应统计失败'
    };
  }
};