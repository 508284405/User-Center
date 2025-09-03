import request from '../config';

// 好友相关接口类型定义
export interface FriendInfo {
  id: number;
  friendUserId: string;
  friendUserName?: string;
  friendAvatar?: string;
  remarkName?: string;
  friendGroup?: string;
  status: number;
  statusText: string;
  onlineStatus?: string;
  lastSeenAt?: number;
  appliedAt?: number;
  processedAt?: number;
  createdAt: number;
  updatedAt: number;
  displayName: string;
  canChat: boolean;
  unreadCount?: number;
  lastMessage?: string;
  lastMessageTime?: number;
}

export interface FriendApplication {
  id: number;
  fromUserId: string;
  fromUserName?: string;
  fromUserAvatar?: string;
  toUserId: string;
  applyMessage?: string;
  status: number;
  statusText: string;
  appliedAt: number;
  processedAt?: number;
}

export interface FriendGroup {
  id: number;
  groupName: string;
  groupOrder: number;
  groupColor?: string;
  friendCount: number;
}

export interface UserSearchResult {
  userId: string;
  userName: string;
  avatar?: string;
  email?: string;
  isFriend: boolean;
  hasApplication: boolean;
}

// API响应类型
export interface ApiResponse<T = any> {
  data?: T;
  success: boolean;
  errCode?: string;
  errMessage?: string;
}

/**
 * 获取好友列表
 */
export const getFriendsList = async (params: {
  userId: string;
  keyword?: string;
  friendGroup?: string;
  onlineStatus?: string;
}): Promise<ApiResponse<FriendInfo[]>> => {
  try {
    const response = await request.get('/api/chat/friends/list', { params });
    return {
      success: response.data?.success !== false,
      data: response.data?.data || [],
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('获取好友列表失败:', error);
    return {
      success: false,
      errCode: error.code || 'FRIENDS_LIST_ERROR',
      errMessage: error.message || '获取好友列表失败'
    };
  }
};

/**
 * 发送好友申请
 */
export const sendFriendRequest = async (data: {
  fromUserId: string;
  toUserId: string;
  applyMessage?: string;
}): Promise<ApiResponse> => {
  try {
    const response = await request.post('/api/chat/friends/apply', data);
    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('发送好友申请失败:', error);
    return {
      success: false,
      errCode: error.code || 'FRIEND_REQUEST_ERROR',
      errMessage: error.message || '发送好友申请失败'
    };
  }
};

/**
 * 处理好友申请
 */
export const processFriendApplication = async (data: {
  applicationId: number;
  action: 'accept' | 'reject' | 'block';
  processedBy: string;
  rejectReason?: string;
  remarkName?: string;
  friendGroup?: string;
}): Promise<ApiResponse> => {
  try {
    const response = await request.post('/api/chat/friends/process-application', data);
    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('处理好友申请失败:', error);
    return {
      success: false,
      errCode: error.code || 'PROCESS_APPLICATION_ERROR',
      errMessage: error.message || '处理申请失败'
    };
  }
};

/**
 * 获取收到的好友申请列表
 */
export const getReceivedApplications = async (userId: string): Promise<ApiResponse<FriendApplication[]>> => {
  try {
    const response = await request.get(`/api/chat/friends/applications/received`, {
      params: { userId }
    });
    return {
      success: response.data?.success !== false,
      data: response.data?.data || [],
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('获取收到的申请失败:', error);
    return {
      success: false,
      errCode: error.code || 'GET_APPLICATIONS_ERROR',
      errMessage: error.message || '获取申请列表失败'
    };
  }
};

/**
 * 获取发送的好友申请列表
 */
export const getSentApplications = async (userId: string): Promise<ApiResponse<FriendApplication[]>> => {
  try {
    const response = await request.get(`/api/chat/friends/applications/sent`, {
      params: { userId }
    });
    return {
      success: response.data?.success !== false,
      data: response.data?.data || [],
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('获取发送的申请失败:', error);
    return {
      success: false,
      errCode: error.code || 'GET_SENT_APPLICATIONS_ERROR',
      errMessage: error.message || '获取发送的申请列表失败'
    };
  }
};

/**
 * 搜索用户
 */
export const searchUsers = async (params: {
  keyword: string;
  searchUserId: string;
  page?: number;
  size?: number;
}): Promise<ApiResponse<UserSearchResult[]>> => {
  try {
    const response = await request.get('/api/chat/friends/search-users', { params });
    return {
      success: response.data?.success !== false,
      data: response.data?.data || [],
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('搜索用户失败:', error);
    return {
      success: false,
      errCode: error.code || 'SEARCH_USERS_ERROR',
      errMessage: error.message || '搜索用户失败'
    };
  }
};

/**
 * 删除好友
 */
export const deleteFriend = async (friendId: number, userId: string): Promise<ApiResponse> => {
  try {
    const response = await request.delete(`/api/chat/friends/${friendId}`, {
      params: { userId }
    });
    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('删除好友失败:', error);
    return {
      success: false,
      errCode: error.code || 'DELETE_FRIEND_ERROR',
      errMessage: error.message || '删除好友失败'
    };
  }
};

/**
 * 更新好友信息
 */
export const updateFriend = async (friendId: number, data: {
  remarkName?: string;
  friendGroup?: string;
}): Promise<ApiResponse> => {
  try {
    const response = await request.put(`/api/chat/friends/${friendId}`, data);
    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('更新好友信息失败:', error);
    return {
      success: false,
      errCode: error.code || 'UPDATE_FRIEND_ERROR',
      errMessage: error.message || '更新好友信息失败'
    };
  }
};

/**
 * 获取好友分组列表
 */
export const getFriendGroups = async (userId: string): Promise<ApiResponse<FriendGroup[]>> => {
  try {
    const response = await request.get('/api/chat/friends/groups', {
      params: { userId }
    });
    return {
      success: response.data?.success !== false,
      data: response.data?.data || [],
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('获取好友分组失败:', error);
    return {
      success: false,
      errCode: error.code || 'GET_GROUPS_ERROR',
      errMessage: error.message || '获取分组列表失败'
    };
  }
};

/**
 * 创建好友分组
 */
export const createFriendGroup = async (data: {
  userId: string;
  groupName: string;
  groupColor?: string;
}): Promise<ApiResponse> => {
  try {
    const response = await request.post('/api/chat/friends/groups', data);
    return {
      success: response.data?.success !== false,
      data: response.data?.data,
      errCode: response.data?.errCode,
      errMessage: response.data?.errMessage
    };
  } catch (error: any) {
    console.error('创建好友分组失败:', error);
    return {
      success: false,
      errCode: error.code || 'CREATE_GROUP_ERROR',
      errMessage: error.message || '创建分组失败'
    };
  }
};