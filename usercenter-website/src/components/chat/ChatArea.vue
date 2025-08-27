<script setup lang="ts">
import { ref, nextTick, watch, onMounted } from 'vue';
import { Promotion, Refresh } from '@element-plus/icons-vue';
import { Message } from '@/types/chat';
import MessageItem from './MessageItem.vue';
import { scrollToBottom } from '@/utils/chat';

// Props
interface Props {
  sessionId?: string;
  messages: Message[];
  loading?: boolean;
}

const props = defineProps<Props>();

// Emits
interface Emits {
  (e: 'send-message', content: string): void;
  (e: 'retry-message', messageId: string): void;
  (e: 'answer-clarification', questionId: string, answer: string): void;
}

const emit = defineEmits<Emits>();

// 响应式数据
const messageInput = ref('');
const messagesContainer = ref<HTMLElement>();
const inputRef = ref<HTMLTextAreaElement>();

// 发送消息
const sendMessage = () => {
  const content = messageInput.value.trim();
  if (!content || props.loading) return;
  
  emit('send-message', content);
  messageInput.value = '';
  
  // 聚焦输入框
  nextTick(() => {
    inputRef.value?.focus();
  });
};

// 重试消息
const retryMessage = (messageId: string) => {
  if (props.loading) return;
  emit('retry-message', messageId);
};

// 处理澄清问题回答
const handleClarificationAnswer = (questionId: string, answer: string) => {
  if (props.loading) return;
  emit('answer-clarification', questionId, answer);
};

// 处理键盘事件
const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault();
    if (!props.loading) {
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

// 监听消息变化，自动滚动
watch(() => props.messages, () => {
  autoScrollToBottom();
}, { deep: true });

// 监听会话变化，自动滚动
watch(() => props.sessionId, () => {
  autoScrollToBottom();
});

// 组件挂载后聚焦输入框
onMounted(() => {
  inputRef.value?.focus();
  autoScrollToBottom();
});
</script>

<template>
  <div class="chat-area">
    <!-- 消息列表 -->
    <div 
      ref="messagesContainer"
      class="messages-container"
      v-loading="loading"
    >
      <div v-if="messages.length === 0" class="empty-messages">
        <div class="empty-icon">💬</div>
        <div class="empty-text">开始对话吧！</div>
      </div>
      
      <div v-else class="messages-list">
        <MessageItem
          v-for="message in messages"
          :key="message.id"
          :message="message"
          @retry-message="retryMessage"
          @answer-clarification="handleClarificationAnswer"
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
             size="small"
           >
             发送
           </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-area {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f8f9fa;
}

.empty-messages {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.input-area {
  border-top: 1px solid #e4e7ed;
  background: #fff;
  padding: 16px;
}

.input-container {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.message-input {
  flex: 1;
}

.input-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
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
  .input-container {
    flex-direction: column;
    align-items: stretch;
  }
  
  .input-actions {
    flex-direction: row;
    justify-content: flex-end;
  }
  
  .messages-container {
    padding: 12px;
  }
  
  .input-area {
    padding: 12px;
  }
}
</style> 