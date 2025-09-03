<template>
  <div class="chat-example">
    <div class="connection-status">
      <div class="status-indicator" :class="connectionStatus">
        {{ connectionStatusText }}
      </div>
      <div class="stats">
        <span>总未读: {{ totalUnreadCount }}</span>
        <span>离线消息: {{ offlineMessageCount }}</span>
      </div>
      <button v-if="connectionStatus === 'disconnected'" @click="handleReconnect">
        重新连接
      </button>
    </div>

    <div class="conversation-list">
      <h3>会话列表</h3>
      <div
        v-for="conversation in conversations"
        :key="conversation.id"
        class="conversation-item"
        :class="{ active: activeConversation === conversation.id }"
        @click="selectConversation(conversation.id)"
      >
        <span class="name">{{ conversation.name }}</span>
        <span class="type">{{ conversation.type === 'GROUP' ? '群聊' : '私聊' }}</span>
        <span v-if="getUnreadCount(conversation.id) > 0" class="unread-badge">
          {{ getUnreadCount(conversation.id) }}
        </span>
      </div>
    </div>

    <div class="chat-area" v-if="activeConversation">
      <div class="messages">
        <div
          v-for="message in messages"
          :key="message.id"
          class="message"
          :class="{ own: message.isOwn }"
        >
          <div class="content">{{ message.content }}</div>
          <div class="time">{{ formatTime(message.time) }}</div>
        </div>
      </div>

      <div class="input-area">
        <input
          v-model="inputMessage"
          @keyup.enter="sendMessage"
          placeholder="输入消息..."
          class="message-input"
        />
        <button @click="sendMessage" :disabled="!canSend">发送</button>
      </div>
    </div>

    <div class="offline-messages" v-if="offlineMessages.length > 0">
      <h3>离线消息</h3>
      <div
        v-for="message in offlineMessages"
        :key="message.msgId"
        class="offline-message"
      >
        <div class="content">{{ message.content }}</div>
        <div class="from">来自: {{ message.fromUserId }}</div>
        <button @click="markAsRead(message.sessionId)">标记已读</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import {
  initWebSocketConnection,
  disconnect,
  connectionStatus,
  sendChatMessage,
  sendGroupMessage,
  registerMessageHandler,
  cleanup,
  offlineMessages,
  unreadCounts,
  getTotalUnreadCount,
  getConversationUnreadCount,
  markOfflineMessagesAsRead,
  updateUnreadCount,
  reconnect,
  getConnectionInfo
} from '../utils/chatWebSocket';

// 模拟数据
const conversations = ref([
  { id: 'conv_1', name: '客服小王', type: 'PRIVATE' },
  { id: 'conv_2', name: '技术支持群', type: 'GROUP', groupId: 101 },
  { id: 'conv_3', name: '产品讨论群', type: 'GROUP', groupId: 102 }
]);

const messages = ref([]);
const inputMessage = ref('');
const activeConversation = ref('');

// 计算属性
const connectionStatusText = computed(() => {
  switch (connectionStatus.value) {
    case 'connected': return '已连接';
    case 'connecting': return '连接中...';
    case 'disconnected': return '已断开';
    default: return '未知状态';
  }
});

const totalUnreadCount = computed(() => getTotalUnreadCount());
const offlineMessageCount = computed(() => offlineMessages.value.length);

const canSend = computed(() => 
  connectionStatus.value === 'connected' && 
  inputMessage.value.trim().length > 0 &&
  activeConversation.value
);

// 方法
const selectConversation = (conversationId: string) => {
  activeConversation.value = conversationId;
  
  // 标记为已读，清除未读计数
  updateUnreadCount(conversationId, false);
  
  // 模拟加载历史消息
  loadMessages(conversationId);
};

const loadMessages = (conversationId: string) => {
  // 这里应该从API加载历史消息
  // 现在只是模拟数据
  messages.value = [
    {
      id: 1,
      content: '你好！',
      time: Date.now() - 60000,
      isOwn: false
    },
    {
      id: 2,
      content: '有什么可以帮您的吗？',
      time: Date.now() - 30000,
      isOwn: true
    }
  ];
};

const sendMessage = () => {
  if (!canSend.value) return;
  
  const conversation = conversations.value.find(c => c.id === activeConversation.value);
  if (!conversation) return;
  
  const message = {
    msgId: `msg_${Date.now()}`,
    sessionId: activeConversation.value,
    content: inputMessage.value,
    messageType: 'TEXT',
    fromUserId: '12345', // 当前用户ID
    createTime: Date.now()
  };
  
  let success = false;
  
  if (conversation.type === 'GROUP') {
    // 发送群聊消息
    message.groupId = conversation.groupId;
    message.chatType = 'GROUP';
    success = sendGroupMessage(message);
  } else {
    // 发送私聊消息
    message.chatType = 'User';
    success = sendChatMessage(message);
  }
  
  if (success) {
    // 添加到本地消息列表
    messages.value.push({
      id: message.msgId,
      content: message.content,
      time: message.createTime,
      isOwn: true
    });
    
    inputMessage.value = '';
  }
};

