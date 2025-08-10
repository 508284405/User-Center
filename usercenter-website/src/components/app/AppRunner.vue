<script setup lang="ts">
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue';
import { fetchEventSource } from '@microsoft/fetch-event-source';
import { ElMessage } from 'element-plus';
import { Promotion, ArrowLeft, FullScreen } from '@element-plus/icons-vue';
import MessageItem from '@/components/chat/MessageItem.vue';
import { scrollToBottom } from '@/utils/chat';
import { Message, MessageType, MessageStatus } from '@/types/chat';
import { getApp } from '@/api/smartcs/app';
import { getPublicApp } from '@/api/smartcs/app-public';

// 简单的UUID生成函数
const generateId = () => {
  return Date.now().toString(36) + Math.random().toString(36).substring(2);
};

// Props
interface Props {
  appId: number;
  mode: 'admin' | 'public';
  appName?: string;
}

const props = defineProps<Props>();

// Emits
interface Emits {
  (e: 'close'): void;
}

const emit = defineEmits<Emits>();

// 响应式数据
const messages = ref<Message[]>([]);
const messageInput = ref('');
const loading = ref(false);
const appInfo = ref<any>(null);
const sessionId = ref<string>(generateId());

const messagesContainer = ref<HTMLElement>();
const inputRef = ref<HTMLTextAreaElement>();

let abortController: AbortController | null = null;

// 计算属性
const displayTitle = computed(() => {
  if (props.appName) return props.appName;
  if (appInfo.value) return appInfo.value.name;
  return '应用运行';
});

// API Base URL
const apiBaseUrl = computed(() => {
  if (props.mode === 'admin') {
    return '/smartcs/api/admin/app';
  } else {
    return '/smartcs/api/app';
  }
});

// 获取应用信息
const fetchAppInfo = async () => {
  try {
    let result;
    
    if (props.mode === 'admin') {
      result = await getApp(props.appId);
    } else {
      result = await getPublicApp(props.appId);
    }
    
    if (result.success && result.data) {
      appInfo.value = result.data;
    } else {
      throw new Error(result.errMessage || '获取应用信息失败');
    }
  } catch (error: any) {
    console.error('获取应用信息失败:', error);
    ElMessage.error(error.message || '获取应用信息失败');
  }
};

// 发送消息
const sendMessage = async () => {
  const content = messageInput.value.trim();
  if (!content || loading.value) return;

  // 添加用户消息
  const userMessage: Message = {
    id: generateId(),
    sessionId: sessionId.value,
    content,
    type: MessageType.USER,
    status: MessageStatus.SENT,
    timestamp: Date.now()
  };
  messages.value.push(userMessage);
  messageInput.value = '';

  // 添加AI回复消息占位符
  const aiMessage: Message = {
    id: generateId(),
    sessionId: sessionId.value,
    content: '',
    type: MessageType.ASSISTANT,
    status: MessageStatus.SENDING,
    timestamp: Date.now()
  };
  messages.value.push(aiMessage);

  autoScrollToBottom();
  loading.value = true;

  try {
    await sendSSERequest(content, aiMessage);
  } catch (error: any) {
    console.error('发送消息失败:', error);
    aiMessage.status = MessageStatus.ERROR;
    aiMessage.content = '抱歉，发送消息失败，请稍后重试。';
    ElMessage.error(error.message || '发送消息失败');
  } finally {
    loading.value = false;
    
    // 聚焦输入框
    nextTick(() => {
      inputRef.value?.focus();
    });
  }
};

