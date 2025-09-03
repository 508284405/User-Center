<template>
  <div class="enhanced-chat-area">
    <!-- 消息列表 -->
    <div 
      ref="messagesContainer" 
      class="messages-container"
      @scroll="handleScroll"
    >
      <!-- 加载更多指示器 -->
      <div v-if="loadingHistory" class="loading-indicator">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载历史消息...</span>
      </div>

      <!-- 消息列表 -->
      <div class="messages-list">
        <enhanced-message-item
          v-for="(message, index) in sortedMessages"
          :key="message.msgId || message.id"
          :message="message"
          :current-user-id="currentUserId"
          :previous-message="index > 0 ? sortedMessages[index - 1] : undefined"
          :user-map="userMap"
          @reply-message="handleReplyMessage"
          @copy-message="handleCopyMessage"
          @forward-message="handleForwardMessage"
          @select-message="handleSelectMessage"
          @jump-to-message="handleJumpToMessage"
          @message-updated="handleMessageUpdated"
        />
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && messages.length === 0" class="empty-state">
        <i class="el-icon-chat-dot-square"></i>
        <p>暂无消息</p>
      </div>

      <!-- 加载指示器 -->
      <div v-if="loading" class="loading-indicator">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
    </div>

    <!-- 回复预览区域 -->
    <div v-if="replyingTo" class="reply-preview">
      <div class="reply-content">
        <div class="reply-indicator"></div>
        <div class="reply-info">
          <div class="reply-to">回复 {{ getReplyUserName(replyingTo) }}</div>
          <div class="reply-message">{{ getQuotedSummary(replyingTo.content) }}</div>
        </div>
      </div>
      <el-button
        type="text"
        class="cancel-reply"
        @click="cancelReply"
      >
        <el-icon><Close /></el-icon>
      </el-button>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <!-- 工具栏 -->
      <div class="input-toolbar">
        <el-button-group>
          <el-button
            size="small"
            :disabled="!hasSelectedMessages"
            @click="batchDelete"
            type="danger"
          >
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
          <el-button
            size="small"
            :disabled="!hasSelectedMessages"
            @click="batchForward"
            type="primary"
          >
            <el-icon><Connection /></el-icon>
            批量转发
          </el-button>
          <el-button
            size="small"
            :disabled="!hasSelectedMessages"
            @click="batchMarkRead"
          >
            <el-icon><View /></el-icon>
            标记已读
          </el-button>
          <el-button
            size="small"
            @click="clearSelection"
            :disabled="!hasSelectedMessages"
          >
            <el-icon><Close /></el-icon>
            取消选择
          </el-button>
        </el-button-group>

        <div class="toolbar-right">
          <!-- 在线状态指示器 -->
          <user-status-indicator 
            :user-id="currentUserId"
            :status="userStatus"
          />
          
          <!-- 连接状态 -->
          <div 
            :class="[
              'connection-status',
              {
                'connected': isConnected,
                'connecting': isConnecting,
                'disconnected': isDisconnected
              }
            ]"
            :title="connectionStatusText"
          >
            <el-icon>
              <Connection v-if="isConnected" />
              <Loading v-else-if="isConnecting" class="is-loading" />
              <WarningFilled v-else />
            </el-icon>
          </div>
        </div>
      </div>

      <!-- 输入框 -->
      <div class="input-wrapper">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="3"
          :maxlength="1000"
          show-word-limit
          placeholder="输入消息... (Ctrl+Enter发送)"
          :disabled="isDisconnected || sending"
          @keydown.ctrl.enter="sendMessage"
          @input="handleInputChange"
          @focus="handleInputFocus"
          @blur="handleInputBlur"
        />
        
        <div class="input-actions">
          <el-button
            type="primary"
            :loading="sending"
            :disabled="!inputMessage.trim() || isDisconnected"
            @click="sendMessage"
          >
            发送
          </el-button>
        </div>
      </div>
    </div>

    <!-- 批量操作对话框 -->
    <el-dialog
      v-model="batchDeleteVisible"
      title="批量删除消息"
      width="450px"
      :append-to-body="true"
    >
      <div class="batch-delete-content">
        <p>将删除 {{ selectedMessages.size }} 条消息</p>
        <el-radio-group v-model="batchDeleteType">
          <el-radio :label="0">仅自己可见删除</el-radio>
          <el-radio :label="1">双方删除</el-radio>
        </el-radio-group>
        <el-input
          v-model="batchDeleteReason"
          type="textarea"
          placeholder="删除原因（可选）"
          :rows="2"
          maxlength="100"
          show-word-limit
          style="margin-top: 15px"
        />
      </div>
      <template #footer>
        <el-button @click="batchDeleteVisible = false">取消</el-button>
        <el-button 
          type="danger" 
          @click="confirmBatchDelete"
          :loading="batchDeleteLoading"
        >
          确定删除
        </el-button>
      </template>
    </el-dialog>

    <!-- 转发对话框 -->
    <message-forward-dialog
      v-model="forwardDialogVisible"
      :messages="forwardMessages"
      @forward="handleForward"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, watch, onMounted, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Loading, Close, Delete, View, Connection, WarningFilled } from '@element-plus/icons-vue';
