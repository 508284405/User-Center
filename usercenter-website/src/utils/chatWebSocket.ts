// @ts-ignore
import SockJS from 'sockjs-client';
import { Client, Frame, Message } from '@stomp/stompjs';
import { ref } from 'vue';
import { ElMessage } from 'element-plus';

// WebSocket消息类型
export interface ChatMessage {
  msgId?: string;
  sessionId?: string;
  groupId?: number;  // 群聊消息的群组ID
  content: string;
  messageType: string;
  fromUserId: string;
  fromUserType?: string;
  toUserId?: string;
  chatType?: string;  // 'User', 'GROUP' 等
  createTime?: number;
}

// 确认消息类型
export interface AckMessage {
  originalMsgId?: string;
  sessionId?: string;
  status: string;
  errorCode?: string;
  errorMessage?: string;
}

// 撤回消息类型
export interface RecallMessage {
  msgId: string;
  sessionId?: string;
  userId?: string;
  reason?: string;
  recallTime?: number;
  type: 'RECALL';
}

// 撤回通知消息类型
export interface RecallNotification {
  msgId: string;
  sessionId?: string;
  userId: string;
  reason?: string;
  recallTime: number;
  type: 'RECALL';
}

// 输入状态消息类型
export interface TypingMessage {
  sessionId: string;
  userId: string;
  userType?: string;
  isTyping: boolean;
  timestamp: number;
  type: 'TYPING';
}

// 输入状态通知类型
export interface TypingStatusNotification {
  sessionId: string;
  userId: string;
  userName?: string;
  isTyping: boolean;
  timestamp: number;
  type: 'TYPING_STATUS';
}

// 用户状态枚举
export enum UserStatus {
  ONLINE = 'ONLINE',
  OFFLINE = 'OFFLINE',
  BUSY = 'BUSY',
  AWAY = 'AWAY',
  INVISIBLE = 'INVISIBLE'
}

// 用户状态消息类型
export interface UserStatusMessage {
  userId: string;
  status: UserStatus;
  timestamp: number;
  type: 'USER_STATUS';
}

// 用户状态通知类型
export interface UserStatusNotification {
  userId: string;
  userName?: string;
  status: UserStatus;
  timestamp: number;
  type: 'USER_STATUS_NOTIFICATION';
}

// 删除消息类型
export interface DeleteMessage {
  msgId: string;
  sessionId: string;
  deleteType: number; // 0-仅自己可见删除 1-双方删除
  reason?: string;
  type: 'DELETE_MESSAGE';
}

// 编辑消息类型
export interface EditMessage {
  msgId: string;
  sessionId: string;
  newContent: string;
  editedAt?: number;
  type: 'EDIT_MESSAGE';
}

// 已读回执消息类型
export interface ReadReceiptMessage {
  msgId: string;
  sessionId: string;
  userId?: string;
  readAt?: number;
  type: 'READ_RECEIPT';
}

// 消息状态更新类型
export interface MessageStatusUpdate {
  msgId: string;
  sessionId: string;
  sendStatus: number;
  statusText?: string;
  statusIcon?: string;
  failReason?: string;
  retryCount?: number;
  canRetry?: boolean;
  updatedAt?: number;
  type: 'MESSAGE_STATUS_UPDATE';
}

// 回复消息类型
export interface ReplyMessage extends ChatMessage {
  replyToMsgId: string;
  quotedContent?: string;
  quotedFromUser?: string;
  type: 'REPLY_MESSAGE';
}

// 表情反应消息类型
export interface ReactionMessage {
  msgId: string;
  sessionId: string;
  userId: string;
  emoji: string;
  name: string;
  action: 'add' | 'remove';
  timestamp: number;
  type: 'REACTION_UPDATE';
  reactions?: ReactionSummary[];
}

// 反应统计摘要
export interface ReactionSummary {
  emoji: string;
  name: string;
  count: number;
  userIds: string[];
}

// 通用WebSocket消息类型
export type WebSocketMessage = ChatMessage | AckMessage | RecallMessage | RecallNotification | TypingMessage | TypingStatusNotification | UserStatusMessage | UserStatusNotification | DeleteMessage | EditMessage | ReadReceiptMessage | MessageStatusUpdate | ReplyMessage | ReactionMessage;

