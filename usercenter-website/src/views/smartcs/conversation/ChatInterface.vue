<template>
  <div class="chat-interface">
    <!-- 主聊天区域 -->
    <div class="main-chat-area">
      <!-- 头部工具栏 -->
      <div class="chat-toolbar">
        <div class="toolbar-left">
          <h2 class="chat-title">
            <el-icon><ChatDotRound /></el-icon>
            智能对话
          </h2>
          <div v-if="currentSession" class="session-info">
            <span class="session-name">{{ currentSession.name }}</span>
            <el-tag size="small" :type="getSessionStatusType(currentSession.status)">
              {{ currentSession.status }}
            </el-tag>
          </div>
        </div>
        
        <div class="toolbar-right">
          <!-- 高级设置按钮 -->
          <el-button
            type="text"
            :icon="Setting"
            @click="showAdvancedSettings = !showAdvancedSettings"
            :class="{ active: showAdvancedSettings }"
            class="settings-toggle"
          >
            高级设置
          </el-button>
          
          <!-- 调试面板按钮 -->
          <el-button
            type="text"
            :icon="Monitor"
            @click="showDebugPanel = !showDebugPanel"
            :class="{ active: showDebugPanel }"
            class="debug-toggle"
          >
            调试信息
          </el-button>
          
          <!-- 新建会话 -->
          <el-button type="primary" :icon="Plus" @click="createNewSession">
            新会话
          </el-button>
        </div>
      </div>

      <!-- 聊天消息区域 -->
      <div class="chat-messages-container">
        <ChatArea
          :session-id="currentSession?.sessionId"
          :messages="messages"
          :loading="isLoading"
          @send-message="handleSendMessage"
          @retry-message="handleRetryMessage"
        />
      </div>
    </div>

    <!-- 侧边栏面板 -->
    <div class="chat-sidebar" v-show="showAdvancedSettings || showDebugPanel">
      <!-- 高级设置面板 -->
      <div v-show="showAdvancedSettings" class="settings-panel">
        <div class="panel-header">
          <h3>高级设置</h3>
          <el-button 
            type="text" 
            :icon="Close" 
            @click="showAdvancedSettings = false"
            class="close-btn"
          />
        </div>
        
        <!-- RAG配置预设 -->
        <div class="settings-section">
          <RagConfigPresets
            @preset-applied="handlePresetApplied"
            @custom-config-requested="showConfigPanel = true"
            @quick-apply-requested="applyCurrentRagConfig"
          />
        </div>
        
        <!-- RAG详细配置（可展开） -->
        <el-collapse v-model="activeConfigSections" class="config-collapse">
          <el-collapse-item name="ragConfig">
            <template #title>
              <div class="collapse-title">
                <el-icon><Tools /></el-icon>
                <span>RAG组件配置</span>
                <el-tag
                  v-if="ragStore.isCustomConfig"
                  type="warning"
                  size="small"
                  class="custom-tag"
                >
                  自定义
                </el-tag>
              </div>
            </template>
            
            <RagConfigPanel
              v-model="showConfigPanel"
              @config-applied="handleRagConfigApplied"
              @config-cancelled="showConfigPanel = false"
            />
          </el-collapse-item>
        </el-collapse>
      </div>

      <!-- 调试信息面板 -->
      <div v-show="showDebugPanel" class="debug-panel">
        <div class="panel-header">
          <h3>调试信息</h3>
          <el-button 
            type="text" 
            :icon="Close" 
            @click="showDebugPanel = false"
            class="close-btn"
          />
        </div>
        
        <DebugInfoPanel />
      </div>
    </div>

    <!-- 配置对话框 -->
    <el-dialog
      v-model="showConfigPanel"
      title="RAG组件配置"
      width="800px"
      :before-close="handleConfigDialogClose"
      destroy-on-close
    >
      <RagConfigPanel
        @config-applied="handleRagConfigApplied"
        @config-cancelled="handleConfigDialogClose"
      />
    </el-dialog>

    <!-- 会话创建对话框 -->
    <el-dialog
      v-model="showSessionDialog"
      title="创建新会话"
      width="500px"
    >
      <el-form :model="sessionForm" label-width="100px">
        <el-form-item label="会话名称">
          <el-input
            v-model="sessionForm.name"
            placeholder="输入会话名称（可选）"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="应用选择">
          <el-select
            v-model="sessionForm.appId"
            placeholder="选择应用"
            style="width: 100%"
          >
            <el-option
              v-for="app in availableApps"
              :key="app.id"
              :label="app.name"
              :value="app.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="模型选择">
          <el-select
            v-model="sessionForm.modelId"
            placeholder="选择模型"
            style="width: 100%"
          >
            <el-option
              v-for="model in availableModels"
              :key="model.id"
              :label="`${model.name} (${model.vendor})`"
              :value="model.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showSessionDialog = false">取消</el-button>
        <el-button
          type="primary"
          @click="confirmCreateSession"
          :loading="creating"
          :disabled="!sessionForm.appId || !sessionForm.modelId"
        >
          创建
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  ChatDotRound,
  Setting,
  Monitor,
  Plus,
  Close,
  Tools
} from '@element-plus/icons-vue';

