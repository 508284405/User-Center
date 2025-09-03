import { ref, computed } from 'vue';
import { sendTypingStatus, registerMessageHandler, TypingMessage, TypingStatusNotification } from '@/utils/chatWebSocket';
import { debounce } from '@/utils/chat';

/**
 * 输入状态管理的组合式函数
 */
export function useTyping(sessionId?: string) {
  // 当前会话正在输入的用户列表
  const typingUsers = ref<Map<string, { userId: string; userName?: string; timestamp: number }>>(new Map());
  
  // 输入状态发送防抖定时器
  let typingTimer: number | null = null;
  
  // 计算当前有哪些用户正在输入
  const typingUsersList = computed(() => {
    const users = Array.from(typingUsers.value.values());
    // 过滤掉超时的用户（5秒内没有更新状态）
    const now = Date.now();
    return users.filter(user => now - user.timestamp < 5000);
  });
  
  // 格式化输入提示文本
  const typingText = computed(() => {
    const users = typingUsersList.value;
    if (users.length === 0) return '';
    
    if (users.length === 1) {
      const userName = users[0].userName || `用户${users[0].userId}`;
      return `${userName} 正在输入...`;
    } else if (users.length <= 3) {
      const names = users.map(u => u.userName || `用户${u.userId}`);
      return `${names.join('、')} 正在输入...`;
    } else {
      return `${users.length} 个用户正在输入...`;
    }
  });
  
  // 是否有用户正在输入
  const hasTypingUsers = computed(() => typingUsersList.value.length > 0);
  
  // 开始输入
  const startTyping = (currentSessionId: string) => {
    if (!currentSessionId) return;
    
    const userId = localStorage.getItem('userId') || '';
    if (!userId) return;
    
    // 发送开始输入状态
    const typingMsg: TypingMessage = {
      sessionId: currentSessionId,
      userId,
      userType: 'USER',
      isTyping: true,
      timestamp: Date.now(),
      type: 'TYPING'
    };
    
    sendTypingStatus(typingMsg);
    
    // 清除之前的定时器
    if (typingTimer) {
      clearTimeout(typingTimer);
    }
    
    // 设置2秒后自动发送停止输入状态
    typingTimer = window.setTimeout(() => {
      stopTyping(currentSessionId);
    }, 2000);
  };
  
  // 停止输入
  const stopTyping = (currentSessionId: string) => {
    if (!currentSessionId) return;
    
    const userId = localStorage.getItem('userId') || '';
    if (!userId) return;
    
    // 清除定时器
    if (typingTimer) {
      clearTimeout(typingTimer);
      typingTimer = null;
    }
    
    // 发送停止输入状态
    const typingMsg: TypingMessage = {
      sessionId: currentSessionId,
      userId,
      userType: 'USER',
      isTyping: false,
      timestamp: Date.now(),
      type: 'TYPING'
    };
    
    sendTypingStatus(typingMsg);
  };
  
  // 防抖的开始输入函数
  const debouncedStartTyping = debounce((currentSessionId: string) => {
    startTyping(currentSessionId);
  }, 300);
  
  // 处理输入事件
  const handleInputEvent = (currentSessionId: string) => {
    if (!currentSessionId) return;
    debouncedStartTyping(currentSessionId);
  };
  
  // 处理输入状态通知
  const handleTypingNotification = (notification: TypingStatusNotification) => {
    const currentUserId = localStorage.getItem('userId') || '';
    
    // 忽略自己的输入状态
    if (notification.userId === currentUserId) return;
    
    // 只处理当前会话的输入状态
    if (sessionId && notification.sessionId !== sessionId) return;
    
    if (notification.isTyping) {
      // 用户开始输入
      typingUsers.value.set(notification.userId, {
        userId: notification.userId,
        userName: notification.userName,
        timestamp: notification.timestamp
      });
    } else {
      // 用户停止输入
      typingUsers.value.delete(notification.userId);
    }
  };
  
  // 清理过期的输入状态
  const cleanupExpiredTyping = () => {
    const now = Date.now();
    const expiredUsers: string[] = [];
    
    typingUsers.value.forEach((user, userId) => {
      if (now - user.timestamp > 5000) { // 5秒超时
        expiredUsers.push(userId);
      }
    });
    
    expiredUsers.forEach(userId => {
      typingUsers.value.delete(userId);
    });
  };
  
  // 启动清理定时器
  const cleanupInterval = setInterval(cleanupExpiredTyping, 1000);
  
  // 注册输入状态处理器
  const registerTypingHandler = () => {
    return registerMessageHandler('typing', handleTypingNotification);
  };
  
  // 清理函数
  const cleanup = () => {
    if (typingTimer) {
      clearTimeout(typingTimer);
      typingTimer = null;
    }
    clearInterval(cleanupInterval);
    typingUsers.value.clear();
  };
  
  return {
    typingUsersList,
    typingText,
    hasTypingUsers,
    startTyping,
    stopTyping,
    handleInputEvent,
    registerTypingHandler,
    cleanup
  };
}