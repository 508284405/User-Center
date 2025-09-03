<template>
  <div class="conversation-settings">
    <el-dropdown 
      trigger="click" 
      @command="handleSettingAction"
      placement="bottom-end"
    >
      <el-button 
        :icon="Setting" 
        circle
        size="small"
        type="text"
        class="settings-trigger"
      />
      <template #dropdown>
        <el-dropdown-menu>
          <!-- 置顶设置 -->
          <el-dropdown-item 
            :command="{ action: 'togglePin' }"
            :divided="false"
          >
            <el-icon>
              <Top v-if="!settings?.isPinned" />
              <Bottom v-else />
            </el-icon>
            {{ settings?.isPinned ? '取消置顶' : '置顶会话' }}
          </el-dropdown-item>

          <!-- 免打扰设置 -->
          <el-dropdown-item 
            :command="{ action: 'toggleMute' }"
          >
            <el-icon>
              <Bell v-if="!settings?.isMuted" />
              <MuteNotification v-else />
            </el-icon>
            {{ settings?.isMuted ? '取消免打扰' : '消息免打扰' }}
          </el-dropdown-item>

          <!-- 会话标签 -->
          <el-dropdown-item 
            :command="{ action: 'manageTags' }"
          >
            <el-icon><PriceTag /></el-icon>
            管理标签
          </el-dropdown-item>

          <!-- 消息搜索 -->
          <el-dropdown-item 
            :command="{ action: 'searchMessages' }"
            divided
          >
            <el-icon><Search /></el-icon>
            搜索消息
          </el-dropdown-item>

          <!-- 自定义背景 -->
          <el-dropdown-item 
            :command="{ action: 'setBackground' }"
          >
            <el-icon><Picture /></el-icon>
            聊天背景
          </el-dropdown-item>

          <!-- 归档设置 -->
          <el-dropdown-item 
            :command="{ action: 'toggleArchive' }"
            divided
          >
            <el-icon>
              <FolderAdd v-if="!settings?.isArchived" />
              <FolderRemove v-else />
            </el-icon>
            {{ settings?.isArchived ? '取消归档' : '归档会话' }}
          </el-dropdown-item>

          <!-- 清空聊天记录 -->
          <el-dropdown-item 
            :command="{ action: 'clearHistory' }"
            class="danger-item"
          >
            <el-icon><Delete /></el-icon>
            清空聊天记录
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>

    <!-- 免打扰设置对话框 -->
    <mute-settings-dialog
      v-model="showMuteDialog"
      :session-id="sessionId"
      :current-settings="settings"
      @settings-updated="handleSettingsUpdated"
    />

    <!-- 标签管理对话框 -->
    <conversation-tags-dialog
      v-model="showTagsDialog"
      :user-id="userId"
      :session-id="sessionId"
      @tags-updated="handleTagsUpdated"
    />

    <!-- 消息搜索对话框 -->
    <message-search-dialog
      v-model="showSearchDialog"
      :user-id="userId"
      :session-id="sessionId"
    />

    <!-- 背景设置对话框 -->
    <background-settings-dialog
      v-model="showBackgroundDialog"
      :user-id="userId"
      :session-id="sessionId"
      :current-background="settings?.customBackground"
      @background-updated="handleBackgroundUpdated"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Setting, Top, Bottom, Bell, MuteNotification, PriceTag,
  Search, Picture, FolderAdd, FolderRemove, Delete
} from '@element-plus/icons-vue';
import {
  getConversationSettings,
  togglePinConversation,
  toggleArchiveConversation,
  type ConversationSettings
} from '@/api/smartcs/conversation';
import MuteSettingsDialog from './MuteSettingsDialog.vue';
import ConversationTagsDialog from './ConversationTagsDialog.vue';
import MessageSearchDialog from './MessageSearchDialog.vue';
import BackgroundSettingsDialog from './BackgroundSettingsDialog.vue';

// Props
interface Props {
  userId: string;
  sessionId: number;
}

const props = defineProps<Props>();

// Emits
const emit = defineEmits<{
  'settings-changed': [settings: ConversationSettings];
}>();

// 响应式数据
const settings = ref<ConversationSettings | null>(null);
const loading = ref(false);
const showMuteDialog = ref(false);
const showTagsDialog = ref(false);
const showSearchDialog = ref(false);
const showBackgroundDialog = ref(false);