// 组件导入
import ChatArea from '@/components/chat/ChatArea.vue';
import RagConfigPanel from '@/components/chat/RagConfigPanel.vue';
import RagConfigPresets from '@/components/chat/RagConfigPresets.vue';
import DebugInfoPanel from '@/components/chat/DebugInfoPanel.vue';

// Store和工具导入
import { useRagConfigStore } from '@/stores/ragConfig';
import { chatWithApp, AppChatRequest, AppChatResponse } from '@/api/smartcs/app';
import { chatWithApp as enhancedChatWithApp } from '@/api/chat';
import { Session, Message, MessageType, MessageStatus, RagConfigPreset } from '@/types/chat';

// Store实例
const ragStore = useRagConfigStore();

// 界面状态
const showAdvancedSettings = ref(false);
const showDebugPanel = ref(false);
const showConfigPanel = ref(false);
const showSessionDialog = ref(false);
const activeConfigSections = ref(['ragConfig']);

// 聊天状态
const currentSession = ref<Session | null>(null);
const messages = ref<Message[]>([]);
const isLoading = ref(false);
const creating = ref(false);

// 会话表单
const sessionForm = ref({
  name: '',
  appId: undefined as number | undefined,
  modelId: undefined as number | undefined
});

// 模拟数据（实际项目中应该从API获取）
const availableApps = ref([
  { id: 1, name: '智能助手', description: '通用智能助手' },
  { id: 2, name: '文档问答', description: '基于文档的问答系统' },
  { id: 3, name: '代码助手', description: '编程相关问答' }
]);

const availableModels = ref([
  { id: 1, name: 'GPT-4', vendor: 'OpenAI' },
  { id: 2, name: 'Claude-3', vendor: 'Anthropic' },
  { id: 3, name: '通义千问', vendor: '阿里巴巴' }
]);

// 计算属性
const getSessionStatusType = (status: string) => {
  switch (status) {
    case 'active': return 'success';
    case 'ended': return 'info';
    case 'error': return 'danger';
    default: return 'primary';
  }
};

// 方法实现

/**
 * 创建新会话
 */
const createNewSession = () => {
  sessionForm.value = {
    name: '',
    appId: availableApps.value[0]?.id,
    modelId: availableModels.value[0]?.id
  };
  showSessionDialog.value = true;
};

/**
 * 确认创建会话
 */
const confirmCreateSession = async () => {
  if (!sessionForm.value.appId || !sessionForm.value.modelId) {
    ElMessage.warning('请选择应用和模型');
    return;
  }

  creating.value = true;
  
  try {
    // 创建新会话
    const newSession: Session = {
      sessionId: `session_${Date.now()}`,
      name: sessionForm.value.name || `会话 ${new Date().toLocaleTimeString()}`,
      customerId: 1, // 当前用户ID
      status: 'active',
      createdAt: Date.now(),
      updatedAt: Date.now()
    };

    currentSession.value = newSession;
    messages.value = [];

    showSessionDialog.value = false;
    ElMessage.success('会话创建成功');
  } catch (error) {
    console.error('创建会话失败:', error);
    ElMessage.error('创建会话失败');
  } finally {
    creating.value = false;
  }
};

/**
 * 发送消息
 */
const handleSendMessage = async (content: string) => {
  if (!currentSession.value || isLoading.value) {
    ElMessage.warning('请先创建会话');
    return;
  }

  if (!sessionForm.value.appId || !sessionForm.value.modelId) {
    ElMessage.warning('请配置应用和模型');
    return;
  }

  // 添加用户消息
  const userMessage: Message = {
    id: `msg_${Date.now()}_user`,
    sessionId: currentSession.value.sessionId,
    content,
    type: MessageType.USER,
    status: MessageStatus.SENT,
    timestamp: Date.now()
  };

  messages.value.push(userMessage);
  isLoading.value = true;

  // 添加AI消息占位符
  const aiMessage: Message = {
    id: `msg_${Date.now()}_ai`,
    sessionId: currentSession.value.sessionId,
    content: '',
    type: MessageType.ASSISTANT,
    status: MessageStatus.SENDING,
    timestamp: Date.now()
  };

  messages.value.push(aiMessage);

  try {
    // 准备聊天请求
    const chatRequest: AppChatRequest = {
      appId: sessionForm.value.appId,
      modelId: sessionForm.value.modelId,
      message: content,
      sessionId: currentSession.value.sessionId,
      ragConfig: ragStore.getConfigCopy() // 包含RAG配置
    };

    // 发送聊天请求
    const response = await chatWithApp(chatRequest);

    if (response.success && response.data) {
      // 更新AI消息
      const messageIndex = messages.value.findIndex(m => m.id === aiMessage.id);
      if (messageIndex !== -1) {
        messages.value[messageIndex].content = response.data.content;
        messages.value[messageIndex].status = MessageStatus.RECEIVED;
        messages.value[messageIndex].timestamp = response.data.timestamp;
      }

      // 处理RAG调试信息
      if (response.data.ragDebugInfo) {
        ragStore.setDebugInfo(response.data.ragDebugInfo);
        
        // 如果启用了调试面板，自动显示
        if (ragStore.isValidConfig) {
          showDebugPanel.value = true;
        }
      }
    } else {
      throw new Error(response.errMessage || '请求失败');
    }
  } catch (error: any) {
    console.error('发送消息失败:', error);
    
    // 更新消息状态为错误
    const messageIndex = messages.value.findIndex(m => m.id === aiMessage.id);
    if (messageIndex !== -1) {
      messages.value[messageIndex].content = `发送失败: ${error.message}`;
      messages.value[messageIndex].status = MessageStatus.ERROR;
    }

    ElMessage.error(`发送消息失败: ${error.message}`);
  } finally {
    isLoading.value = false;
  }
};