import { Message } from '@/types/chat';
import { MessageUtils } from '@/utils/messageUtils';
import { 
  connectionStatus, 
  sendChatMessage, 
  sendReplyMessage,
  sendDeleteMessage,
  sendReadReceipt,
  sendTypingStatus,
  sendForwardMessage,
  registerMessageHandler,
  getTotalUnreadCount
} from '@/utils/chatWebSocket';
import EnhancedMessageItem from './EnhancedMessageItem.vue';
import UserStatusIndicator from './UserStatusIndicator.vue';
import MessageForwardDialog from './MessageForwardDialog.vue';

// Props
interface Props {
  sessionId?: string;
  messages: Message[];
  loading?: boolean;
  currentUserId: string;
  userMap?: Map<string, any>;
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  userMap: () => new Map()
});

// Emits
const emit = defineEmits<{
  'send-message': [content: string, replyTo?: Message];
  'load-more-messages': [];
  'message-updated': [message: Message];
}>();

// Reactive state
const messagesContainer = ref<HTMLElement>();
const inputMessage = ref('');
const sending = ref(false);
const loadingHistory = ref(false);
const replyingTo = ref<Message | null>(null);
const selectedMessages = ref<Set<string>>(new Set());
const userStatus = ref('ONLINE');

// 批量删除相关
const batchDeleteVisible = ref(false);
const batchDeleteType = ref<number>(0);
const batchDeleteReason = ref('');
const batchDeleteLoading = ref(false);

// 转发相关
const forwardDialogVisible = ref(false);
const forwardMessages = ref<Message[]>([]);

// 输入状态
const typingTimer = ref<number>();
const isTyping = ref(false);

// Computed properties
const sortedMessages = computed(() => {
  return [...props.messages].sort((a, b) => {
    const aTime = a.createdAt || a.timestamp;
    const bTime = b.createdAt || b.timestamp;
    return aTime - bTime;
  });
});

const hasSelectedMessages = computed(() => {
  return selectedMessages.value.size > 0;
});

const isConnected = computed(() => connectionStatus.value === 'connected');
const isConnecting = computed(() => connectionStatus.value === 'connecting');
const isDisconnected = computed(() => connectionStatus.value === 'disconnected');

const connectionStatusText = computed(() => {
  switch (connectionStatus.value) {
    case 'connected':
      return '已连接';
    case 'connecting':
      return '连接中...';
    case 'disconnected':
      return '已断开连接';
    default:
      return '未知状态';
  }
});

// Methods
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
};

const handleScroll = () => {
  if (!messagesContainer.value) return;
  
  const { scrollTop, scrollHeight, clientHeight } = messagesContainer.value;
  
  // 滚动到顶部时加载更多历史消息
  if (scrollTop <= 100 && !loadingHistory.value) {
    loadMoreMessages();
  }
};

const loadMoreMessages = () => {
  if (props.loading || loadingHistory.value) return;
  
  loadingHistory.value = true;
  emit('load-more-messages');
  
  // 模拟加载完成
  setTimeout(() => {
    loadingHistory.value = false;
  }, 1000);
};

