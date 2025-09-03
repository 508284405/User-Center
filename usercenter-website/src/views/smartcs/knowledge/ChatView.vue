<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Setting, ChatDotRound, Plus, Edit } from '@element-plus/icons-vue';
import { useChatConfig } from '@/composables/useChatConfig';
import { useChat } from '@/composables/useChat';
import { useRecall } from '@/composables/useRecall';
import { useUserStatus } from '@/composables/useUserStatus';
import { chatSessionApi, SessionVO } from '@/api/smartcs/chatSession';
import { Session } from '@/types/chat';
import ConfigPanel from '@/components/chat/ConfigPanel.vue';
import ChatArea from '@/components/chat/ChatArea.vue';
import UserStatusSelector from '@/components/chat/UserStatusSelector.vue';

// 抽屉状态
const drawerVisible = ref(false);

// 会话状态
const sessions = ref<Session[]>([]);
const currentSession = ref<Session | null>(null);

// 使用配置管理
const {
  config,
  botList,
  knowledgeBaseList,
  contentList,
  botLoading,
  knowledgeLoading,
  contentLoading,
  initialize: initializeConfig,
  validateConfig,
  getSelectedBot,
  getSelectedKnowledgeBase,
  getSelectedContent,
  selectKnowledgeBase,
  clearKnowledgeBaseSelection,
  loadContentList
} = useChatConfig();

// 使用聊天管理
const {
  messages,
  isLoading,
  sendMessage,
  loadMessages,
  switchSession,
  setCurrentSessionId,
  clearMessages
} = useChat();

// 使用撤回功能
const {
  isRecalling,
  recallMessage,
  registerRecallHandler
} = useRecall();

// 使用用户状态功能
const {
  currentUserStatus,
  onlineUserCount,
  setCurrentUserStatus,
  registerUserStatusHandler,
  initializeUserStatus,
  setUserOffline,
  cleanup: cleanupUserStatus,
  UserStatus
} = useUserStatus();

// 计算属性
const selectedBotName = computed(() => {
  const bot = getSelectedBot();
  return bot ? `${bot.botName} (${bot.vendor}/${bot.modelType})` : '未选择机器人';
});

const selectedKnowledgeBasesText = computed(() => {
  if (!config.selectedKnowledgeBaseId) {
    return '未选择知识库';
  }
  const selectedKb = getSelectedKnowledgeBase();
  const selectedContent = getSelectedContent();
  let text = selectedKb ? selectedKb.name : '未选择知识库';
  if (selectedContent) {
    text += ` | ${selectedContent.title}`;
  }
  return text;
});

// 转换 SessionVO 为 Session 类型
const convertSessionVOToSession = (sessionVO: SessionVO): Session => {
  return {
    sessionId: sessionVO.sessionId,
    name: `会话 ${new Date(sessionVO.createdAt || Date.now()).toLocaleTimeString()}`,
    sessionName: sessionVO.sessionName,
    customerId: sessionVO.customerId,
    botId: undefined, // 历史会话不依赖当前配置
    botName: undefined,
            knowledgeBaseId: undefined, // 历史会话不依赖当前配置
            knowledgeBaseName: undefined,
    status: sessionVO.status || 'active',
    createdAt: sessionVO.createdAt ? new Date(sessionVO.createdAt).getTime() : Date.now(),
    updatedAt: sessionVO.createdAt ? new Date(sessionVO.createdAt).getTime() : Date.now(),
    lastMessage: sessionVO.lastMessage,
    lastMsgTime: sessionVO.lastMsgTime ? new Date(sessionVO.lastMsgTime).getTime() : undefined
  };
};

// 初始化会话列表
const initializeSessions = async () => {
  console.log('开始初始化会话列表，customerId:', config.customerId);
  
  if (!config.customerId) {
    console.log('用户ID未设置，跳过会话列表初始化');
    return;
  }

  try {
    console.log('正在获取用户会话列表...');
    // 获取用户会话列表
    const sessionVOList = await chatSessionApi.getCustomerSessions(config.customerId, 20);
    console.log('API 返回的会话列表:', sessionVOList);
    
    if (sessionVOList && sessionVOList.length > 0) {
      console.log(`获取到 ${sessionVOList.length} 个会话`);
      
      // 转换为本地Session格式
      const sessionList = sessionVOList.map(convertSessionVOToSession);
      console.log('转换后的会话列表:', sessionList);
      
      // 按最后消息时间或更新时间排序，最新的在前
      sessionList.sort((a, b) => {
        const timeA = a.lastMsgTime || a.updatedAt;
        const timeB = b.lastMsgTime || b.updatedAt;
        return timeB - timeA;
      });
      
      sessions.value = sessionList;
      
      // 选择最近的会话作为当前会话
      const latestSession = sessionList[0];
      console.log('选择最近的会话:', latestSession);
      currentSession.value = latestSession;
      
      // 切换到该会话并加载历史消息
      console.log('切换到会话并加载历史消息...');
      await switchSession(latestSession.sessionId);
      
      console.log(`✅ 会话初始化完成 - 已加载 ${sessionList.length} 个会话，当前会话: ${latestSession.sessionId}`);
    } else {
      console.log('用户暂无会话记录');
    }
  } catch (error: unknown) {
    console.error('❌ 初始化会话列表失败:', error);
    const errorMessage = error instanceof Error ? error.message : '未知错误';
    ElMessage.error(`加载会话列表失败: ${errorMessage}`);
  }
};