/**
 * 重试消息
 */
const handleRetryMessage = async (messageId: string) => {
  const messageIndex = messages.value.findIndex(m => m.id === messageId);
  if (messageIndex === -1) return;

  const message = messages.value[messageIndex];
  if (message.type !== MessageType.USER) return;

  // 移除失败的AI响应
  const aiMessageIndex = messages.value.findIndex(
    (m, index) => index > messageIndex && m.type === MessageType.ASSISTANT
  );
  if (aiMessageIndex !== -1) {
    messages.value.splice(aiMessageIndex, 1);
  }

  // 重新发送
  await handleSendMessage(message.content);
};

/**
 * 处理预设应用
 */
const handlePresetApplied = (preset: RagConfigPreset) => {
  ElMessage.success(`已应用"${ragStore.currentPresetDefinition?.name}"预设配置`);
};

/**
 * 处理RAG配置应用
 */
const handleRagConfigApplied = () => {
  showConfigPanel.value = false;
  ElMessage.success('RAG配置已应用');
};

/**
 * 应用当前RAG配置
 */
const applyCurrentRagConfig = () => {
  if (!ragStore.isValidConfig) {
    ElMessage.warning('当前配置无效，请先修正配置错误');
    return;
  }
  
  ragStore.saveConfigToStorage();
  ElMessage.success('RAG配置已保存并应用');
};

/**
 * 关闭配置对话框
 */
const handleConfigDialogClose = () => {
  showConfigPanel.value = false;
};

// 初始化
onMounted(async () => {
  // 加载RAG配置
  ragStore.loadConfigFromStorage();
  
  // 创建默认会话
  if (!currentSession.value) {
    const defaultSession: Session = {
      sessionId: `session_${Date.now()}`,
      name: `默认会话 ${new Date().toLocaleTimeString()}`,
      customerId: 1,
      status: 'active',
      createdAt: Date.now(),
      updatedAt: Date.now()
    };
    
    currentSession.value = defaultSession;
    
    // 设置默认应用和模型
    if (availableApps.value.length > 0 && availableModels.value.length > 0) {
      sessionForm.value.appId = availableApps.value[0].id;
      sessionForm.value.modelId = availableModels.value[0].id;
    }
  }
});

// 监听RAG配置变化
watch(() => ragStore.currentConfig, () => {
  // 配置变化时可以做一些处理，比如显示提示等
}, { deep: true });
</script>

<style scoped lang="scss">
.chat-interface {
  display: flex;
  height: calc(100vh - 120px);
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.main-chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0; // 防止flex item收缩问题
}

.chat-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color-page);
  
  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .chat-title {
      margin: 0;
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 18px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }
    
    .session-info {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .session-name {
        font-size: 14px;
        color: var(--el-text-color-regular);
      }
    }
  }
  
  .toolbar-right {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .settings-toggle,
    .debug-toggle {
      &.active {
        color: var(--el-color-primary);
        background: var(--el-color-primary-light-9);
      }
    }
  }
}

.chat-messages-container {
  flex: 1;
  overflow: hidden;
}

.chat-sidebar {
  width: 400px;
  border-left: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color-page);
  display: flex;
  flex-direction: column;
}

.settings-panel,
.debug-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  
  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid var(--el-border-color-lighter);
    
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
    }
    
    .close-btn {
      padding: 4px;
    }
  }
}

.settings-section {
  padding: 16px 20px;
}

.config-collapse {
  border: none;
  
  .collapse-title {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .custom-tag {
      margin-left: auto;
    }
  }
}

.debug-panel {
  .panel-content {
    flex: 1;
    overflow-y: auto;
    padding: 16px 20px;
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .chat-sidebar {
    width: 350px;
  }
}

@media (max-width: 768px) {
  .chat-interface {
    flex-direction: column;
  }
  
  .chat-sidebar {
    width: 100%;
    height: 50%;
    border-left: none;
    border-top: 1px solid var(--el-border-color-lighter);
  }
  
  .chat-toolbar {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
    
    .toolbar-right {
      justify-content: flex-end;
    }
  }
}
</style>