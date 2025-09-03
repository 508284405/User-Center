<template>
  <div 
    :class="[
      'message-item',
      {
        'message-own': isOwnMessage,
        'message-other': !isOwnMessage,
        'message-recalled': message.isRecalled,
        'message-deleted': isDeleted,
        'message-editing': isEditing
      }
    ]"
    @contextmenu.prevent="handleContextMenu"
  >
    <!-- 消息时间显示 -->
    <div 
      v-if="shouldShowTime" 
      class="message-time"
    >
      {{ formatTime(message.timestamp || message.createdAt) }}
    </div>

    <!-- 消息主体 -->
    <div class="message-body">
      <!-- 发送者信息（群聊场景） -->
      <div 
        v-if="shouldShowSender && !isOwnMessage" 
        class="message-sender"
      >
        {{ getSenderName(message) }}
      </div>

      <!-- 消息内容区域 -->
      <div 
        :class="[
          'message-content',
          {
            'content-own': isOwnMessage,
            'content-other': !isOwnMessage,
            'content-recalled': message.isRecalled,
            'content-reply': isReplyMessage
          }
        ]"
      >
        <!-- 回复引用显示 -->
        <div 
          v-if="isReplyMessage" 
          class="reply-quote"
          @click="jumpToOriginalMessage"
        >
          <div class="quote-indicator"></div>
          <div class="quote-content">
            <div class="quote-author">{{ message.quotedFromUser || '未知用户' }}</div>
            <div class="quote-text">{{ getQuotedSummary(message.quotedContent) }}</div>
          </div>
        </div>

        <!-- 消息文本内容 -->
        <div 
          v-if="!message.isRecalled"
          class="message-text"
        >
          <!-- 编辑模式 -->
          <div v-if="isEditing" class="edit-mode">
            <el-input
              v-model="editContent"
              type="textarea"
              :rows="3"
              placeholder="编辑消息内容..."
              maxlength="1000"
              show-word-limit
              @keydown.ctrl.enter="confirmEdit"
              @keydown.esc="cancelEdit"
            />
            <div class="edit-actions">
              <el-button size="small" @click="cancelEdit">取消</el-button>
              <el-button 
                type="primary" 
                size="small" 
                @click="confirmEdit"
                :loading="editLoading"
              >
                保存
              </el-button>
            </div>
          </div>

          <!-- 正常显示模式 -->
          <div v-else class="normal-mode">
            <div class="text-content">
              {{ message.content }}
            </div>
            
            <!-- 编辑标识 -->
            <div 
              v-if="message.isEdited" 
              class="edit-indicator"
              :title="`编辑于 ${formatTime(message.editedAt)}`"
            >
              (已编辑{{ message.editCount > 1 ? ` ${message.editCount}次` : '' }})
            </div>
          </div>
        </div>

        <!-- 撤回消息显示 -->
        <div v-else class="recalled-message">
          <i class="el-icon-warning-outline"></i>
          <span>{{ getRecallText() }}</span>
        </div>

        <!-- 表情反应区域 -->
        <div v-if="reactions.length > 0" class="message-reactions">
          <div
            v-for="reaction in reactions"
            :key="reaction.emoji"
            :class="[
              'reaction-item',
              { 'reaction-active': isUserReacted(reaction) }
            ]"
            @click="toggleReaction(reaction.emoji, reaction.name)"
            :title="getReactionTooltip(reaction)"
          >
            <span class="reaction-emoji">{{ reaction.emoji }}</span>
            <span class="reaction-count">{{ reaction.count }}</span>
          </div>
          
          <!-- 添加表情按钮 -->
          <div class="add-reaction-btn">
            <EmojiPicker @emoji-selected="addReaction" />
          </div>
        </div>

        <!-- 快捷反应按钮（无反应时显示） -->
        <div 
          v-else-if="!message.isRecalled"
          class="quick-reactions"
          @mouseenter="showQuickReactions = true"
          @mouseleave="showQuickReactions = false"
        >
          <transition name="fade">
            <div v-show="showQuickReactions" class="quick-reaction-list">
              <span
                v-for="emoji in quickEmojis"
                :key="emoji.code"
                class="quick-emoji"
                @click="addReaction(emoji)"
                :title="emoji.name"
              >
                {{ emoji.emoji }}
              </span>
              <EmojiPicker @emoji-selected="addReaction" />
            </div>
          </transition>
        </div>

        <!-- 消息状态和时间 -->
        <div class="message-meta">
          <span class="message-timestamp">{{ formatDetailTime(message.timestamp || message.createdAt) }}</span>
          
          <!-- 消息状态显示（仅自己的消息） -->
          <div v-if="isOwnMessage && !message.isRecalled" class="message-status">
            <!-- 发送状态图标 -->
            <span 
              :class="[
                'status-icon',
                {
                  'status-sending': message.sendStatus === 0,
                  'status-delivered': message.sendStatus === 1,
                  'status-failed': message.sendStatus === 2,
                  'status-read': message.sendStatus === 3
                }
              ]"
              :title="getStatusText(message.sendStatus)"
            >
              {{ getStatusIcon(message.sendStatus) }}
            </span>
            
            <!-- 重试按钮（发送失败时显示） -->
            <el-button
              v-if="canRetry"
              type="text"
              size="small"
              class="retry-btn"
              @click="retryMessage"
              :loading="retryLoading"
              title="重试发送"
            >
              <i class="el-icon-refresh"></i>
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 右键菜单 -->
    <el-dropdown
      ref="contextMenuRef"
      trigger="manual"
      :visible-arrow="false"
      popper-class="message-context-menu"
    >
      <div></div>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item
            v-if="canReply"
            @click="replyMessage"
            icon="el-icon-back"
          >
            回复
          </el-dropdown-item>
          <el-dropdown-item
            v-if="canEdit"
            @click="startEdit"
            icon="el-icon-edit"
          >
            编辑
          </el-dropdown-item>
          <el-dropdown-item
            @click="copyMessage"
            icon="el-icon-copy-document"
          >
            复制
          </el-dropdown-item>
          <el-dropdown-item
            v-if="canRecall"
            @click="showRecallDialog"
            icon="el-icon-delete"
          >
            撤回
          </el-dropdown-item>
          <el-dropdown-item
            v-if="canDelete"
            @click="showDeleteDialog"
            icon="el-icon-delete"
          >
            删除
          </el-dropdown-item>
          <el-dropdown-item
            @click="forwardMessage"
            icon="el-icon-share"
          >
            转发
          </el-dropdown-item>
          <el-dropdown-item
            @click="selectMessage"
            icon="el-icon-check"
          >
            选择
          </el-dropdown-item>
          <el-dropdown-item
            v-if="!message.isRecalled"
            @click="showEmojiPicker"
            icon="el-icon-star-off"
          >
            表情反应
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>

    <!-- 撤回确认对话框 -->
    <el-dialog
      v-model="recallDialogVisible"
      title="撤回消息"
      width="400px"
      :append-to-body="true"
    >
      <div class="recall-content">
        <p>确定要撤回这条消息吗？</p>
        <el-input
          v-model="recallReason"
          type="textarea"
          placeholder="撤回原因（可选）"
          :rows="3"
          maxlength="100"
          show-word-limit
        />
      </div>
      <template #footer>
        <el-button @click="recallDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="confirmRecall"
          :loading="recallLoading"
        >
          确定撤回
        </el-button>
      </template>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="删除消息"
      width="400px"
      :append-to-body="true"
    >
      <div class="delete-content">
        <p>选择删除方式：</p>
        <el-radio-group v-model="deleteType">
          <el-radio :label="0">仅自己可见删除</el-radio>
          <el-radio :label="1">双方删除</el-radio>
        </el-radio-group>
        <el-input
          v-model="deleteReason"
          type="textarea"
          placeholder="删除原因（可选）"
          :rows="2"
          maxlength="100"
          show-word-limit
          style="margin-top: 15px"
        />
      </div>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button 
          type="danger" 
          @click="confirmDelete"
          :loading="deleteLoading"
        >
          确定删除
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Message, MessageSendStatus, MessageContentType, MessageDeleteType, MessageReaction } from '@/types/chat';
import { MessageUtils } from '@/utils/messageUtils';
import EmojiPicker from './EmojiPicker.vue';
import { 
  sendRecallMessage, 
  sendDeleteMessage, 
  sendEditMessage, 
  sendReadReceipt,
  sendRetryMessage,
  sendAddReaction
} from '@/utils/chatWebSocket';
import { getMessageReactions, addMessageReaction } from '@/api/chat';

