import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { 
  sendUserStatus, 
  registerMessageHandler, 
  UserStatus, 
  UserStatusMessage, 
  UserStatusNotification 
} from '@/utils/chatWebSocket';

/**
 * 用户状态管理的组合式函数
 */
export function useUserStatus() {
  // 当前用户状态
  const currentUserStatus = ref<UserStatus>(UserStatus.ONLINE);
  
  // 其他用户状态映射
  const userStatusMap = ref<Map<string, { userId: string; userName?: string; status: UserStatus; timestamp: number }>>(new Map());
  
  // 状态中文显示映射
  const statusTextMap = {
    [UserStatus.ONLINE]: '在线',
    [UserStatus.OFFLINE]: '离线',
    [UserStatus.BUSY]: '忙碌',
    [UserStatus.AWAY]: '离开',
    [UserStatus.INVISIBLE]: '隐身'
  };
  
  // 状态颜色映射
  const statusColorMap = {
    [UserStatus.ONLINE]: '#67c23a',
    [UserStatus.OFFLINE]: '#909399',
    [UserStatus.BUSY]: '#f56c6c',
    [UserStatus.AWAY]: '#e6a23c',
    [UserStatus.INVISIBLE]: '#c0c4cc'
  };
  
  // 获取状态显示文本
  const getStatusText = (status: UserStatus): string => {
    return statusTextMap[status] || '未知';
  };
  
  // 获取状态颜色
  const getStatusColor = (status: UserStatus): string => {
    return statusColorMap[status] || '#909399';
  };
  
  // 获取用户状态
  const getUserStatus = (userId: string): UserStatus => {
    const user = userStatusMap.value.get(userId);
    return user ? user.status : UserStatus.OFFLINE;
  };
  
  // 获取用户状态信息
  const getUserStatusInfo = (userId: string) => {
    const user = userStatusMap.value.get(userId);
    if (!user) {
      return {
        status: UserStatus.OFFLINE,
        text: getStatusText(UserStatus.OFFLINE),
        color: getStatusColor(UserStatus.OFFLINE),
        timestamp: 0
      };
    }
    
    return {
      status: user.status,
      text: getStatusText(user.status),
      color: getStatusColor(user.status),
      timestamp: user.timestamp
    };
  };
  
  // 设置当前用户状态
  const setCurrentUserStatus = async (status: UserStatus): Promise<boolean> => {
    const userId = localStorage.getItem('userId') || '';
    if (!userId) {
      ElMessage.error('用户ID不存在');
      return false;
    }
    
    try {
      const statusMessage: UserStatusMessage = {
        userId,
        status,
        timestamp: Date.now(),
        type: 'USER_STATUS'
      };
      
      const success = sendUserStatus(statusMessage);
      if (success) {
        currentUserStatus.value = status;
        console.log(`用户状态已更新为: ${getStatusText(status)}`);
        return true;
      } else {
        ElMessage.error('更新状态失败');
        return false;
      }
    } catch (error) {
      console.error('设置用户状态失败:', error);
      ElMessage.error('设置用户状态失败');
      return false;
    }
  };
  
  // 处理用户状态通知
  const handleUserStatusNotification = (notification: UserStatusNotification) => {
    const currentUserId = localStorage.getItem('userId') || '';
    
    // 忽略自己的状态通知
    if (notification.userId === currentUserId) return;
    
    // 更新用户状态映射
    if (notification.status === UserStatus.OFFLINE) {
      // 用户离线，从映射中移除
      userStatusMap.value.delete(notification.userId);
    } else {
      // 用户在线或其他状态，更新映射
      userStatusMap.value.set(notification.userId, {
        userId: notification.userId,
        userName: notification.userName,
        status: notification.status,
        timestamp: notification.timestamp
      });
    }
    
    console.log(`用户 ${notification.userName || notification.userId} 状态更新为: ${getStatusText(notification.status)}`);
  };
  
  // 获取在线用户列表
  const getOnlineUsers = computed(() => {
    const users = Array.from(userStatusMap.value.values());
    return users.filter(user => user.status !== UserStatus.OFFLINE);
  });
  
  // 获取在线用户数量
  const onlineUserCount = computed(() => getOnlineUsers.value.length);
  
  // 清理过期的用户状态
  const cleanupExpiredStatus = () => {
    const now = Date.now();
    const expiredUsers: string[] = [];
    
    userStatusMap.value.forEach((user, userId) => {
      // 超过10分钟没有更新状态的用户标记为离线
      if (now - user.timestamp > 10 * 60 * 1000) {
        expiredUsers.push(userId);
      }
    });
    
    expiredUsers.forEach(userId => {
      userStatusMap.value.delete(userId);
    });
  };
  
  // 启动清理定时器
  const cleanupInterval = setInterval(cleanupExpiredStatus, 60000); // 每分钟检查一次
  
  // 注册用户状态处理器
  const registerUserStatusHandler = () => {
    return registerMessageHandler('userStatus', handleUserStatusNotification);
  };
  
  // 初始化用户状态（上线）
  const initializeUserStatus = async () => {
    await setCurrentUserStatus(UserStatus.ONLINE);
  };
  
  // 用户离线
  const setUserOffline = async () => {
    await setCurrentUserStatus(UserStatus.OFFLINE);
  };
  
  // 清理函数
  const cleanup = () => {
    clearInterval(cleanupInterval);
    userStatusMap.value.clear();
  };
  
  return {
    currentUserStatus,
    userStatusMap,
    UserStatus,
    getStatusText,
    getStatusColor,
    getUserStatus,
    getUserStatusInfo,
    getOnlineUsers,
    onlineUserCount,
    setCurrentUserStatus,
    registerUserStatusHandler,
    initializeUserStatus,
    setUserOffline,
    cleanup
  };
}