// 创建新会话
const createNewSession = async () => {
  if (!validateConfig()) {
    return;
  }

  try {
    const sessionVO = await chatSessionApi.createSession({
      customerId: config.customerId
    });

    if (sessionVO) {
      const newSession: Session = {
        sessionId: sessionVO.sessionId,
        name: `会话 ${new Date().toLocaleTimeString()}`,
        customerId: config.customerId,
        botId: config.selectedBotId,
        botName: getSelectedBot()?.botName,
        knowledgeBaseId: config.selectedKnowledgeBaseId,
        knowledgeBaseName: getSelectedKnowledgeBase()?.name,
        contentId: config.selectedContentId,
        contentName: getSelectedContent()?.title,
        status: sessionVO.sessionState,
        createdAt: Date.now(),
        updatedAt: Date.now()
      };

      sessions.value.unshift(newSession);
      currentSession.value = newSession;
      
      // 设置useChat的当前会话ID并清空消息
      setCurrentSessionId(newSession.sessionId);
      clearMessages();
      
      ElMessage.success('会话创建成功');
      drawerVisible.value = false;
    }
  } catch (error: unknown) {
    console.error('创建会话失败:', error);
    const errorMessage = error instanceof Error ? error.message : '创建会话失败';
    ElMessage.error(errorMessage);
  }
};

// 选择会话
const selectSession = async (session: Session) => {
  currentSession.value = session;
  await switchSession(session.sessionId);
  drawerVisible.value = false;
};

// 刷新内容列表
const handleRefreshContent = () => {
  if (config.selectedKnowledgeBaseId) {
    console.log('ChatView: 手动刷新内容列表');
    selectKnowledgeBase(config.selectedKnowledgeBaseId, true); // 强制加载
  }
};

// 编辑会话名称
const editSessionName = async () => {
  if (!currentSession.value) {
    return;
  }

  try {
    const { value: newName } = await ElMessageBox.prompt(
      '请输入新的会话名称',
      '编辑会话名称',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: currentSession.value.sessionName || currentSession.value.name,
        inputValidator: (value) => {
          if (!value || value.trim() === '') {
            return '会话名称不能为空';
          }
          if (value.length > 50) {
            return '会话名称长度不能超过50个字符';
          }
          return true;
        }
      }
    );

    if (newName && newName.trim() !== '') {
      const updatedSession = await chatSessionApi.updateSessionName(currentSession.value.sessionId, newName.trim());
      if (updatedSession) {
        // 更新当前会话
        currentSession.value.sessionName = newName.trim();
        
        // 更新会话列表中的对应项
        const sessionIndex = sessions.value.findIndex(s => s.sessionId === currentSession.value!.sessionId);
        if (sessionIndex !== -1) {
          sessions.value[sessionIndex].sessionName = newName.trim();
        }
        
        ElMessage.success('会话名称更新成功');
      }
    }
  } catch (error) {
    // 用户取消操作，不显示错误信息
    if (error !== 'cancel') {
      console.error('编辑会话名称失败:', error);
    }
  }
};

// 发送消息处理
const handleSendMessage = async (content: string) => {
  if (!currentSession.value || isLoading.value) {
    if (!currentSession.value) {
      ElMessage.warning('请先创建会话');
    }
    return;
  }

  // 使用当前配置的机器人和知识库，如果会话没有配置的话
  const botId = currentSession.value.botId || config.selectedBotId;
  const knowledgeBaseId = currentSession.value.knowledgeBaseId || config.selectedKnowledgeBaseId;
  const contentId = currentSession.value.contentId || config.selectedContentId;

  console.log('发送消息 - botId:', botId, 'knowledgeBaseId:', knowledgeBaseId, 'contentId:', contentId);

  await sendMessage(content, botId, knowledgeBaseId, contentId);
};

// 处理撤回消息
const handleRecallMessage = async (messageId: string) => {
  if (!currentSession.value) {
    ElMessage.warning('请先选择会话');
    return;
  }
  
  console.log('撤回消息:', messageId);
  await recallMessage(messageId, currentSession.value.sessionId);
};

// 处理用户状态变更
const handleStatusChange = async (status: UserStatus) => {
  console.log('用户状态变更:', status);
  await setCurrentUserStatus(status);
};