// 消息处理回调函数类型
export type MessageHandler = (message: any) => void;

// WebSocket连接状态
export const connectionStatus = ref<'disconnected' | 'connecting' | 'connected'>('disconnected');

// 离线消息存储
export const offlineMessages = ref<ChatMessage[]>([]);

// 未读消息计数
export const unreadCounts = ref<Map<string, number>>(new Map());

// 存储回调函数的映射
const messageHandlers: Map<string, MessageHandler[]> = new Map();

// Stomp客户端
let stompClient: Client | null = null;

// 用户信息
let currentUserId: string | null = null;
let userType: string | null = null;

// 初始化WebSocket连接
export const initWebSocketConnection = (userId: string, type: string) => {
  console.log('[WebSocket] 初始化连接参数:', { userId, type });
  try {
    // 只保留 SockJS + STOMP 连接，后端仅支持 STOMP over WebSocket
    const sockjsUrl = `http://localhost:8082/smartcs/ws/agent?token=Bearer ${localStorage.getItem('token')}`;
    console.log('[SockJS] 连接地址:', sockjsUrl);
    console.log('[SockJS] 连接参数:', {
      userId,
      userType: type,
      token: localStorage.getItem('token')
    });
    // 创建SockJS实例
    let socket;
    try {
      socket = new SockJS(sockjsUrl);
      console.log('[SockJS] SockJS实例创建成功');
    } catch (e) {
      console.error('[SockJS] SockJS实例创建失败:', e);
      throw e;
    }

    // 创建Stomp客户端
    try {
      stompClient = new Client({
        webSocketFactory: () => socket,
        connectHeaders: {
          userId: userId,
          userType: type,
          Authorization: `Bearer ${localStorage.getItem('token')}`
        },
        debug: function(str) {
          console.log('[STOMP DEBUG]:', str);
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000
      });
      console.log('[STOMP] Stomp客户端创建成功');
    } catch (e) {
      console.error('[STOMP] Stomp客户端创建失败:', e);
      throw e;
    }

    // 连接成功回调
    stompClient.onConnect = (frame: Frame) => {
      connectionStatus.value = 'connected';
      console.log('[STOMP] 连接成功', { frame, time: new Date().toISOString() });
      
      // 重置重连计数
      reconnectAttempts = 0;
      stopReconnect();
      // 订阅个人消息
      stompClient?.subscribe('/user/queue/messages', (message: Message) => {
        console.log('[STOMP] 收到个人消息:', message);
        handleMessage(message, 'message');
      });
      // 订阅确认消息
      stompClient?.subscribe('/user/queue/reply', (message: Message) => {
        console.log('[STOMP] 收到确认消息:', message);
        handleMessage(message, 'ack');
      });
      // 订阅心跳响应
      stompClient?.subscribe('/user/queue/heartbeat', (message: Message) => {
        console.log('[STOMP] 收到心跳消息:', message);
        handleMessage(message, 'heartbeat');
      });
      // 订阅撤回消息通知
      stompClient?.subscribe('/user/queue/recall', (message: Message) => {
        console.log('[STOMP] 收到撤回通知:', message);
        handleMessage(message, 'recall');
      });
      // 订阅输入状态通知
      stompClient?.subscribe('/user/queue/typing', (message: Message) => {
        console.log('[STOMP] 收到输入状态通知:', message);
        handleMessage(message, 'typing');
      });
      // 订阅用户状态通知
      stompClient?.subscribe('/user/queue/userStatus', (message: Message) => {
        console.log('[STOMP] 收到用户状态通知:', message);
        handleMessage(message, 'userStatus');
      });
      
      // 订阅消息状态更新
      stompClient?.subscribe('/user/queue/status', (message: Message) => {
        console.log('[STOMP] 收到消息状态更新:', message);
        handleMessage(message, 'messageStatus');
      });
      
      // 订阅已读回执
      stompClient?.subscribe('/user/queue/readReceipts', (message: Message) => {
        console.log('[STOMP] 收到已读回执:', message);
        handleMessage(message, 'readReceipt');
      });
      
      // 订阅表情反应通知
      stompClient?.subscribe('/user/queue/reactions', (message: Message) => {
        console.log('[STOMP] 收到表情反应通知:', message);
        handleMessage(message, 'reaction');
      });
      // 发送心跳消息
      startHeartbeat();
      
      // 连接成功后恢复离线消息和未读计数
      recoverOfflineData();
    };

    // 连接错误回调
    stompClient.onStompError = (frame: Frame) => {
      console.error('[STOMP] 连接错误:', frame, { time: new Date().toISOString() });
      connectionStatus.value = 'disconnected';
      ElMessage.error('WebSocket连接错误');
    };

    // 断开/关闭回调
    stompClient.onDisconnect = (frame?: Frame) => {
      connectionStatus.value = 'disconnected';
      console.log('[STOMP] 连接断开', { frame, time: new Date().toISOString() });
      // 启动自动重连
      startReconnect();
    };
    stompClient.onWebSocketClose = (event: CloseEvent) => {
      connectionStatus.value = 'disconnected';
      console.log('[STOMP] WebSocket底层关闭', { event, time: new Date().toISOString() });
      // 启动自动重连
      startReconnect();
    };
    stompClient.onWebSocketError = (event: Event) => {
      connectionStatus.value = 'disconnected';
      console.error('[STOMP] WebSocket底层出错', { event, time: new Date().toISOString() });
      // 启动自动重连
      startReconnect();
    };

    // 连接
    try {
      console.log('[STOMP] 尝试激活连接...');
      stompClient.activate();
    } catch (e) {
      console.error('[STOMP] 激活连接异常:', e);
      throw e;
    }

    // 保存用户信息
    currentUserId = userId;
    userType = type;
    // 如果已经连接，先断开连接
    if (stompClient && connectionStatus.value === 'connected') {
      disconnect();
    }
    // 设置连接状态
    connectionStatus.value = 'connecting';
  } catch (err) {
    console.error('[WebSocket] 初始化异常:', err);
  }
};

// 处理接收到的消息
const handleMessage = (message: Message, type: string) => {
  try {
    const parsedMessage = JSON.parse(message.body);
    console.log(`收到WebSocket ${type}消息:`, parsedMessage);
    
    // 如果是聊天消息，自动更新未读计数
    if (type === 'message' && parsedMessage.sessionId) {
      updateUnreadCount(parsedMessage.sessionId, true);
    }
    
    // 调用注册的回调函数
    const handlers = messageHandlers.get(type) || [];
    handlers.forEach(handler => handler(parsedMessage));
  } catch (error) {
    console.error('处理WebSocket消息出错:', error);
  }
};

// 注册消息处理回调
export const registerMessageHandler = (type: string, handler: MessageHandler) => {
  const handlers = messageHandlers.get(type) || [];
  handlers.push(handler);
  messageHandlers.set(type, handlers);
  
  return () => {
    // 返回取消注册的函数
    const index = handlers.indexOf(handler);
    if (index !== -1) {
      handlers.splice(index, 1);
      messageHandlers.set(type, handlers);
    }
  };
};

// 发送私聊消息
export const sendChatMessage = (message: ChatMessage) => {
  if (!stompClient || connectionStatus.value !== 'connected') {
    ElMessage.error('WebSocket未连接');
    return false;
  }
  
  try {
    stompClient.publish({
      destination: '/app/chat.sendMessage',
      body: JSON.stringify(message)
    });
    return true;
  } catch (error) {
    console.error('发送WebSocket消息出错:', error);
    return false;
  }
};

// 发送群聊消息
export const sendGroupMessage = (message: ChatMessage) => {
  if (!stompClient || connectionStatus.value !== 'connected') {
    ElMessage.error('WebSocket未连接');
    return false;
  }
  
  if (!message.groupId) {
    console.error('群聊消息必须包含groupId');
    return false;
  }
  
  try {
    // 设置群聊消息类型
    message.chatType = 'GROUP';
    
    stompClient.publish({
      destination: '/app/chat.sendGroupMessage',
      body: JSON.stringify(message)
    });
    return true;
  } catch (error) {
    console.error('发送群聊消息出错:', error);
    return false;
  }
};

// 发送确认消息
export const sendAck = (ackMessage: AckMessage) => {
  if (!stompClient || connectionStatus.value !== 'connected') {
    return false;
  }

  // 确保 type 字段为 'ACK'
  const ackMsgWithType = { ...ackMessage, type: 'ACK' };

  try {
    stompClient.publish({
      destination: '/app/chat.ack',
      body: JSON.stringify(ackMsgWithType)
    });
    return true;
  } catch (error) {
    console.error('发送确认消息出错:', error);
    return false;
  }
};

// 发送撤回消息
export const sendRecallMessage = (recallMessage: RecallMessage) => {
  if (!stompClient || connectionStatus.value !== 'connected') {
    ElMessage.error('WebSocket未连接');
    return false;
  }
  
  try {
    // 确保 type 字段为 'RECALL'
    const recallMsgWithType = { ...recallMessage, type: 'RECALL' };
    
    stompClient.publish({
      destination: '/app/chat.recallMessage',
      body: JSON.stringify(recallMsgWithType)
    });
    return true;
  } catch (error) {
    console.error('发送撤回消息出错:', error);
    ElMessage.error('撤回消息失败');
    return false;
  }
};

// 发送输入状态消息
export const sendTypingStatus = (typingMessage: TypingMessage) => {
  if (!stompClient || connectionStatus.value !== 'connected') {
    return false;
  }
  
  try {
    // 确保 type 字段为 'TYPING'
    const typingMsgWithType = { ...typingMessage, type: 'TYPING' };
    
    stompClient.publish({
      destination: '/app/chat.typing',
      body: JSON.stringify(typingMsgWithType)
    });
    return true;
  } catch (error) {
    console.error('发送输入状态消息出错:', error);
    return false;
  }
};

// 发送用户状态消息
export const sendUserStatus = (statusMessage: UserStatusMessage) => {
  if (!stompClient || connectionStatus.value !== 'connected') {
    return false;
  }
  
  try {
    // 确保 type 字段为 'USER_STATUS'
    const statusMsgWithType = { ...statusMessage, type: 'USER_STATUS' };
    
    stompClient.publish({
      destination: '/app/chat.userStatus',
      body: JSON.stringify(statusMsgWithType)
    });
    return true;
  } catch (error) {
    console.error('发送用户状态消息出错:', error);
    return false;
  }
};

// 发送删除消息
export const sendDeleteMessage = (deleteMessage: DeleteMessage) => {
  if (!stompClient || connectionStatus.value !== 'connected') {
    ElMessage.error('WebSocket未连接');
    return false;
  }
  
  try {
    const deleteMsgWithType = { ...deleteMessage, type: 'DELETE_MESSAGE' };
    
    stompClient.publish({
      destination: '/app/chat.deleteMessage',
      body: JSON.stringify(deleteMsgWithType)
    });
    return true;
  } catch (error) {
    console.error('发送删除消息出错:', error);
    ElMessage.error('删除消息失败');
    return false;
  }
};

// 发送编辑消息
export const sendEditMessage = (editMessage: EditMessage) => {
  if (!stompClient || connectionStatus.value !== 'connected') {
    ElMessage.error('WebSocket未连接');
    return false;
  }
  
  try {
    const editMsgWithType = { ...editMessage, type: 'EDIT_MESSAGE' };
    
    stompClient.publish({
      destination: '/app/chat.editMessage',
      body: JSON.stringify(editMsgWithType)
    });
    return true;
  } catch (error) {
    console.error('发送编辑消息出错:', error);
    ElMessage.error('编辑消息失败');
    return false;
  }
};

// 发送已读回执
export const sendReadReceipt = (readReceiptMessage: ReadReceiptMessage) => {
  if (!stompClient || connectionStatus.value !== 'connected') {
    return false;
  }
  
  try {
    const readMsgWithType = { ...readReceiptMessage, type: 'READ_RECEIPT' };
    
    stompClient.publish({
      destination: '/app/chat.markRead',
      body: JSON.stringify(readMsgWithType)
    });
    return true;
  } catch (error) {
    console.error('发送已读回执出错:', error);
    return false;
  }
};

// 发送重试消息
export const sendRetryMessage = (msgId: string, sessionId: string) => {
  if (!stompClient || connectionStatus.value !== 'connected') {
    ElMessage.error('WebSocket未连接');
    return false;
  }
  
  try {
    const retryMessage: MessageStatusUpdate = {
      msgId,
      sessionId,
      sendStatus: 0, // SENDING
      type: 'MESSAGE_STATUS_UPDATE'
    };
    
    stompClient.publish({
      destination: '/app/chat.retryMessage',
      body: JSON.stringify(retryMessage)
    });
    return true;
  } catch (error) {
    console.error('发送重试消息出错:', error);
    ElMessage.error('重试发送失败');
    return false;
  }
};

// 发送回复消息
export const sendReplyMessage = (replyMessage: ReplyMessage) => {
  if (!stompClient || connectionStatus.value !== 'connected') {
    ElMessage.error('WebSocket未连接');
    return false;
  }
  
  try {
    const replyMsgWithType = { ...replyMessage, type: 'REPLY_MESSAGE' };
    
    stompClient.publish({
      destination: '/app/chat.sendMessage',
      body: JSON.stringify(replyMsgWithType)
    });
    return true;
  } catch (error) {
    console.error('发送回复消息出错:', error);
    ElMessage.error('发送回复失败');
    return false;
  }
};

// 转发消息接口定义
export interface ForwardMessage {
  sessionId: string;
  fromUserId: string;
  content: string;
  originalMsgId?: string;
  targetSessionIds?: string[];
  type?: string;
}

export const sendForwardMessage = (forwardMessage: ForwardMessage) => {
  if (!stompClient || connectionStatus.value !== 'connected') {
    ElMessage.error('WebSocket未连接');
    return false;
  }

  try {
    const forwardMsgWithType = { ...forwardMessage, type: 'FORWARD_MESSAGE' };
    stompClient.publish({
      destination: '/app/chat.forwardMessage',
      body: JSON.stringify(forwardMsgWithType)
    });
    console.log('[WebSocket] 发送转发消息:', forwardMsgWithType);
    return true;
  } catch (error) {
    console.error('[WebSocket] 发送转发消息失败:', error);
    ElMessage.error('发送转发消息失败');
    return false;
  }
};

// 发送表情反应消息
export const sendAddReaction = (reactionMessage: ReactionMessage) => {
  if (!stompClient || connectionStatus.value !== 'connected') {
    ElMessage.error('WebSocket未连接');
    return false;
  }
  
  try {
    const reactionMsgWithType = { ...reactionMessage, type: 'REACTION_UPDATE' };
    
    stompClient.publish({
      destination: '/app/chat.reaction',
      body: JSON.stringify(reactionMsgWithType)
    });
    
    console.log('[WebSocket] 发送表情反应消息:', reactionMsgWithType);
    return true;
  } catch (error) {
    console.error('[WebSocket] 发送表情反应消息失败:', error);
    ElMessage.error('发送表情反应失败');
    return false;
  }
};

// 断开连接
export const disconnect = () => {
  if (stompClient) {
    stompClient.deactivate();
    stompClient = null;
  }
  connectionStatus.value = 'disconnected';
  console.log('WebSocket连接已断开');
};

// 心跳计时器
let heartbeatTimer: number | null = null;

// 重连相关
let reconnectTimer: number | null = null;
let reconnectAttempts: number = 0;
const maxReconnectAttempts: number = 10;
const reconnectBaseDelay: number = 1000; // 1秒基础延迟

// 开始发送心跳
const startHeartbeat = () => {
  // 先清除之前的定时器
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer);
  }
  
  // 每30秒发送一次心跳
  heartbeatTimer = window.setInterval(() => {
    if (stompClient && connectionStatus.value === 'connected') {
      stompClient.publish({
        destination: '/app/chat.heartbeat',
        body: JSON.stringify({ type: 'HEARTBEAT' })
      });
    }
  }, 30000);
};

