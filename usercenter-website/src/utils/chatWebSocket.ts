// @ts-ignore
import SockJS from 'sockjs-client';
import { Client, Frame, Message } from '@stomp/stompjs';
import { ref } from 'vue';
import { ElMessage } from 'element-plus';

// WebSocket消息类型
export interface ChatMessage {
  msgId?: string;
  sessionId: string;
  content: string;
  messageType: string;
  fromUserId: string;
  fromUserType?: string;
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

// 通用WebSocket消息类型
export type WebSocketMessage = ChatMessage | AckMessage;

// 消息处理回调函数类型
export type MessageHandler = (message: any) => void;

// WebSocket连接状态
export const connectionStatus = ref<'disconnected' | 'connecting' | 'connected'>('disconnected');

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
      // 发送心跳消息
      startHeartbeat();
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
    };
    stompClient.onWebSocketClose = (event: CloseEvent) => {
      connectionStatus.value = 'disconnected';
      console.log('[STOMP] WebSocket底层关闭', { event, time: new Date().toISOString() });
    };
    stompClient.onWebSocketError = (event: Event) => {
      connectionStatus.value = 'disconnected';
      console.error('[STOMP] WebSocket底层出错', { event, time: new Date().toISOString() });
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

// 发送消息
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

// 组件卸载时清理
export const cleanup = () => {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer);
    heartbeatTimer = null;
  }
  disconnect();
};
