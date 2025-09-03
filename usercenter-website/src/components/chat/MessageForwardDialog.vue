<template>
  <el-dialog
    v-model="visible"
    title="转发消息"
    width="600px"
    :before-close="handleClose"
  >
    <div class="forward-dialog-content">
      <!-- 转发消息预览 -->
      <div class="forward-messages-preview">
        <h4>转发内容预览</h4>
        <div class="messages-preview-list">
          <div 
            v-for="message in forwardMessages" 
            :key="message.msgId"
            class="preview-message-item"
          >
            <div class="message-sender">
              {{ getSenderDisplayName(message) }}
            </div>
            <div class="message-content">
              <div v-if="message.msgType === MessageContentType.TEXT">
                {{ message.content }}
              </div>
              <div v-else-if="message.msgType === MessageContentType.IMAGE">
                [图片]
              </div>
              <div v-else-if="message.msgType === MessageContentType.ORDER_CARD">
                [订单卡片]
              </div>
              <div v-else>
                [其他类型消息]
              </div>
            </div>
            <div class="message-time">
              {{ MessageUtils.formatTime(message.timestamp) }}
            </div>
          </div>
        </div>
      </div>

      <!-- 转发目标选择 -->
      <div class="forward-target-section">
        <h4>选择转发目标</h4>
        
        <!-- 搜索框 -->
        <el-input
          v-model="searchKeyword"
          placeholder="搜索会话或用户"
          prefix-icon="Search"
          clearable
          class="search-input"
        />
        
        <!-- 目标列表 -->
        <div class="target-tabs">
          <el-tabs v-model="activeTab" @tab-click="handleTabClick">
            <el-tab-pane label="最近会话" name="sessions">
              <div class="target-list" v-loading="sessionsLoading">
                <div
                  v-for="session in filteredSessions"
                  :key="session.sessionId"
                  class="target-item"
                  :class="{ selected: selectedTargets.some(t => t.id === session.sessionId && t.type === 'session') }"
                  @click="toggleTarget('session', session.sessionId, session)"
                >
                  <el-checkbox 
                    :model-value="selectedTargets.some(t => t.id === session.sessionId && t.type === 'session')"
                    @click.stop
                  />
                  <div class="target-info">
                    <div class="target-name">会话{{ session.sessionId }}</div>
                    <div class="target-desc">客户{{ session.customerId }}</div>
                  </div>
                  <div class="target-time">{{ formatTime(session.lastMsgTime) }}</div>
                </div>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="联系人" name="contacts">
              <div class="target-list" v-loading="contactsLoading">
                <div
                  v-for="contact in filteredContacts"
                  :key="contact.id"
                  class="target-item"
                  :class="{ selected: selectedTargets.some(t => t.id === contact.id && t.type === 'contact') }"
                  @click="toggleTarget('contact', contact.id, contact)"
                >
                  <el-checkbox 
                    :model-value="selectedTargets.some(t => t.id === contact.id && t.type === 'contact')"
                    @click.stop
                  />
                  <div class="target-info">
                    <div class="target-name">{{ contact.name || contact.username }}</div>
                    <div class="target-desc">{{ contact.email || contact.phone }}</div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>

      <!-- 已选择的转发目标 -->
      <div v-if="selectedTargets.length > 0" class="selected-targets">
        <h4>已选择转发目标 ({{ selectedTargets.length }})</h4>
        <div class="selected-list">
          <el-tag
            v-for="target in selectedTargets"
            :key="`${target.type}-${target.id}`"
            closable
            @close="removeTarget(target)"
            class="target-tag"
          >
            {{ getTargetDisplayName(target) }}
          </el-tag>
        </div>
      </div>

      <!-- 转发选项 -->
      <div class="forward-options">
        <el-checkbox v-model="includeOriginalSender">包含原发送者信息</el-checkbox>
        <el-checkbox v-model="combineMessages">合并为一条消息</el-checkbox>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleForward"
          :disabled="selectedTargets.length === 0"
          :loading="forwarding"
        >
          转发 ({{ selectedTargets.length }})
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Message, MessageContentType, Session } from '@/types/chat';
import { MessageUtils } from '@/utils/messageUtils';
import { chatSessionApi } from '@/api/smartcs/chatSession';
import { formatDate } from '@/utils/format';