// Props
interface Props {
  message: Message;
  currentUserId: string;
  previousMessage?: Message;
  userMap?: Map<string, any>;
}

// Emoji interface
interface EmojiItem {
  code: string;
  emoji: string;
  name: string;
  category: string;
}

const props = withDefaults(defineProps<Props>(), {
  userMap: () => new Map()
});

// Emits
const emit = defineEmits<{
  'reply-message': [message: Message];
  'copy-message': [content: string];
  'forward-message': [message: Message];
  'select-message': [messageId: string];
  'jump-to-message': [messageId: string];
  'message-updated': [message: Message];
}>();

// Reactive state
const isEditing = ref(false);
const editContent = ref('');
const editLoading = ref(false);

const recallDialogVisible = ref(false);
const recallReason = ref('');
const recallLoading = ref(false);

const deleteDialogVisible = ref(false);
const deleteType = ref<number>(0);
const deleteReason = ref('');
const deleteLoading = ref(false);

const retryLoading = ref(false);
const showQuickReactions = ref(false);
const reactions = ref<MessageReaction[]>([]);
const reactionLoading = ref(false);

const contextMenuRef = ref();

// 快捷表情列表
const quickEmojis: EmojiItem[] = [
  { code: 'thumbsup', emoji: '👍', name: '点赞', category: 'people' },
  { code: 'heart', emoji: '❤️', name: '红心', category: 'symbols' },
  { code: 'laughing', emoji: '😆', name: '大笑', category: 'smileys' },
  { code: 'surprised', emoji: '😮', name: '惊讶', category: 'smileys' },
  { code: 'cry', emoji: '😢', name: '哭泣', category: 'smileys' }
];