// 发送SSE请求
const sendSSERequest = async (content: string, aiMessage: Message) => {
  return new Promise<void>((resolve, reject) => {
    abortController = new AbortController();
    
    const token = localStorage.getItem('token');
    const headers: Record<string, string> = {
      'Content-Type': 'application/json'
    };
    
    // 仅管理员模式需要认证
    if (props.mode === 'admin' && token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const requestBody = {
      appId: props.appId,
      message: content,
      sessionId: sessionId.value
    };

    fetchEventSource(`http://localhost:8082${apiBaseUrl.value}/chat`, {
      method: 'POST',
      headers,
      body: JSON.stringify(requestBody),
      signal: abortController.signal,

      async onopen(response) {
        if (response.ok) {
          console.log('SSE 连接已建立');
          aiMessage.status = MessageStatus.RECEIVED;
        } else {
          const errorText = await response.text();
          throw new Error(`HTTP ${response.status}: ${errorText}`);
        }
      },

      onmessage(event) {
        try {
          if (event.data === 'complete') {
            console.log('SSE 消息流结束');
            aiMessage.status = MessageStatus.RECEIVED;
            resolve();
            return;
          }

          // 解析SSE消息 - 处理Spring SseEmitter的格式
          let eventData = event.data;
          if (eventData.startsWith('data:')) {
            eventData = eventData.substring(5).trim();
          }

          if (eventData === '[DONE]' || eventData === 'complete') {
            aiMessage.status = MessageStatus.RECEIVED;
            resolve();
            return;
          }

          // 尝试解析JSON数据
          try {
            const data = JSON.parse(eventData);
            handleSSEMessage(data, aiMessage);
          } catch (parseError) {
            // 如果不是JSON格式，直接作为文本内容处理
            if (eventData && eventData.trim()) {
              aiMessage.content += eventData;
              autoScrollToBottom();
            }
          }
          
        } catch (error) {
          console.error('处理SSE消息失败:', error);
        }
      },

      onerror(error) {
        console.error('SSE 连接错误:', error);
        aiMessage.status = MessageStatus.ERROR;
        if (!aiMessage.content) {
          aiMessage.content = '抱歉，连接出现问题，请稍后重试。';
        }
        reject(error);
      },

      onclose() {
        console.log('SSE 连接已关闭');
        aiMessage.status = MessageStatus.RECEIVED;
        resolve();
      }
    });
  });
};

// 处理SSE消息
const handleSSEMessage = (data: any, aiMessage: Message) => {
  if (data.event === 'start') {
    console.log('开始接收AI回复');
  } else if (data.event === 'progress' || data.event === 'data') {
    if (data.data && data.data.content) {
      aiMessage.content += data.data.content;
      autoScrollToBottom();
    } else if (typeof data.data === 'string') {
      aiMessage.content += data.data;
      autoScrollToBottom();
    }
  } else if (data.event === 'complete') {
    console.log('AI回复完成');
  } else if (data.event === 'error') {
    console.error('AI回复错误:', data.data);
    aiMessage.content = data.data?.message || '回复过程中出现错误';
    aiMessage.status = MessageStatus.ERROR;
  }
};

// 处理键盘事件
const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault();
    if (!loading.value) {
      sendMessage();
    }
  }
};

// 自动滚动到底部
const autoScrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      scrollToBottom(messagesContainer.value);
    }
  });
};

// 重试消息
const retryMessage = (messageId: string) => {
  if (loading.value) return;
  
  // 找到失败的消息
  const messageIndex = messages.value.findIndex(msg => msg.id === messageId);
  if (messageIndex === -1) return;
  
  // 找到对应的用户消息
  let userMessageIndex = messageIndex - 1;
  while (userMessageIndex >= 0 && messages.value[userMessageIndex].type !== MessageType.USER) {
    userMessageIndex--;
  }
  
  if (userMessageIndex >= 0) {
    const userMessage = messages.value[userMessageIndex];
    // 移除失败的AI回复和之后的消息
    messages.value = messages.value.slice(0, messageIndex);
    
    // 重新发送
    messageInput.value = userMessage.content;
    sendMessage();
  }
};

// 关闭应用
const closeApp = () => {
  if (abortController) {
    abortController.abort();
  }
  emit('close');
};

