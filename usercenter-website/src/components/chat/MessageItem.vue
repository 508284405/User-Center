<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { User, Service, CopyDocument, Refresh, Loading, ArrowDown, ArrowUp } from '@element-plus/icons-vue';
import { Message, MessageType, MessageStatus } from '@/types/chat';
import { formatTimestamp, copyToClipboard } from '@/utils/chat';

// Props
interface Props {
  message: Message;
}

const props = defineProps<Props>();

// Emits
interface Emits {
  (e: 'retry-message', messageId: string): void;
}

const emit = defineEmits<Emits>();

// 响应式数据
const isThinkingExpanded = ref(false);

// 计算属性
const isUser = computed(() => props.message.type === MessageType.USER);
const isAssistant = computed(() => props.message.type === MessageType.ASSISTANT);
const isError = computed(() => props.message.status === MessageStatus.ERROR);
const isSending = computed(() => props.message.status === MessageStatus.SENDING);
const hasThinkingContent = computed(() => {
  const result = isAssistant.value && props.message.thinkingContent && props.message.thinkingContent.trim().length > 0;
  return result;
});

const messageClass = computed(() => ({
  'message-item': true,
  'user-message': isUser.value,
  'assistant-message': isAssistant.value,
  'error-message': isError.value,
  'sending-message': isSending.value
}));

// 复制消息内容
const copyMessage = async () => {
  const success = await copyToClipboard(props.message.content);
  if (success) {
    ElMessage.success('已复制到剪贴板');
  } else {
    ElMessage.error('复制失败');
  }
};

// 重试消息
const retryMessage = () => {
  emit('retry-message', props.message.id);
};

// 切换思考内容展开状态
const toggleThinking = () => {
  isThinkingExpanded.value = !isThinkingExpanded.value;
};

// 监听关键变化用于调试
watch(() => props.message.thinkingContent, (newContent) => {
  if (newContent && newContent.length > 0) {
    console.log('思考内容已更新:', newContent.length, '字符');
  }
});
</script>

<template>
  <div :class="messageClass">
    <div class="message-avatar">
      <el-avatar :size="32" v-if="isUser">
        <el-icon><User /></el-icon>
      </el-avatar>
             <el-avatar :size="32" v-else>
         <el-icon><Service /></el-icon>
       </el-avatar>
    </div>
    
    <div class="message-content">
      <div class="message-header">
        <span class="message-sender">
          {{ isUser ? '我' : 'AI助手' }}
        </span>
        <span class="message-time">
          {{ formatTimestamp(message.timestamp) }}
        </span>
      </div>
      
      <div class="message-body">
        <!-- 思考内容区域 -->
        <div v-if="hasThinkingContent" class="thinking-section">
          <div class="thinking-header" @click="toggleThinking">
            <span class="thinking-label">💭 思考过程</span>
            <el-icon class="thinking-toggle">
              <ArrowDown v-if="!isThinkingExpanded" />
              <ArrowUp v-else />
            </el-icon>
          </div>
          <div v-if="isThinkingExpanded" class="thinking-content">
            {{ message.thinkingContent }}
          </div>
        </div>
        
        <!-- 消息内容 -->
        <div class="message-text" v-if="message.content">
          {{ message.content }}
        </div>
        <div class="message-loading" v-else-if="isSending">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span v-if="!hasThinkingContent">正在思考中...</span>
          <span v-else>正在回复中...</span>
        </div>
        <div class="message-error" v-else-if="isError">
          <span>消息发送失败</span>
        </div>
      </div>
      
      <div class="message-actions" v-if="message.content">
                 <el-button
           size="small"
           text
           :icon="CopyDocument"
           @click="copyMessage"
           title="复制"
         />
        <el-button
          v-if="isError && isUser"
          size="small"
          text
          :icon="Refresh"
          @click="retryMessage"
          title="重试"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.user-message {
  flex-direction: row-reverse;
}

.user-message .message-content {
  align-items: flex-end;
}

.user-message .message-header {
  text-align: right;
}

.user-message .message-body {
  background: #409eff;
  color: white;
}

.assistant-message .message-body {
  background: #f0f0f0;
  color: #303133;
}

.error-message .message-body {
  background: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fbc4c4;
}

.sending-message .message-body {
  background: #f8f9fa;
  color: #909399;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
  font-size: 12px;
  color: #909399;
}

.message-sender {
  font-weight: 500;
}

.message-time {
  font-size: 11px;
}

.message-body {
  padding: 12px 16px;
  border-radius: 12px;
  word-wrap: break-word;
  line-height: 1.5;
}

.message-text {
  white-space: pre-wrap;
}

.message-loading {
  display: flex;
  align-items: center;
  gap: 8px;
  font-style: italic;
}

.message-error {
  font-style: italic;
}

.message-actions {
  display: flex;
  gap: 4px;
  margin-top: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.message-item:hover .message-actions {
  opacity: 1;
}

.user-message .message-actions {
  justify-content: flex-end;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message-content {
    max-width: 85%;
  }
  
  .message-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;
  }
  
  .user-message .message-header {
    align-items: flex-end;
  }
  
  .message-body {
    padding: 10px 12px;
  }
}

.thinking-section {
  margin-bottom: 8px;
}

.thinking-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 8px;
  background: rgba(64, 158, 255, 0.1);
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
  font-size: 12px;
  color: #409eff;
}

.thinking-header:hover {
  background: rgba(64, 158, 255, 0.15);
}

.thinking-label {
  font-weight: 500;
}

.thinking-toggle {
  font-size: 12px;
  transition: transform 0.2s;
}

.thinking-content {
  margin-top: 4px;
  padding: 8px;
  background: rgba(64, 158, 255, 0.05);
  border-radius: 6px;
  font-size: 12px;
  color: #606266;
  white-space: pre-wrap;
  line-height: 1.4;
  border-left: 3px solid #409eff;
}
</style> 