// Computed properties
const isOwnMessage = computed(() => 
  MessageUtils.isOwnMessage(props.message, props.currentUserId)
);

const isDeleted = computed(() => 
  MessageUtils.isDeletedMessage(props.message)
);

const isReplyMessage = computed(() => 
  MessageUtils.isReplyMessage(props.message)
);

const shouldShowTime = computed(() => 
  MessageUtils.shouldShowTime(props.message, props.previousMessage)
);

const shouldShowSender = computed(() => 
  MessageUtils.shouldShowSender(props.message, props.previousMessage)
);

const canRecall = computed(() => 
  MessageUtils.canRecall(props.message, props.currentUserId)
);

const canEdit = computed(() => 
  MessageUtils.canEdit(props.message, props.currentUserId)
);

const canDelete = computed(() => 
  MessageUtils.canDelete(props.message, props.currentUserId)
);

const canReply = computed(() => 
  MessageUtils.canReply(props.message)
);

const canRetry = computed(() => 
  MessageUtils.canRetry(props.message, props.currentUserId)
);

// Methods
const formatTime = (timestamp?: number) => {
  if (!timestamp) return '';
  return MessageUtils.formatTime(timestamp);
};

const formatDetailTime = (timestamp?: number) => {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  return date.toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  });
};

const getSenderName = (message: Message) => {
  return MessageUtils.getSenderDisplayName(message, props.userMap);
};

const getQuotedSummary = (content?: string) => {
  if (!content) return '';
  return MessageUtils.getQuotedSummary(content);
};

const getRecallText = () => {
  if (isOwnMessage.value) {
    return '你撤回了一条消息';
  } else {
    const senderName = getSenderName(props.message);
    return `${senderName}撤回了一条消息`;
  }
};

const getStatusIcon = (status?: number) => {
  if (status === undefined) return '';
  return MessageUtils.getStatusIcon(status);
};

const getStatusText = (status?: number) => {
  if (status === undefined) return '';
  return MessageUtils.getStatusText(status);
};

const handleContextMenu = (event: MouseEvent) => {
  event.preventDefault();
  
  // 显示右键菜单
  nextTick(() => {
    if (contextMenuRef.value) {
      contextMenuRef.value.visible = true;
      
      // 设置菜单位置
      const menu = document.querySelector('.message-context-menu');
      if (menu) {
        menu.style.left = `${event.clientX}px`;
        menu.style.top = `${event.clientY}px`;
      }
    }
  });
};

const replyMessage = () => {
  emit('reply-message', props.message);
  contextMenuRef.value.visible = false;
};

const startEdit = () => {
  isEditing.value = true;
  editContent.value = props.message.content;
  contextMenuRef.value.visible = false;
};

const cancelEdit = () => {
  isEditing.value = false;
  editContent.value = '';
};