const sendMessage = () => {
  if (!inputMessage.value.trim() || sending.value || isDisconnected.value) {
    return;
  }
  
  sending.value = true;
  
  try {
    const content = inputMessage.value.trim();
    
    if (replyingTo.value) {
      // 发送回复消息
      const success = sendReplyMessage({
        msgId: MessageUtils.generateMessageId(),
        sessionId: props.sessionId || '',
        content,
        messageType: 'USER',
        fromUserId: props.currentUserId,
        createTime: Date.now(),
        replyToMsgId: replyingTo.value.msgId,
        quotedContent: replyingTo.value.content,
        quotedFromUser: getReplyUserName(replyingTo.value),
        type: 'REPLY_MESSAGE'
      });
      
      if (success) {
        emit('send-message', content, replyingTo.value);
        inputMessage.value = '';
        cancelReply();
        ElMessage.success('回复发送成功');
      } else {
        ElMessage.error('回复发送失败');
      }
    } else {
      // 发送普通消息
      const success = sendChatMessage({
        msgId: MessageUtils.generateMessageId(),
        sessionId: props.sessionId || '',
        content,
        messageType: 'USER',
        fromUserId: props.currentUserId,
        createTime: Date.now()
      });
      
      if (success) {
        emit('send-message', content);
        inputMessage.value = '';
        ElMessage.success('消息发送成功');
      } else {
        ElMessage.error('消息发送失败');
      }
    }
  } catch (error) {
    console.error('发送消息失败:', error);
    ElMessage.error('发送消息失败');
  } finally {
    sending.value = false;
  }
};

const handleInputChange = () => {
  // 发送输入状态
  if (!isTyping.value && props.sessionId) {
    isTyping.value = true;
    sendTypingStatus({
      sessionId: props.sessionId,
      userId: props.currentUserId,
      userType: 'CUSTOMER',
      isTyping: true,
      timestamp: Date.now(),
      type: 'TYPING'
    });
  }
  
  // 清除之前的计时器
  if (typingTimer.value) {
    clearTimeout(typingTimer.value);
  }
  
  // 3秒后停止输入状态
  typingTimer.value = window.setTimeout(() => {
    if (isTyping.value && props.sessionId) {
      isTyping.value = false;
      sendTypingStatus({
        sessionId: props.sessionId,
        userId: props.currentUserId,
        userType: 'CUSTOMER',
        isTyping: false,
        timestamp: Date.now(),
        type: 'TYPING'
      });
    }
  }, 3000);
};

const handleInputFocus = () => {
  // 自动滚动到底部
  scrollToBottom();
};

const handleInputBlur = () => {
  // 停止输入状态
  if (isTyping.value && props.sessionId) {
    isTyping.value = false;
    sendTypingStatus({
      sessionId: props.sessionId,
      userId: props.currentUserId,
      userType: 'CUSTOMER',
      isTyping: false,
      timestamp: Date.now(),
      type: 'TYPING'
    });
  }
};

const handleReplyMessage = (message: Message) => {
  replyingTo.value = message;
  scrollToBottom();
};

const cancelReply = () => {
  replyingTo.value = null;
};

const getReplyUserName = (message: Message) => {
  return MessageUtils.getSenderDisplayName(message, props.userMap);
};

const getQuotedSummary = (content: string) => {
  return MessageUtils.getQuotedSummary(content);
};

const handleCopyMessage = (content: string) => {
  console.log('消息已复制:', content);
};

const handleForwardMessage = (message: Message) => {
  console.log('转发消息:', message);
  forwardMessages.value = [message];
  forwardDialogVisible.value = true;
};

const handleSelectMessage = (messageId: string) => {
  if (selectedMessages.value.has(messageId)) {
    selectedMessages.value.delete(messageId);
  } else {
    selectedMessages.value.add(messageId);
  }
};

const clearSelection = () => {
  selectedMessages.value.clear();
};

const handleJumpToMessage = (messageId: string) => {
  // 滚动到指定消息
  const messageElement = document.querySelector(`[data-message-id="${messageId}"]`);
  if (messageElement) {
    messageElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
    
    // 高亮显示
    messageElement.classList.add('highlighted');
    setTimeout(() => {
      messageElement.classList.remove('highlighted');
    }, 2000);
  }
};