// 恢复离线数据
const recoverOfflineData = async () => {
  if (!currentUserId) return;
  
  try {
    // 恢复离线消息
    await recoverOfflineMessages();
    
    // 恢复未读计数
    await recoverUnreadCounts();
    
    console.log('[WebSocket] 离线数据恢复完成');
  } catch (error) {
    console.error('[WebSocket] 离线数据恢复失败:', error);
  }
};

// 恢复离线消息
const recoverOfflineMessages = async () => {
  try {
    const response = await fetch(`/api/offline-messages?receiverId=${currentUserId}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (response.ok) {
      const messages = await response.json();
      offlineMessages.value = messages;
      
      // 触发离线消息处理回调
      const handlers = messageHandlers.get('offline') || [];
      handlers.forEach(handler => handler(messages));
      
      console.log(`[WebSocket] 恢复了 ${messages.length} 条离线消息`);
    }
  } catch (error) {
    console.error('[WebSocket] 恢复离线消息失败:', error);
  }
};

// 恢复未读计数
const recoverUnreadCounts = async () => {
  try {
    const response = await fetch(`/api/unread-counts?userId=${currentUserId}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (response.ok) {
      const counts = await response.json();
      const countsMap = new Map();
      Object.entries(counts).forEach(([conversationId, count]) => {
        countsMap.set(conversationId, count as number);
      });
      unreadCounts.value = countsMap;
      
      console.log('[WebSocket] 恢复未读计数:', counts);
    }
  } catch (error) {
    console.error('[WebSocket] 恢复未读计数失败:', error);
  }
};

// 标记离线消息为已读
export const markOfflineMessagesAsRead = async (conversationId: string) => {
  if (!currentUserId) return false;
  
  try {
    const response = await fetch('/api/offline-messages/mark-read', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        receiverId: currentUserId,
        conversationId: conversationId
      })
    });
    
    if (response.ok) {
      // 移除已读的离线消息
      offlineMessages.value = offlineMessages.value.filter(
        msg => msg.sessionId !== conversationId
      );
      
      // 清除未读计数
      unreadCounts.value.delete(conversationId);
      
      console.log(`[WebSocket] 标记会话 ${conversationId} 离线消息为已读`);
      return true;
    }
    
    return false;
  } catch (error) {
    console.error('[WebSocket] 标记离线消息已读失败:', error);
    return false;
  }
};