const confirmEdit = async () => {
  if (!editContent.value.trim()) {
    ElMessage.warning('消息内容不能为空');
    return;
  }
  
  if (editContent.value === props.message.content) {
    cancelEdit();
    return;
  }
  
  editLoading.value = true;
  
  try {
    const success = sendEditMessage({
      msgId: props.message.msgId,
      sessionId: props.message.sessionId,
      newContent: editContent.value.trim(),
      editedAt: Date.now(),
      type: 'EDIT_MESSAGE'
    });
    
    if (success) {
      // 乐观更新
      const updatedMessage = {
        ...props.message,
        content: editContent.value.trim(),
        isEdited: true,
        editedAt: Date.now(),
        editCount: (props.message.editCount || 0) + 1
      };
      
      emit('message-updated', updatedMessage);
      cancelEdit();
      ElMessage.success('消息编辑成功');
    } else {
      ElMessage.error('消息编辑失败');
    }
  } catch (error) {
    console.error('编辑消息失败:', error);
    ElMessage.error('消息编辑失败');
  } finally {
    editLoading.value = false;
  }
};

const copyMessage = async () => {
  try {
    await navigator.clipboard.writeText(props.message.content);
    ElMessage.success('消息已复制');
    emit('copy-message', props.message.content);
  } catch (error) {
    console.error('复制消息失败:', error);
    ElMessage.error('复制失败');
  }
  contextMenuRef.value.visible = false;
};

const showRecallDialog = () => {
  recallDialogVisible.value = true;
  contextMenuRef.value.visible = false;
};

const confirmRecall = async () => {
  recallLoading.value = true;
  
  try {
    const success = sendRecallMessage({
      msgId: props.message.msgId,
      sessionId: props.message.sessionId,
      userId: props.currentUserId,
      reason: recallReason.value || undefined,
      recallTime: Date.now(),
      type: 'RECALL'
    });
    
    if (success) {
      recallDialogVisible.value = false;
      recallReason.value = '';
      ElMessage.success('消息撤回成功');
    } else {
      ElMessage.error('消息撤回失败');
    }
  } catch (error) {
    console.error('撤回消息失败:', error);
    ElMessage.error('消息撤回失败');
  } finally {
    recallLoading.value = false;
  }
};

const showDeleteDialog = () => {
  deleteDialogVisible.value = true;
  contextMenuRef.value.visible = false;
};

const confirmDelete = async () => {
  deleteLoading.value = true;
  
  try {
    const success = sendDeleteMessage({
      msgId: props.message.msgId,
      sessionId: props.message.sessionId,
      deleteType: deleteType.value,
      reason: deleteReason.value || undefined,
      type: 'DELETE_MESSAGE'
    });
    
    if (success) {
      deleteDialogVisible.value = false;
      deleteReason.value = '';
      ElMessage.success('消息删除成功');
    } else {
      ElMessage.error('消息删除失败');
    }
  } catch (error) {
    console.error('删除消息失败:', error);
    ElMessage.error('消息删除失败');
  } finally {
    deleteLoading.value = false;
  }
};

const forwardMessage = () => {
  emit('forward-message', props.message);
  contextMenuRef.value.visible = false;
};

const selectMessage = () => {
  emit('select-message', props.message.msgId);
  contextMenuRef.value.visible = false;
};

const jumpToOriginalMessage = () => {
  if (props.message.replyToMsgId) {
    emit('jump-to-message', props.message.replyToMsgId);
  }
};

const retryMessage = async () => {
  retryLoading.value = true;
  
  try {
    const success = sendRetryMessage(props.message.msgId, props.message.sessionId);
    
    if (success) {
      // 乐观更新消息状态
      const updatedMessage = {
        ...props.message,
        sendStatus: MessageSendStatus.SENDING,
        retryCount: (props.message.retryCount || 0) + 1
      };
      
      emit('message-updated', updatedMessage);
      ElMessage.success('重试发送中...');
    } else {
      ElMessage.error('重试发送失败');
    }
  } catch (error) {
    console.error('重试发送失败:', error);
    ElMessage.error('重试发送失败');
  } finally {
    retryLoading.value = false;
  }
};

// Methods for reactions
const loadReactions = async () => {
  try {
    const response = await getMessageReactions(props.message.msgId);
    if (response.data) {
      reactions.value = response.data;
    }
  } catch (error) {
    console.error('加载消息反应失败:', error);
  }
};