// 接口定义
interface ForwardTarget {
  type: 'session' | 'contact';
  id: string;
  name: string;
  data: any;
}

interface Contact {
  id: string;
  name?: string;
  username?: string;
  email?: string;
  phone?: string;
}

interface Props {
  modelValue: boolean;
  messages: Message[];
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void;
  (e: 'forward', targets: ForwardTarget[], messages: Message[], options: {
    includeOriginalSender: boolean;
    combineMessages: boolean;
  }): void;
}

const props = defineProps<Props>();
const emits = defineEmits<Emits>();

// 响应式数据
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emits('update:modelValue', value)
});

const forwardMessages = computed(() => props.messages);

const searchKeyword = ref('');
const activeTab = ref('sessions');
const selectedTargets = ref<ForwardTarget[]>([]);
const includeOriginalSender = ref(true);
const combineMessages = ref(false);
const forwarding = ref(false);

// 数据加载状态
const sessionsLoading = ref(false);
const contactsLoading = ref(false);

// 会话和联系人列表
const sessions = ref<Session[]>([]);
const contacts = ref<Contact[]>([]);

// 过滤后的列表
const filteredSessions = computed(() => {
  if (!searchKeyword.value) return sessions.value;
  
  const keyword = searchKeyword.value.toLowerCase();
  return sessions.value.filter(session => 
    String(session.sessionId).includes(keyword) ||
    String(session.customerId).includes(keyword) ||
    (session.sessionName && session.sessionName.toLowerCase().includes(keyword))
  );
});

const filteredContacts = computed(() => {
  if (!searchKeyword.value) return contacts.value;
  
  const keyword = searchKeyword.value.toLowerCase();
  return contacts.value.filter(contact =>
    (contact.name && contact.name.toLowerCase().includes(keyword)) ||
    (contact.username && contact.username.toLowerCase().includes(keyword)) ||
    (contact.email && contact.email.toLowerCase().includes(keyword)) ||
    (contact.phone && contact.phone.includes(keyword))
  );
});

// 获取发送者显示名称
const getSenderDisplayName = (message: Message) => {
  return MessageUtils.getSenderDisplayName(message);
};

// 获取目标显示名称
const getTargetDisplayName = (target: ForwardTarget) => {
  if (target.type === 'session') {
    return `会话${target.id}`;
  } else {
    return target.name;
  }
};

// 格式化时间
const formatTime = (time: any) => {
  if (!time) return '';
  return formatDate(time);
};

// 切换选择目标
const toggleTarget = (type: 'session' | 'contact', id: string, data: any) => {
  const existingIndex = selectedTargets.value.findIndex(t => t.type === type && t.id === id);
  
  if (existingIndex !== -1) {
    selectedTargets.value.splice(existingIndex, 1);
  } else {
    const name = type === 'session' 
      ? `会话${id}` 
      : (data.name || data.username || `联系人${id}`);
    
    selectedTargets.value.push({
      type,
      id,
      name,
      data
    });
  }
};

// 移除目标
const removeTarget = (target: ForwardTarget) => {
  const index = selectedTargets.value.findIndex(t => 
    t.type === target.type && t.id === target.id
  );
  if (index !== -1) {
    selectedTargets.value.splice(index, 1);
  }
};

// 处理标签页点击
const handleTabClick = (tab: any) => {
  if (tab.props.name === 'contacts' && contacts.value.length === 0) {
    loadContacts();
  }
};

// 加载会话列表
const loadSessions = async () => {
  sessionsLoading.value = true;
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    const agentId = userInfo.id;
    
    if (!agentId) {
      ElMessage.error('未获取到客服ID');
      return;
    }
    
    const sessionList = await chatSessionApi.getAgentActiveSessions(agentId);
    sessions.value = sessionList;
  } catch (error) {
    console.error('加载会话列表失败:', error);
    ElMessage.error('加载会话列表失败');
  } finally {
    sessionsLoading.value = false;
  }
};