// 进入全屏
const enterFullscreen = () => {
  const element = document.documentElement;
  if (element.requestFullscreen) {
    element.requestFullscreen();
  }
};

// 组件挂载后初始化
onMounted(async () => {
  await fetchAppInfo();
  inputRef.value?.focus();
});

// 组件卸载时清理
onUnmounted(() => {
  if (abortController) {
    abortController.abort();
  }
});
</script>

<template>
  <div class="app-runner">
    <!-- 顶部标题栏 -->
    <div class="app-header">
      <div class="header-left">
        <el-button 
          :icon="ArrowLeft" 
          @click="closeApp"
          size="large"
          text
          class="close-btn"
        >
          返回
        </el-button>
        <div class="app-title">
          <h2>{{ displayTitle }}</h2>
          <span v-if="appInfo" class="app-description">{{ appInfo.description }}</span>
        </div>
      </div>
      <div class="header-right">
        <el-button 
          :icon="FullScreen" 
          @click="enterFullscreen"
          size="large"
          text
          title="全屏"
        />
      </div>
    </div>

    <!-- 聊天区域 -->
    <div class="chat-container">
      <!-- 消息列表 -->
      <div 
        ref="messagesContainer"
        class="messages-container"
      >
        <div v-if="messages.length === 0" class="empty-messages">
          <div class="empty-icon">💬</div>
          <div class="empty-text">开始与应用对话吧！</div>
          <div class="empty-hint">输入您的问题，AI助手将为您提供帮助</div>
        </div>
        
        <div v-else class="messages-list">
          <MessageItem
            v-for="message in messages"
            :key="message.id"
            :message="message"
            @retry-message="retryMessage"
          />
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <div class="input-container">
          <el-input
            ref="inputRef"
            v-model="messageInput"
            type="textarea"
            placeholder="输入消息... (Enter发送，Shift+Enter换行)"
            :rows="3"
            resize="none"
            :disabled="loading"
            @keydown="handleKeydown"
            class="message-input"
          />
          <div class="input-actions">
            <el-button
              type="primary"
              :icon="Promotion"
              @click="sendMessage"
              :disabled="!messageInput.trim() || loading"
              :loading="loading"
              size="large"
            >
              发送
            </el-button>
          </div>
        </div>
        <div class="input-footer">
          <span class="powered-by">Powered by SmartCS AI</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.app-runner {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  display: flex;
  flex-direction: column;
  z-index: 2000;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid #e4e7ed;
  background: #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.close-btn {
  font-size: 16px;
}

.app-title h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
  font-weight: 600;
}

.app-description {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
  display: block;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f8f9fa;
}

.empty-messages {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.6;
}

.empty-text {
  font-size: 18px;
  margin-bottom: 8px;
  color: #606266;
}

.empty-hint {
  font-size: 14px;
  color: #c0c4cc;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 800px;
  margin: 0 auto;
}

.input-area {
  border-top: 1px solid #e4e7ed;
  background: #fff;
  padding: 20px;
}

.input-container {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  max-width: 800px;
  margin: 0 auto;
}

.message-input {
  flex: 1;
}

.input-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-footer {
  text-align: center;
  margin-top: 12px;
}

.powered-by {
  font-size: 12px;
  color: #c0c4cc;
}

/* 自定义滚动条 */
.messages-container::-webkit-scrollbar {
  width: 6px;
}

.messages-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-header {
    padding: 12px 16px;
  }
  
  .header-left {
    gap: 12px;
  }
  
  .app-title h2 {
    font-size: 18px;
  }
  
  .messages-container {
    padding: 16px;
  }
  
  .input-area {
    padding: 16px;
  }
  
  .input-container {
    flex-direction: column;
    align-items: stretch;
  }
  
  .input-actions {
    flex-direction: row;
    justify-content: flex-end;
  }
  
  .messages-list {
    gap: 12px;
  }
}

/* 平滑动画 */
.messages-list {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>