// 监听知识库选择变化
watch(() => config.selectedKnowledgeBaseId, (newKnowledgeBaseId, oldKnowledgeBaseId) => {
  if (newKnowledgeBaseId && newKnowledgeBaseId !== oldKnowledgeBaseId) {
    console.log('知识库选择变化，触发内容加载:', newKnowledgeBaseId);
    selectKnowledgeBase(newKnowledgeBaseId, true); // 强制加载
  }
});

// 页面初始化
onMounted(async () => {
  console.log('🚀 开始页面初始化...');
  
  console.log('1️⃣ 初始化配置...');
  await initializeConfig();
  console.log('✅ 配置初始化完成');
  
  // 注册消息处理器
  console.log('2️⃣ 注册消息处理器...');
  registerRecallHandler(messages.value);
  registerUserStatusHandler();
  
  // 初始化用户状态
  console.log('3️⃣ 初始化用户状态...');
  await initializeUserStatus();
  
  // 如果有选中的知识库，加载其内容
  if (config.selectedKnowledgeBaseId) {
    console.log('3️⃣ 加载选中知识库的内容...');
    selectKnowledgeBase(config.selectedKnowledgeBaseId, true); // 强制加载
  }
  
  console.log('4️⃣ 初始化会话列表...');
  await initializeSessions();
  console.log('✅ 页面初始化完成');
});

// 页面卸载时清理
onUnmounted(async () => {
  console.log('🧹 页面卸载，清理资源...');
  
  // 设置用户离线状态
  await setUserOffline();
  
  // 清理用户状态管理
  cleanupUserStatus();
  
  console.log('✅ 资源清理完成');
});
</script>

<template>
  <div class="chat-view">
    <!-- 页面头部 -->
    <div class="chat-header">
      <div class="header-left">
        <h2>智能聊天</h2>
        <div class="session-info" v-if="currentSession">
          <div class="session-name-wrapper">
            <span class="session-name">{{ currentSession.sessionName || currentSession.name }}</span>
            <el-button 
              :icon="Edit" 
              size="small" 
              text 
              @click="editSessionName"
              class="edit-name-btn"
              title="编辑会话名称"
            />
          </div>
          <span class="session-config">
            {{ selectedBotName }} | {{ selectedKnowledgeBasesText }}
          </span>
        </div>
      </div>
      <div class="header-actions">
        <div class="status-info">
          <UserStatusSelector 
            :current-status="currentUserStatus"
            @status-change="handleStatusChange"
          />
          <span class="online-count">{{ onlineUserCount }} 在线</span>
        </div>
        
        <div class="action-buttons">
          <el-button 
            type="primary" 
            :icon="Plus" 
            @click="createNewSession"
            :loading="botLoading || knowledgeLoading"
          >
            新建会话
          </el-button>
          <el-button 
            :icon="Setting" 
            @click="drawerVisible = true"
          >
            配置
          </el-button>
        </div>
      </div>
    </div>

    <!-- 聊天区域 -->
    <div class="chat-content">
      <div v-if="!currentSession" class="empty-state">
        <el-empty description="请创建一个新会话开始聊天">
          <el-button 
            type="primary" 
            :icon="ChatDotRound"
            @click="createNewSession"
            :loading="botLoading || knowledgeLoading"
          >
            创建会话
          </el-button>
        </el-empty>
      </div>
      
      <ChatArea
        v-else
        :session-id="currentSession.sessionId"
        :messages="messages as any"
        :loading="isLoading"
        @send-message="handleSendMessage"
        @recall-message="handleRecallMessage"
      />
    </div>

    <!-- 配置抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      title="聊天配置"
      direction="ltr"
      size="400px"
    >
      <ConfigPanel
        v-model="config"
        :bot-list="botList as any"
        :knowledge-base-list="knowledgeBaseList as any"
        :content-list="contentList as any"
        :bot-loading="botLoading"
        :knowledge-loading="knowledgeLoading"
        :content-loading="contentLoading"
        :sessions="sessions"
        :current-session-id="currentSession?.sessionId"
        @session-created="createNewSession"
        @session-selected="selectSession"
        @refresh-content="handleRefreshContent"
      />
    </el-drawer>
  </div>
</template>

<style scoped>
.chat-view {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #e4e7ed;
  background: #fafafa;
}

.header-left h2 {
  margin: 0 0 4px 0;
  color: #303133;
  font-size: 18px;
}

.session-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.session-name-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.session-name {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.edit-name-btn {
  padding: 4px;
  min-height: auto;
  opacity: 0.7;
  transition: opacity 0.2s;
}

.edit-name-btn:hover {
  opacity: 1;
}

.session-config {
  font-size: 12px;
  color: #909399;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.online-count {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
  }
  
  .status-info {
    justify-content: space-between;
  }
  
  .action-buttons {
    justify-content: flex-end;
  }
  
  .session-info {
    margin-top: 8px;
  }
}
</style> 