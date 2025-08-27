<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { User, Service, CopyDocument, Refresh, Loading, ArrowDown, ArrowUp, QuestionFilled } from '@element-plus/icons-vue';
import { Message, MessageType, MessageStatus, MessageWithSlotFilling } from '@/types/chat';
import { formatTimestamp, copyToClipboard } from '@/utils/chat';

// Props
interface Props {
  message: MessageWithSlotFilling;
}

const props = defineProps<Props>();

// Emits
interface Emits {
  (e: 'retry-message', messageId: string): void;
  (e: 'answer-clarification', questionId: string, answer: string): void;
}

const emit = defineEmits<Emits>();

// 响应式数据
const isThinkingExpanded = ref(false);

// 计算属性
const isUser = computed(() => props.message.type === MessageType.USER);
const isAssistant = computed(() => props.message.type === MessageType.ASSISTANT);
const isClarification = computed(() => props.message.type === MessageType.CLARIFICATION);
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
  'clarification-message': isClarification.value,
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

// 回答澄清问题
const answerClarificationQuestion = (answer: string) => {
  emit('answer-clarification', props.message.id, answer);
};

// 使用建议回答
const useSuggestedResponse = (response: string) => {
  answerClarificationQuestion(response);
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
      <el-avatar :size="32" v-else-if="isClarification" type="warning">
        <el-icon><QuestionFilled /></el-icon>
      </el-avatar>
      <el-avatar :size="32" v-else>
        <el-icon><Service /></el-icon>
      </el-avatar>
    </div>
    
    <div class="message-content">
      <div class="message-header">
        <span class="message-sender">
          {{ isUser ? '我' : isClarification ? '槽位助手' : 'AI助手' }}
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
        
        <!-- 澄清问题界面 -->
        <div v-if="isClarification && message.clarificationData" class="clarification-content">
          <div class="clarification-intro">
            <el-icon class="clarification-icon"><QuestionFilled /></el-icon>
            <span>为了更好地帮助您，我需要一些额外信息：</span>
          </div>
          
          <!-- 澄清问题列表 -->
          <div class="clarification-questions">
            <div 
              v-for="(question, index) in message.clarificationData.questions" 
              :key="index"
              class="clarification-question"
            >
              <div class="question-text">{{ question }}</div>
              
              <!-- 建议回答 -->
              <div v-if="message.clarificationData.suggestedResponses && message.clarificationData.suggestedResponses.length > 0" class="suggested-responses">
                <div class="suggested-label">建议回答：</div>
                <div class="response-buttons">
                  <el-button
                    v-for="response in message.clarificationData.suggestedResponses"
                    :key="response"
                    size="small"
                    type="primary"
                    plain
                    @click="useSuggestedResponse(response)"
                  >
                    {{ response }}
                  </el-button>
                </div>
              </div>
              
              <!-- 示例值显示 -->
              <div v-if="message.clarificationData.examples && message.clarificationData.examples.length > 0" class="examples">
                <div class="examples-label">示例：</div>
                <div class="examples-list">
                  <el-tag 
                    v-for="example in message.clarificationData.examples" 
                    :key="example"
                    size="small"
                    type="info"
                  >
                    {{ example }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 上下文提示 -->
          <div v-if="message.clarificationData.contextHint" class="context-hint">
            <el-alert :title="message.clarificationData.contextHint" type="info" :closable="false" />
          </div>
        </div>

        <!-- 常规消息内容 -->
        <div class="message-text" v-else-if="message.content">
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

.clarification-message .message-body {
  background: #fff7e6;
  color: #303133;
  border: 1px solid #ffd591;
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

/* 澄清问题样式 */
.clarification-content {
  padding: 0;
}

.clarification-intro {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-weight: 500;
  color: #e6a23c;
}

.clarification-icon {
  font-size: 16px;
}

.clarification-questions {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.clarification-question {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.question-text {
  font-weight: 500;
  margin-bottom: 12px;
  color: #303133;
}

.suggested-responses {
  margin-bottom: 12px;
}

.suggested-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.response-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.examples {
  margin-bottom: 12px;
}

.examples-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.examples-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.context-hint {
  margin-top: 16px;
}

@media (max-width: 768px) {
  .response-buttons {
    flex-direction: column;
    align-items: stretch;
  }
  
  .response-buttons .el-button {
    justify-content: flex-start;
  }
}
</style> 