const getUnreadCount = (conversationId: string) => {
  return getConversationUnreadCount(conversationId);
};

const markAsRead = async (conversationId: string) => {
  const success = await markOfflineMessagesAsRead(conversationId);
  if (success) {
    console.log('离线消息已标记为已读');
  }
};

const handleReconnect = () => {
  reconnect();
};

const formatTime = (timestamp: number) => {
  return new Date(timestamp).toLocaleTimeString();
};

// 消息处理器
const handleIncomingMessage = (message: any) => {
  console.log('收到新消息:', message);
  
  // 如果是当前会话的消息，添加到消息列表
  if (message.sessionId === activeConversation.value) {
    messages.value.push({
      id: message.msgId,
      content: message.content,
      time: message.createTime || Date.now(),
      isOwn: false
    });
  }
};

const handleOfflineMessages = (messages: any[]) => {
  console.log('收到离线消息:', messages);
  // 离线消息会自动存储在 offlineMessages 响应式变量中
};

// 生命周期
onMounted(() => {
  // 初始化WebSocket连接
  const userId = '12345'; // 应该从用户状态获取
  const userType = 'USER';
  
  initWebSocketConnection(userId, userType);
  
  // 注册消息处理器
  const unsubscribeMessage = registerMessageHandler('message', handleIncomingMessage);
  const unsubscribeOffline = registerMessageHandler('offline', handleOfflineMessages);
  
  // 组件销毁时取消订阅
  onUnmounted(() => {
    unsubscribeMessage();
    unsubscribeOffline();
    cleanup();
  });
});

// 监听连接状态变化
watch(connectionStatus, (newStatus) => {
  console.log('连接状态变化:', newStatus);
});
</script>

<style scoped>
.chat-example {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.connection-status {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px 15px;
  background: #f5f5f5;
  border-radius: 8px;
  margin-bottom: 20px;
}

.status-indicator {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  text-transform: uppercase;
}

.status-indicator.connected {
  background: #d4edda;
  color: #155724;
}

.status-indicator.connecting {
  background: #fff3cd;
  color: #856404;
}

.status-indicator.disconnected {
  background: #f8d7da;
  color: #721c24;
}

.stats {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: #666;
}

.conversation-list {
  margin-bottom: 30px;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 15px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  margin-bottom: 5px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.conversation-item:hover {
  background-color: #f9f9f9;
}

.conversation-item.active {
  background-color: #e3f2fd;
  border-color: #2196f3;
}

.conversation-item .name {
  font-weight: 500;
}

.conversation-item .type {
  font-size: 12px;
  color: #666;
  background: #e0e0e0;
  padding: 2px 6px;
  border-radius: 10px;
}

.unread-badge {
  background: #ff4444;
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
  margin-left: auto;
}

.chat-area {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 30px;
}

.messages {
  height: 400px;
  overflow-y: auto;
  padding: 15px;
  background: #fafafa;
}

.message {
  margin-bottom: 15px;
  max-width: 70%;
}

.message.own {
  margin-left: auto;
}

.message .content {
  padding: 10px 15px;
  border-radius: 18px;
  background: white;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.message.own .content {
  background: #2196f3;
  color: white;
}

.message .time {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
  padding: 0 15px;
}

.input-area {
  display: flex;
  padding: 15px;
  background: white;
  border-top: 1px solid #e0e0e0;
}

.message-input {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  outline: none;
  margin-right: 10px;
}

.message-input:focus {
  border-color: #2196f3;
}

button {
  padding: 10px 20px;
  background: #2196f3;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
}

button:hover:not(:disabled) {
  background: #1976d2;
}

button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.offline-messages {
  border: 1px solid #ff9800;
  border-radius: 8px;
  padding: 15px;
  background: #fff8e1;
}

.offline-messages h3 {
  margin: 0 0 15px 0;
  color: #e65100;
}

.offline-message {
  padding: 10px;
  background: white;
  border-radius: 6px;
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.offline-message .content {
  flex: 1;
  font-weight: 500;
}

.offline-message .from {
  font-size: 12px;
  color: #666;
  margin: 0 10px;
}

.offline-message button {
  background: #ff9800;
  font-size: 12px;
  padding: 6px 12px;
}

.offline-message button:hover {
  background: #f57c00;
}
</style>