const handleMessageUpdated = (message: Message) => {
  emit('message-updated', message);
};

const batchDelete = () => {
  if (selectedMessages.value.size === 0) {
    ElMessage.warning('请先选择要删除的消息');
    return;
  }
  
  batchDeleteVisible.value = true;
};

const batchForward = () => {
  if (selectedMessages.value.size === 0) {
    ElMessage.warning('请先选择要转发的消息');
    return;
  }
  
  // 获取选中的消息对象
  const selectedMsgs = props.messages.filter(msg => 
    selectedMessages.value.has(msg.msgId || msg.id || '')
  );
  
  forwardMessages.value = selectedMsgs;
  forwardDialogVisible.value = true;
};

const confirmBatchDelete = async () => {
  batchDeleteLoading.value = true;
  
  try {
    const messageIds = Array.from(selectedMessages.value);
    
    // 逐个发送删除请求
    const promises = messageIds.map(msgId => {
      return new Promise<boolean>((resolve) => {
        const success = sendDeleteMessage({
          msgId,
          sessionId: props.sessionId || '',
          deleteType: batchDeleteType.value,
          reason: batchDeleteReason.value || undefined,
          type: 'DELETE_MESSAGE'
        });
        resolve(success);
      });
    });
    
    const results = await Promise.all(promises);
    const successCount = results.filter(Boolean).length;
    
    if (successCount === messageIds.length) {
      ElMessage.success(`成功删除 ${successCount} 条消息`);
      clearSelection();
      batchDeleteVisible.value = false;
      batchDeleteReason.value = '';
    } else {
      ElMessage.warning(`删除了 ${successCount}/${messageIds.length} 条消息`);
    }
  } catch (error) {
    console.error('批量删除失败:', error);
    ElMessage.error('批量删除失败');
  } finally {
    batchDeleteLoading.value = false;
  }
};

// 转发处理
const handleForward = async (targets: any[], messages: Message[], options: any) => {
  console.log('转发到目标:', targets, messages, options);
  
  try {
    // 构建转发消息内容
    let forwardContent = '';
    
    if (options.combineMessages) {
      // 合并为一条消息
      if (options.includeOriginalSender) {
        forwardContent = messages.map(msg => {
          const senderName = MessageUtils.getSenderDisplayName(msg);
          const time = MessageUtils.formatTime(msg.timestamp);
          return `[${time}] ${senderName}: ${msg.content}`;
        }).join('\n');
      } else {
        forwardContent = messages.map(msg => msg.content).join('\n');
      }
      
      // 发送合并后的消息到每个目标
      for (const target of targets) {
        if (target.type === 'session') {
          await sendForward(target.id, forwardContent, options);
        }
        // 其他目标类型的处理...
      }
    } else {
      // 分别转发每条消息
      for (const target of targets) {
        for (const message of messages) {
          let content = message.content;
          if (options.includeOriginalSender) {
            const senderName = MessageUtils.getSenderDisplayName(message);
            const time = MessageUtils.formatTime(message.timestamp);
            content = `[转发] [${time}] ${senderName}: ${content}`;
          } else {
            content = `[转发] ${content}`;
          }
          
          if (target.type === 'session') {
            await sendForward(target.id, content, options);
          }
          // 其他目标类型的处理...
        }
      }
    }
    
    ElMessage.success(`消息已转发至 ${targets.length} 个目标`);
  } catch (error) {
    console.error('转发失败:', error);
    ElMessage.error('转发失败');
  }
};

// 发送转发消息
const sendForward = async (sessionId: string, content: string, options: any) => {
  return new Promise<boolean>((resolve) => {
    const success = sendForwardMessage({
      sessionId,
      content,
      fromUserId: props.currentUserId || '',
      type: 'FORWARD'
    });
    resolve(success);
  });
};