// 加载联系人列表（模拟数据）
const loadContacts = async () => {
  contactsLoading.value = true;
  try {
    // 这里应该调用实际的联系人API
    await new Promise(resolve => setTimeout(resolve, 500)); // 模拟API调用
    
    // 模拟联系人数据
    contacts.value = [
      { id: '1', name: '客服主管', username: 'supervisor', email: 'supervisor@company.com' },
      { id: '2', name: '技术支持', username: 'tech', email: 'tech@company.com' },
      { id: '3', name: '销售经理', username: 'sales', email: 'sales@company.com' }
    ];
  } catch (error) {
    console.error('加载联系人失败:', error);
    ElMessage.error('加载联系人失败');
  } finally {
    contactsLoading.value = false;
  }
};

// 处理转发
const handleForward = async () => {
  if (selectedTargets.value.length === 0) {
    ElMessage.warning('请选择转发目标');
    return;
  }
  
  if (forwardMessages.value.length === 0) {
    ElMessage.warning('没有可转发的消息');
    return;
  }
  
  forwarding.value = true;
  
  try {
    // 发出转发事件
    emits('forward', selectedTargets.value, forwardMessages.value, {
      includeOriginalSender: includeOriginalSender.value,
      combineMessages: combineMessages.value
    });
    
    ElMessage.success(`已转发至 ${selectedTargets.value.length} 个目标`);
    handleClose();
  } catch (error) {
    console.error('转发失败:', error);
    ElMessage.error('转发失败');
  } finally {
    forwarding.value = false;
  }
};

// 处理关闭
const handleClose = () => {
  visible.value = false;
  // 重置状态
  selectedTargets.value = [];
  searchKeyword.value = '';
  activeTab.value = 'sessions';
  includeOriginalSender.value = true;
  combineMessages.value = false;
};

// 监听对话框打开
watch(visible, (newValue) => {
  if (newValue) {
    loadSessions();
  }
});

onMounted(() => {
  // 组件挂载时不需要立即加载数据，等对话框打开时再加载
});
</script>

<style scoped>
.forward-dialog-content {
  max-height: 600px;
  overflow-y: auto;
}

.forward-messages-preview {
  margin-bottom: 24px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.forward-messages-preview h4 {
  margin: 0 0 12px 0;
  color: #303133;
}

.messages-preview-list {
  max-height: 200px;
  overflow-y: auto;
}

.preview-message-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
  padding: 8px;
  background: white;
  border-radius: 4px;
  border-left: 3px solid #409eff;
}

.preview-message-item:last-child {
  margin-bottom: 0;
}

.message-sender {
  font-weight: bold;
  font-size: 12px;
  color: #409eff;
  margin-right: 8px;
  min-width: 60px;
}

.message-content {
  flex: 1;
  font-size: 14px;
  color: #606266;
  word-break: break-word;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
  white-space: nowrap;
}

.forward-target-section h4 {
  margin: 0 0 12px 0;
  color: #303133;
}

.search-input {
  margin-bottom: 16px;
}

.target-tabs {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.target-list {
  max-height: 300px;
  overflow-y: auto;
}

.target-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.target-item:hover {
  background-color: #f5f7fa;
}

.target-item.selected {
  background-color: #ecf5ff;
}

.target-item:last-child {
  border-bottom: none;
}

.target-info {
  flex: 1;
  margin-left: 12px;
  overflow: hidden;
}

.target-name {
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.target-desc {
  font-size: 12px;
  color: #909399;
}

.target-time {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.selected-targets {
  margin: 24px 0 16px 0;
  padding: 16px;
  background: #f0f9ff;
  border-radius: 8px;
}

.selected-targets h4 {
  margin: 0 0 12px 0;
  color: #303133;
}

.selected-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.target-tag {
  margin: 0;
}

.forward-options {
  margin: 16px 0;
  padding: 16px;
  background: #fafbfc;
  border-radius: 8px;
}

.forward-options :deep(.el-checkbox) {
  display: block;
  margin-bottom: 8px;
}

.forward-options :deep(.el-checkbox:last-child) {
  margin-bottom: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>