// 方法
const loadSettings = async () => {
  if (!props.userId || !props.sessionId) return;

  loading.value = true;
  try {
    const response = await getConversationSettings(props.userId, props.sessionId);
    if (response.success && response.data) {
      settings.value = response.data;
    } else {
      // 如果没有设置，使用默认设置
      settings.value = {
        id: 0,
        userId: props.userId,
        sessionId: props.sessionId,
        isPinned: false,
        isMuted: false,
        isArchived: false,
        createdAt: Date.now(),
        updatedAt: Date.now()
      };
    }
  } catch (error) {
    console.error('加载会话设置失败:', error);
  } finally {
    loading.value = false;
  }
};

const handleSettingAction = async (command: { action: string }) => {
  const { action } = command;

  switch (action) {
    case 'togglePin':
      await handleTogglePin();
      break;
    case 'toggleMute':
      showMuteDialog.value = true;
      break;
    case 'manageTags':
      showTagsDialog.value = true;
      break;
    case 'searchMessages':
      showSearchDialog.value = true;
      break;
    case 'setBackground':
      showBackgroundDialog.value = true;
      break;
    case 'toggleArchive':
      await handleToggleArchive();
      break;
    case 'clearHistory':
      await handleClearHistory();
      break;
  }
};

const handleTogglePin = async () => {
  if (!settings.value) return;

  const newPinState = !settings.value.isPinned;
  
  try {
    const response = await togglePinConversation(props.userId, props.sessionId, newPinState);
    
    if (response.success) {
      settings.value.isPinned = newPinState;
      settings.value.pinnedAt = newPinState ? Date.now() : undefined;
      
      ElMessage.success(newPinState ? '会话已置顶' : '已取消置顶');
      emit('settings-changed', settings.value);
    } else {
      ElMessage.error(response.errMessage || '操作失败');
    }
  } catch (error) {
    console.error('切换置顶状态失败:', error);
    ElMessage.error('操作失败');
  }
};

const handleToggleArchive = async () => {
  if (!settings.value) return;

  const newArchiveState = !settings.value.isArchived;
  const actionText = newArchiveState ? '归档' : '取消归档';
  
  try {
    await ElMessageBox.confirm(
      `确定要${actionText}此会话吗？`,
      `${actionText}会话`,
      {
        type: newArchiveState ? 'warning' : 'info',
        confirmButtonText: `确定${actionText}`,
        cancelButtonText: '取消'
      }
    );

    const response = await toggleArchiveConversation(props.userId, props.sessionId, newArchiveState);
    
    if (response.success) {
      settings.value.isArchived = newArchiveState;
      settings.value.archivedAt = newArchiveState ? Date.now() : undefined;
      
      ElMessage.success(`会话已${actionText}`);
      emit('settings-changed', settings.value);
    } else {
      ElMessage.error(response.errMessage || `${actionText}失败`);
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('切换归档状态失败:', error);
      ElMessage.error('操作失败');
    }
  }
};

const handleClearHistory = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空此会话的所有聊天记录吗？此操作不可恢复！',
      '清空聊天记录',
      {
        type: 'warning',
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    );

    // TODO: 调用清空聊天记录API
    // const response = await clearChatHistory(props.userId, props.sessionId);
    
    // 模拟API调用
    ElMessage.success('聊天记录已清空');
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空聊天记录失败:', error);
      ElMessage.error('清空失败');
    }
  }
};

const handleSettingsUpdated = (newSettings: ConversationSettings) => {
  settings.value = newSettings;
  emit('settings-changed', newSettings);
};

const handleTagsUpdated = () => {
  // 标签更新后可能需要刷新会话列表
  emit('settings-changed', settings.value!);
};

const handleBackgroundUpdated = (background: string) => {
  if (settings.value) {
    settings.value.customBackground = background;
    emit('settings-changed', settings.value);
  }
};

// 生命周期
onMounted(() => {
  loadSettings();
});

// 监听props变化
watch(
  () => [props.userId, props.sessionId],
  () => {
    if (props.userId && props.sessionId) {
      loadSettings();
    }
  },
  { immediate: true }
);
</script>

<style scoped lang="scss">
.conversation-settings {
  .settings-trigger {
    color: #909399;
    
    &:hover {
      color: #409eff;
    }
  }

  :deep(.el-dropdown-menu__item) {
    display: flex;
    align-items: center;
    
    .el-icon {
      margin-right: 8px;
    }

    &.danger-item {
      color: #f56c6c;

      &:hover {
        background-color: #fef0f0;
        color: #f56c6c;
      }
    }
  }
}
</style>