const batchMarkRead = async () => {
  if (selectedMessages.value.size === 0) {
    ElMessage.warning('请先选择要标记的消息');
    return;
  }
  
  try {
    const messageIds = Array.from(selectedMessages.value);
    
    // 逐个发送已读回执
    const promises = messageIds.map(msgId => {
      return new Promise<boolean>((resolve) => {
        const success = sendReadReceipt({
          msgId,
          sessionId: props.sessionId || '',
          userId: props.currentUserId,
          readAt: Date.now(),
          type: 'READ_RECEIPT'
        });
        resolve(success);
      });
    });
    
    const results = await Promise.all(promises);
    const successCount = results.filter(Boolean).length;
    
    if (successCount === messageIds.length) {
      ElMessage.success(`成功标记 ${successCount} 条消息为已读`);
      clearSelection();
    } else {
      ElMessage.warning(`标记了 ${successCount}/${messageIds.length} 条消息为已读`);
    }
  } catch (error) {
    console.error('批量标记已读失败:', error);
    ElMessage.error('批量标记已读失败');
  }
};

// Watch for new messages to auto-scroll
watch(
  () => props.messages.length,
  () => {
    scrollToBottom();
  }
);

// Setup message handlers
onMounted(() => {
  // 注册消息处理器
  const unregisterMessage = registerMessageHandler('message', (message) => {
    console.log('收到新消息:', message);
    scrollToBottom();
  });
  
  const unregisterStatus = registerMessageHandler('messageStatus', (statusUpdate) => {
    console.log('消息状态更新:', statusUpdate);
    // TODO: 更新消息状态
  });
  
  const unregisterReadReceipt = registerMessageHandler('readReceipt', (receipt) => {
    console.log('收到已读回执:', receipt);
    // TODO: 更新消息已读状态
  });
  
  // 组件卸载时取消注册
  onUnmounted(() => {
    unregisterMessage();
    unregisterStatus();
    unregisterReadReceipt();
    
    if (typingTimer.value) {
      clearTimeout(typingTimer.value);
    }
  });
});

// Auto scroll to bottom on mount
onMounted(() => {
  scrollToBottom();
});
</script>

<style scoped lang="scss">
.enhanced-chat-area {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f8f9fa;

  .messages-container {
    flex: 1;
    overflow-y: auto;
    padding: 16px 0;
    scroll-behavior: smooth;

    .loading-indicator {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 16px;
      gap: 8px;
      color: #666;
      font-size: 14px;
    }

    .messages-list {
      min-height: 100%;
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 200px;
      color: #999;
      font-size: 16px;

      i {
        font-size: 48px;
        margin-bottom: 16px;
      }
    }
  }

  .reply-preview {
    display: flex;
    align-items: center;
    padding: 12px 16px;
    background: #e3f2fd;
    border-top: 1px solid #e0e6ed;
    
    .reply-content {
      flex: 1;
      display: flex;
      align-items: flex-start;
      gap: 8px;

      .reply-indicator {
        width: 3px;
        height: 36px;
        background: #2196f3;
        border-radius: 2px;
        margin-top: 2px;
      }

      .reply-info {
        flex: 1;

        .reply-to {
          font-size: 12px;
          font-weight: bold;
          color: #2196f3;
          margin-bottom: 2px;
        }

        .reply-message {
          font-size: 14px;
          color: #666;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }

    .cancel-reply {
      padding: 4px;
      color: #666;

      &:hover {
        color: #333;
      }
    }
  }

  .input-area {
    border-top: 1px solid #e0e6ed;
    background: white;

    .input-toolbar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 8px 16px;
      border-bottom: 1px solid #f0f2f5;

      .toolbar-right {
        display: flex;
        align-items: center;
        gap: 12px;

        .connection-status {
          display: flex;
          align-items: center;
          font-size: 16px;

          &.connected {
            color: #67c23a;
          }

          &.connecting {
            color: #409eff;
          }

          &.disconnected {
            color: #f56c6c;
          }
        }
      }
    }

    .input-wrapper {
      padding: 16px;
      position: relative;

      .input-actions {
        position: absolute;
        bottom: 24px;
        right: 24px;
        z-index: 1;
      }
    }
  }

  // 批量操作对话框样式
  .batch-delete-content {
    p {
      margin-bottom: 15px;
      color: #606266;
      font-weight: bold;
    }
  }
}

// 高亮消息样式
:deep(.highlighted) {
  background: rgba(255, 193, 7, 0.2) !important;
  transition: background-color 0.3s ease;
}

// 滚动条样式
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

  &:hover {
    background: #a8a8a8;
  }
}
</style>