// 更新未读计数
export const updateUnreadCount = (conversationId: string, increment: boolean = true) => {
  const currentCount = unreadCounts.value.get(conversationId) || 0;
  const newCount = increment ? currentCount + 1 : Math.max(0, currentCount - 1);
  
  if (newCount === 0) {
    unreadCounts.value.delete(conversationId);
  } else {
    unreadCounts.value.set(conversationId, newCount);
  }
  
  console.log(`[WebSocket] 更新未读计数: ${conversationId} = ${newCount}`);
};

// 获取总未读消息数
export const getTotalUnreadCount = (): number => {
  let total = 0;
  unreadCounts.value.forEach(count => total += count);
  return total;
};

// 获取会话未读计数
export const getConversationUnreadCount = (conversationId: string): number => {
  return unreadCounts.value.get(conversationId) || 0;
};

// 启动重连
const startReconnect = () => {
  if (reconnectAttempts >= maxReconnectAttempts) {
    console.error('[WebSocket] 达到最大重连次数，停止重连');
    ElMessage.error('WebSocket连接失败，请刷新页面重试');
    return;
  }
  
  if (reconnectTimer) {
    return; // 已有重连任务
  }
  
  const delay = Math.min(reconnectBaseDelay * Math.pow(2, reconnectAttempts), 30000); // 最大30秒
  console.log(`[WebSocket] ${delay}ms 后进行第 ${reconnectAttempts + 1} 次重连`);
  
  reconnectTimer = window.setTimeout(() => {
    reconnectAttempts++;
    reconnectTimer = null;
    
    if (currentUserId && userType) {
      console.log(`[WebSocket] 第 ${reconnectAttempts} 次重连尝试`);
      initWebSocketConnection(currentUserId, userType);
    }
  }, delay);
};

// 停止重连
const stopReconnect = () => {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }
};

// 手动重连
export const reconnect = () => {
  console.log('[WebSocket] 手动重连');
  reconnectAttempts = 0;
  stopReconnect();
  
  if (currentUserId && userType) {
    disconnect();
    initWebSocketConnection(currentUserId, userType);
  }
};

// 获取连接状态信息
export const getConnectionInfo = () => {
  return {
    status: connectionStatus.value,
    reconnectAttempts,
    userId: currentUserId,
    userType,
    totalUnreadCount: getTotalUnreadCount(),
    offlineMessageCount: offlineMessages.value.length
  };
};

// 组件卸载时清理
export const cleanup = () => {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer);
    heartbeatTimer = null;
  }
  stopReconnect();
  disconnect();
  offlineMessages.value = [];
  unreadCounts.value.clear();
};
