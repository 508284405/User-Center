import { Message, MessageType, MessageStatus, Session } from '@/types/chat';

/**
 * 生成唯一ID
 */
export function generateId(): string {
  return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
}

/**
 * 格式化时间戳
 */
export function formatTimestamp(timestamp: number): string {
  const date = new Date(timestamp);
  const now = new Date();
  const diff = now.getTime() - timestamp;
  
  // 一分钟内
  if (diff < 60000) {
    return '刚刚';
  }
  
  // 一小时内
  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`;
  }
  
  // 当天
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
  }
  
  // 昨天
  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  if (date.toDateString() === yesterday.toDateString()) {
    return `昨天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`;
  }
  
  // 一周内
  if (diff < 7 * 24 * 3600000) {
    const weekdays = ['日', '一', '二', '三', '四', '五', '六'];
    return `周${weekdays[date.getDay()]} ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`;
  }
  
  // 超过一周
  return date.toLocaleDateString('zh-CN', { 
    month: '2-digit', 
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
}

/**
 * 格式化时间戳为-拼接形式 (YYYY-MM-DD-HH-mm-ss)
 */
export function formatTimestampWithDash(timestamp: number): string {
  const date = new Date(timestamp);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  
  return `${year}-${month}-${day}-${hours}-${minutes}-${seconds}`;
}

/**
 * 创建用户消息
 */
export function createUserMessage(content: string, sessionId: string): Message {
  // 获取当前用户ID
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
  const userId = userInfo.id?.toString() || '';
  
  return {
    id: generateId(),
    sessionId,
    content,
    type: MessageType.USER,
    status: MessageStatus.SENDING,
    timestamp: Date.now(),
    fromUserId: userId
  };
}

/**
 * 创建助手消息
 */
export function createAssistantMessage(content: string, sessionId: string): Message {
  return {
    id: generateId(),
    sessionId,
    content,
    type: MessageType.ASSISTANT,
    status: MessageStatus.RECEIVED,
    timestamp: Date.now(),
    thinkingContent: '',
    isThinkingExpanded: false
  };
}

/**
 * 复制文本到剪贴板
 */
export async function copyToClipboard(text: string): Promise<boolean> {
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(text);
      return true;
    } else {
      // Fallback for older browsers
      const textArea = document.createElement('textarea');
      textArea.value = text;
      textArea.style.position = 'fixed';
      textArea.style.left = '-999999px';
      textArea.style.top = '-999999px';
      document.body.appendChild(textArea);
      textArea.focus();
      textArea.select();
      const result = document.execCommand('copy');
      textArea.remove();
      return result;
    }
  } catch (error) {
    console.error('复制失败:', error);
    return false;
  }
}

/**
 * 本地存储键名
 */
const STORAGE_KEYS = {
  SESSIONS: 'chat_sessions',
  MESSAGES: 'chat_messages',
  CONFIG: 'chat_config'
};

/**
 * 保存会话到本地存储
 */
export function saveSessionsToStorage(sessions: Session[]): void {
  try {
    localStorage.setItem(STORAGE_KEYS.SESSIONS, JSON.stringify(sessions));
  } catch (error) {
    console.error('保存会话到本地存储失败:', error);
  }
}

/**
 * 从本地存储加载会话
 */
export function loadSessionsFromStorage(): Session[] {
  try {
    const data = localStorage.getItem(STORAGE_KEYS.SESSIONS);
    return data ? JSON.parse(data) : [];
  } catch (error) {
    console.error('从本地存储加载会话失败:', error);
    return [];
  }
}

/**
 * 保存消息到本地存储
 */
export function saveMessagesToStorage(sessionId: string, messages: Message[]): void {
  try {
    const allMessages = loadAllMessagesFromStorage();
    allMessages[sessionId] = messages;
    localStorage.setItem(STORAGE_KEYS.MESSAGES, JSON.stringify(allMessages));
  } catch (error) {
    console.error('保存消息到本地存储失败:', error);
  }
}

/**
 * 从本地存储加载指定会话的消息
 */
export function loadMessagesFromStorage(sessionId: string): Message[] {
  try {
    const allMessages = loadAllMessagesFromStorage();
    return allMessages[sessionId] || [];
  } catch (error) {
    console.error('从本地存储加载消息失败:', error);
    return [];
  }
}

/**
 * 从本地存储加载所有消息
 */
function loadAllMessagesFromStorage(): Record<string, Message[]> {
  try {
    const data = localStorage.getItem(STORAGE_KEYS.MESSAGES);
    return data ? JSON.parse(data) : {};
  } catch (error) {
    console.error('从本地存储加载所有消息失败:', error);
    return {};
  }
}

/**
 * 清理本地存储
 */
export function clearStorage(): void {
  try {
    localStorage.removeItem(STORAGE_KEYS.SESSIONS);
    localStorage.removeItem(STORAGE_KEYS.MESSAGES);
    localStorage.removeItem(STORAGE_KEYS.CONFIG);
  } catch (error) {
    console.error('清理本地存储失败:', error);
  }
}

/**
 * 滚动到页面底部
 */
export function scrollToBottom(element?: Element): void {
  const container = element || document.documentElement;
  container.scrollTop = container.scrollHeight;
}

/**
 * 防抖函数
 */
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: number;
  return (...args: Parameters<T>) => {
    clearTimeout(timeout);
    timeout = setTimeout(() => func.apply(null, args), wait);
  };
}

/**
 * 节流函数
 */
export function throttle<T extends (...args: any[]) => any>(
  func: T,
  limit: number
): (...args: Parameters<T>) => void {
  let inThrottle: boolean;
  return (...args: Parameters<T>) => {
    if (!inThrottle) {
      func.apply(null, args);
      inThrottle = true;
      setTimeout(() => inThrottle = false, limit);
    }
  };
}

/**
 * 检查消息是否可以撤回
 */
export function canRecallMessage(message: Message, currentUserId: string): boolean {
  // 已撤回的消息不能再次撤回
  if (message.isRecalled) {
    return false;
  }
  
  // 只有发送者可以撤回自己的消息
  if (message.fromUserId !== currentUserId && message.type === MessageType.USER) {
    return false;
  }
  
  // 检查时间限制 (2分钟内可撤回)
  const currentTime = Date.now();
  const messageTime = message.timestamp;
  const timeDiff = currentTime - messageTime;
  
  return timeDiff <= 120_000; // 120秒 = 2分钟
}

/**
 * 获取撤回时间限制剩余时间（秒）
 */
export function getRecallTimeRemaining(message: Message): number {
  const currentTime = Date.now();
  const messageTime = message.timestamp;
  const timeDiff = currentTime - messageTime;
  const timeLimit = 120_000; // 2分钟
  
  if (timeDiff >= timeLimit) {
    return 0;
  }
  
  return Math.ceil((timeLimit - timeDiff) / 1000);
}

/**
 * 处理消息撤回状态更新
 */
export function updateMessageRecallStatus(message: Message, recallData: {
  isRecalled: boolean;
  recalledAt?: number;
  recalledBy?: string;
  recallReason?: string;
}): Message {
  return {
    ...message,
    isRecalled: recallData.isRecalled,
    recalledAt: recallData.recalledAt,
    recalledBy: recallData.recalledBy,
    recallReason: recallData.recallReason,
    // 撤回后清空原内容，显示撤回提示
    content: recallData.isRecalled ? '[消息已撤回]' : message.content
  };
} 