const addReaction = async (emoji: EmojiItem) => {
  if (reactionLoading.value) return;
  
  reactionLoading.value = true;
  
  try {
    const response = await addMessageReaction({
      msgId: props.message.msgId,
      sessionId: props.message.sessionId,
      emoji: emoji.emoji,
      name: emoji.name,
      action: 'add'
    });
    
    if (response.success) {
      // 发送WebSocket消息通知其他用户
      sendAddReaction({
        msgId: props.message.msgId,
        sessionId: props.message.sessionId,
        userId: props.currentUserId,
        emoji: emoji.emoji,
        name: emoji.name,
        action: 'add',
        timestamp: Date.now(),
        type: 'REACTION_UPDATE'
      });
      
      // 重新加载反应数据
      await loadReactions();
      ElMessage.success('添加反应成功');
    } else {
      ElMessage.error('添加反应失败');
    }
  } catch (error) {
    console.error('添加表情反应失败:', error);
    ElMessage.error('添加反应失败');
  } finally {
    reactionLoading.value = false;
  }
};

const toggleReaction = async (emoji: string, name: string) => {
  if (reactionLoading.value) return;
  
  const isReacted = isUserReacted({ emoji, userIds: reactions.value.find(r => r.emoji === emoji)?.userIds || [] });
  const action = isReacted ? 'remove' : 'add';
  
  reactionLoading.value = true;
  
  try {
    const response = await addMessageReaction({
      msgId: props.message.msgId,
      sessionId: props.message.sessionId,
      emoji,
      name,
      action
    });
    
    if (response.success) {
      // 发送WebSocket消息通知其他用户
      sendAddReaction({
        msgId: props.message.msgId,
        sessionId: props.message.sessionId,
        userId: props.currentUserId,
        emoji,
        name,
        action,
        timestamp: Date.now(),
        type: 'REACTION_UPDATE'
      });
      
      // 重新加载反应数据
      await loadReactions();
      ElMessage.success(action === 'add' ? '添加反应成功' : '移除反应成功');
    } else {
      ElMessage.error('操作失败');
    }
  } catch (error) {
    console.error('切换表情反应失败:', error);
    ElMessage.error('操作失败');
  } finally {
    reactionLoading.value = false;
  }
};

const isUserReacted = (reaction: { emoji: string; userIds: string[] }) => {
  return reaction.userIds.includes(props.currentUserId);
};

const getReactionTooltip = (reaction: MessageReaction) => {
  if (reaction.userIds.length === 0) return reaction.name;
  
  const userNames = reaction.userIds.map(userId => {
    const user = props.userMap?.get(userId);
    return user?.name || user?.nickname || `用户${userId}`;
  });
  
  if (userNames.length === 1) {
    return `${userNames[0]} 添加了 ${reaction.name}`;
  } else if (userNames.length <= 3) {
    return `${userNames.join('、')} 添加了 ${reaction.name}`;
  } else {
    return `${userNames.slice(0, 2).join('、')} 和其他 ${userNames.length - 2} 人添加了 ${reaction.name}`;
  }
};

const showEmojiPicker = () => {
  // 通过事件触发表情选择器显示
  contextMenuRef.value.visible = false;
  nextTick(() => {
    showQuickReactions.value = true;
  });
};

// 组件挂载时加载反应数据
onMounted(() => {
  if (props.message.msgId) {
    loadReactions();
  }
});

// 自动标记已读（如果不是自己的消息）
if (!isOwnMessage.value && !props.message.isRead) {
  sendReadReceipt({
    msgId: props.message.msgId,
    sessionId: props.message.sessionId,
    userId: props.currentUserId,
    readAt: Date.now(),
    type: 'READ_RECEIPT'
  });
}
</script>

