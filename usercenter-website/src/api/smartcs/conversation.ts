import request from '../config';

// 会话设置相关接口类型定义
export interface ConversationSettings {
  id: number;
  userId: string;
  sessionId: number;
  isPinned: boolean;
  pinnedAt?: number;
  isMuted: boolean;
  mutedAt?: number;
  muteEndAt?: number;
  isArchived: boolean;
  archivedAt?: number;
  customBackground?: string;
  notificationSound?: string;
  createdAt: number;
  updatedAt: number;
}

export interface ConversationTag {
  id: number;
  userId: string;
  tagName: string;
  tagColor?: string;
  tagOrder: number;
  createdAt: number;
  updatedAt: number;
}

export interface MessageSearchResult {
  msgId: string;
  sessionId: number;
  content: string;
  messageType: string;
  senderName?: string;
  createdAt: number;
  highlightText?: string;
}

export interface SearchOptions {
  keyword: string;
  userId: string;
  sessionId?: number;
  messageType?: string;
  startTime?: number;
  endTime?: number;
  page?: number;
  size?: number;
}

// API响应类型
export interface ApiResponse<T = any> {
  data?: T;
  success: boolean;
  errCode?: string;
  errMessage?: string;
}

/**
 * 获取会话设置
 */
export const getConversationSettings = async (userId: string, sessionId: number): Promise<ApiResponse<ConversationSettings>> => {
  try {
    const response = await request.get('/api/chat/conversations/settings', {
      params: { userId, sessionId }
    });
    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('获取会话设置失败:', error);
    return {
      success: false,
      errCode: error.code || 'GET_SETTINGS_ERROR',
      errMessage: error.message || '获取会话设置失败'
    };
  }
};

/**
 * 置顶/取消置顶会话
 */
export const togglePinConversation = async (userId: string, sessionId: number, pin: boolean): Promise<ApiResponse> => {
  try {
    const response = await request.post('/api/chat/conversations/pin', {
      userId,
      sessionId,
      pin
    });
    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('设置会话置顶失败:', error);
    return {
      success: false,
      errCode: error.code || 'PIN_CONVERSATION_ERROR',
      errMessage: error.message || '设置会话置顶失败'
    };
  }
};

/**
 * 设置/取消免打扰
 */
export const toggleMuteConversation = async (data: {
  userId: string;
  sessionId: number;
  mute: boolean;
  muteEndAt?: number; // null表示永久免打扰
}): Promise<ApiResponse> => {
  try {
    const response = await request.post('/api/chat/conversations/mute', data);
    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('设置免打扰失败:', error);
    return {
      success: false,
      errCode: error.code || 'MUTE_CONVERSATION_ERROR',
      errMessage: error.message || '设置免打扰失败'
    };
  }
};

/**
 * 归档/取消归档会话
 */
export const toggleArchiveConversation = async (userId: string, sessionId: number, archive: boolean): Promise<ApiResponse> => {
  try {
    const response = await request.post('/api/chat/conversations/archive', {
      userId,
      sessionId,
      archive
    });
    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('设置会话归档失败:', error);
    return {
      success: false,
      errCode: error.code || 'ARCHIVE_CONVERSATION_ERROR',
      errMessage: error.message || '设置会话归档失败'
    };
  }
};

/**
 * 搜索消息
 */
export const searchMessages = async (options: SearchOptions): Promise<ApiResponse<{
  results: MessageSearchResult[];
  total: number;
  page: number;
  size: number;
}>> => {
  try {
    const response = await request.get('/api/chat/messages/search', {
      params: options
    });
    return {
      success: response.data?.success !== false,
      data: response.data?.data || { results: [], total: 0, page: 1, size: 20 },
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('搜索消息失败:', error);
    return {
      success: false,
      errCode: error.code || 'SEARCH_MESSAGES_ERROR',
      errMessage: error.message || '搜索消息失败',
      data: { results: [], total: 0, page: 1, size: 20 }
    };
  }
};

/**
 * 获取用户的会话标签
 */
export const getConversationTags = async (userId: string): Promise<ApiResponse<ConversationTag[]>> => {
  try {
    const response = await request.get('/api/chat/conversations/tags', {
      params: { userId }
    });
    return {
      success: response.data?.success !== false,
      data: response.data?.data || [],
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('获取会话标签失败:', error);
    return {
      success: false,
      errCode: error.code || 'GET_TAGS_ERROR',
      errMessage: error.message || '获取会话标签失败',
      data: []
    };
  }
};

/**
 * 创建会话标签
 */
export const createConversationTag = async (data: {
  userId: string;
  tagName: string;
  tagColor?: string;
}): Promise<ApiResponse> => {
  try {
    const response = await request.post('/api/chat/conversations/tags', data);
    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('创建会话标签失败:', error);
    return {
      success: false,
      errCode: error.code || 'CREATE_TAG_ERROR',
      errMessage: error.message || '创建会话标签失败'
    };
  }
};

/**
 * 为会话添加标签
 */
export const addTagToConversation = async (data: {
  userId: string;
  sessionId: number;
  tagId: number;
}): Promise<ApiResponse> => {
  try {
    const response = await request.post('/api/chat/conversations/add-tag', data);
    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('添加会话标签失败:', error);
    return {
      success: false,
      errCode: error.code || 'ADD_TAG_ERROR',
      errMessage: error.message || '添加会话标签失败'
    };
  }
};

/**
 * 从会话移除标签
 */
export const removeTagFromConversation = async (data: {
  userId: string;
  sessionId: number;
  tagId: number;
}): Promise<ApiResponse> => {
  try {
    const response = await request.post('/api/chat/conversations/remove-tag', data);
    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('移除会话标签失败:', error);
    return {
      success: false,
      errCode: error.code || 'REMOVE_TAG_ERROR',
      errMessage: error.message || '移除会话标签失败'
    };
  }
};

/**
 * 设置会话自定义背景
 */
export const setConversationBackground = async (data: {
  userId: string;
  sessionId: number;
  background: string;
}): Promise<ApiResponse> => {
  try {
    const response = await request.post('/api/chat/conversations/background', data);
    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('设置会话背景失败:', error);
    return {
      success: false,
      errCode: error.code || 'SET_BACKGROUND_ERROR',
      errMessage: error.message || '设置会话背景失败'
    };
  }
};

/**
 * 获取归档的会话列表
 */
export const getArchivedConversations = async (userId: string): Promise<ApiResponse<ConversationSettings[]>> => {
  try {
    const response = await request.get('/api/chat/conversations/archived', {
      params: { userId }
    });
    return {
      success: response.data?.success !== false,
      data: response.data?.data || [],
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('获取归档会话失败:', error);
    return {
      success: false,
      errCode: error.code || 'GET_ARCHIVED_ERROR',
      errMessage: error.message || '获取归档会话失败',
      data: []
    };
  }
};