<style scoped lang="scss">
.message-item {
  margin-bottom: 16px;
  padding: 0 16px;

  .message-time {
    text-align: center;
    color: #999;
    font-size: 12px;
    margin-bottom: 8px;
  }

  .message-body {
    display: flex;
    flex-direction: column;

    .message-sender {
      font-size: 12px;
      color: #666;
      margin-bottom: 4px;
    }

    .message-content {
      position: relative;
      max-width: 70%;
      word-wrap: break-word;
      word-break: break-all;

      &.content-own {
        margin-left: auto;
        background: #007aff;
        color: white;
        border-radius: 18px 18px 4px 18px;
      }

      &.content-other {
        margin-right: auto;
        background: #f0f0f0;
        color: #333;
        border-radius: 18px 18px 18px 4px;
      }

      &.content-recalled {
        background: #f5f5f5 !important;
        color: #999 !important;
        font-style: italic;
      }

      &.content-reply {
        .reply-quote {
          margin-bottom: 8px;
          padding: 8px 12px;
          background: rgba(0, 0, 0, 0.1);
          border-radius: 8px;
          cursor: pointer;
          display: flex;
          align-items: flex-start;

          .quote-indicator {
            width: 3px;
            background: rgba(255, 255, 255, 0.5);
            border-radius: 2px;
            margin-right: 8px;
            min-height: 30px;
          }

          .quote-content {
            flex: 1;
            min-width: 0;

            .quote-author {
              font-size: 12px;
              font-weight: bold;
              margin-bottom: 2px;
              opacity: 0.8;
            }

            .quote-text {
              font-size: 12px;
              opacity: 0.7;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }
          }

          &:hover {
            background: rgba(0, 0, 0, 0.15);
          }
        }
      }

      .message-text {
        padding: 12px 16px;

        .edit-mode {
          .edit-actions {
            margin-top: 8px;
            display: flex;
            justify-content: flex-end;
            gap: 8px;
          }
        }

        .normal-mode {
          .text-content {
            line-height: 1.4;
            white-space: pre-wrap;
          }

          .edit-indicator {
            font-size: 11px;
            opacity: 0.7;
            margin-top: 4px;
            font-style: italic;
          }
        }
      }

      .recalled-message {
        padding: 12px 16px;
        display: flex;
        align-items: center;
        gap: 6px;

        i {
          font-size: 14px;
        }
      }

      .message-meta {
        padding: 0 16px 8px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        font-size: 11px;
        opacity: 0.7;

        .message-timestamp {
          flex: 1;
        }

        .message-status {
          display: flex;
          align-items: center;
          gap: 4px;

          .status-icon {
            font-size: 12px;
            
            &.status-sending {
              color: #409eff;
            }
            
            &.status-delivered {
              color: #67c23a;
            }
            
            &.status-failed {
              color: #f56c6c;
            }
            
            &.status-read {
              color: #67c23a;
            }
          }

          .retry-btn {
            padding: 2px 4px;
            font-size: 12px;
            
            &:hover {
              color: #409eff;
            }
          }
        }
      }
    }
  }

  &.message-own {
    .message-body {
      align-items: flex-end;
    }
  }

  &.message-other {
    .message-body {
      align-items: flex-start;
    }
  }

  &.message-recalled {
    opacity: 0.6;
  }

  &.message-deleted {
    display: none;
  }

  &.message-editing {
    .message-content {
      background: #fff8e1 !important;
      border: 1px solid #ffc107;
    }
  }
}

// 对话框样式
.recall-content, .delete-content {
  p {
    margin-bottom: 15px;
    color: #606266;
  }
}

// 表情反应样式
.message-reactions {
  margin: 8px 0;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;

  .reaction-item {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 8px;
    border-radius: 16px;
    background: rgba(0, 0, 0, 0.05);
    border: 1px solid transparent;
    cursor: pointer;
    transition: all 0.2s;
    font-size: 12px;

    &:hover {
      background: rgba(0, 0, 0, 0.1);
    }

    &.reaction-active {
      background: rgba(24, 144, 255, 0.1);
      border-color: #1890ff;
      color: #1890ff;
    }

    .reaction-emoji {
      font-size: 14px;
      line-height: 1;
    }

    .reaction-count {
      font-weight: 500;
      min-width: 12px;
      text-align: center;
    }
  }

  .add-reaction-btn {
    display: inline-flex;
    align-items: center;
  }
}

.quick-reactions {
  position: relative;
  margin: 8px 0;

  .quick-reaction-list {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    background: rgba(255, 255, 255, 0.95);
    border-radius: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    backdrop-filter: blur(10px);

    .quick-emoji {
      font-size: 18px;
      cursor: pointer;
      padding: 4px;
      border-radius: 50%;
      transition: all 0.2s;
      line-height: 1;

      &:hover {
        background: rgba(0, 0, 0, 0.1);
        transform: scale(1.2);
      }
    }
  }
}

// 动画效果
.fade-enter-active, .fade-leave-active {
  transition: all 0.3s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

// 右键菜单样式
:deep(.message-context-menu) {
  .el-dropdown-menu__item {
    font-size: 14px;
    
    &:hover {
      background: #f5f7fa;
    }
  }
}
</style>