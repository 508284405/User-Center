<script setup lang="ts">
import { computed, watch } from 'vue';
import { Document, ChatDotRound, Delete, Check, Files } from '@element-plus/icons-vue';
import { BotConfig, KnowledgeBase, Content, ChatConfig, Session } from '@/types/chat';
import { formatShortTime } from '@/utils/dateFormat';

// Props
interface Props {
  modelValue: ChatConfig;
  botList: BotConfig[];
  knowledgeBaseList: KnowledgeBase[];
  contentList: Content[];
  botLoading: boolean;
  knowledgeLoading: boolean;
  contentLoading: boolean;
  sessions: Session[];
  currentSessionId?: string;
}

const props = defineProps<Props>();

// Emits
interface Emits {
  (e: 'update:modelValue', value: ChatConfig): void;
  (e: 'session-created'): void;
  (e: 'session-selected', session: Session): void;
  (e: 'refresh-content'): void;
}

const emit = defineEmits<Emits>();

// 计算属性
const config = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

// 选择机器人
const selectBot = (botId: number) => {
  config.value.selectedBotId = botId;
};

// 选择知识库
const selectKnowledgeBase = (knowledgeBaseId: number) => {
  console.log('ConfigPanel: 选择知识库', knowledgeBaseId);
  config.value.selectedKnowledgeBaseId = knowledgeBaseId;
  config.value.selectedContentId = undefined; // 清空内容选择
  // 触发父组件的知识库选择逻辑
  emit('update:modelValue', config.value);
  console.log('ConfigPanel: 已触发update:modelValue事件', config.value);
};

// 选择内容
const selectContent = (contentId: number | undefined) => {
  config.value.selectedContentId = contentId;
};

// 选择会话
const selectSession = (session: Session) => {
  emit('session-selected', session);
};

// 清空知识库选择
const clearKnowledgeBase = () => {
  console.log('ConfigPanel: 清空知识库选择');
  config.value.selectedKnowledgeBaseId = undefined;
  config.value.selectedContentId = undefined;
  emit('update:modelValue', config.value);
};

// 刷新内容列表
const refreshContent = () => {
  if (config.value.selectedKnowledgeBaseId) {
    console.log('ConfigPanel: 手动刷新内容列表');
    emit('refresh-content');
  }
};

// 创建新会话
const createNewSession = () => {
  emit('session-created');
};

// 监听知识库选择变化，触发内容加载
watch(() => config.value.selectedKnowledgeBaseId, (newKnowledgeBaseId) => {
  if (newKnowledgeBaseId) {
    console.log('ConfigPanel: 监听到知识库选择变化，触发内容加载:', newKnowledgeBaseId);
    // 通过emit触发父组件的知识库选择逻辑
    emit('update:modelValue', config.value);
  }
}, { immediate: true });
</script>

<template>
  <div class="config-panel">
    <!-- 机器人选择 -->
    <div class="config-section">
      <div class="section-header">
        <el-icon><Document /></el-icon>
        <span>选择机器人</span>
      </div>
      
      <div v-loading="botLoading" class="section-content">
        <div v-if="botList.length === 0" class="empty-hint">
          暂无可用机器人
        </div>
        <div v-else class="bot-list">
          <div
            v-for="bot in botList"
            :key="bot.botId"
            class="bot-item"
            :class="{ active: config.selectedBotId === bot.botId }"
            @click="selectBot(bot.botId)"
          >
            <div class="bot-info">
              <div class="bot-name">{{ bot.botName }}</div>
              <div class="bot-model">{{ bot.modelName }}</div>
              <div class="bot-tags">
                <el-tag size="small" type="info">{{ bot.vendor }}</el-tag>
                <el-tag size="small" type="success">{{ bot.modelType }}</el-tag>
              </div>
            </div>
            <div class="bot-status">
              <el-icon v-if="config.selectedBotId === bot.botId" color="#409eff">
                <Check />
              </el-icon>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 知识库选择 -->
    <div class="config-section">
      <div class="section-header">
        <el-icon><Document /></el-icon>
        <span>选择知识库</span>
        <span class="selected-count" v-if="config.selectedKnowledgeBaseId">
          (已选择)
        </span>
      </div>
      
      <div v-loading="knowledgeLoading" class="section-content">
        <div v-if="knowledgeBaseList.length === 0" class="empty-hint">
          暂无可用知识库
        </div>
        <div v-else class="knowledge-list">
          <!-- 不选择知识库选项 -->
          <div
            class="knowledge-item none-option"
            :class="{ active: !config.selectedKnowledgeBaseId }"
            @click="clearKnowledgeBase"
          >
            <div class="knowledge-info">
              <div class="knowledge-name">不选择知识库</div>
              <div class="knowledge-desc">不使用任何知识库进行对话</div>
            </div>
            <div class="knowledge-status">
              <el-radio
                :model-value="config.selectedKnowledgeBaseId"
                :value="undefined"
                @change="clearKnowledgeBase"
              />
            </div>
          </div>
          
          <div
            v-for="kb in knowledgeBaseList"
            :key="kb.id"
            class="knowledge-item"
            :class="{ active: config.selectedKnowledgeBaseId === kb.id }"
            @click="selectKnowledgeBase(kb.id)"
          >
            <div class="knowledge-info">
              <div class="knowledge-name">{{ kb.name }}</div>
              <div class="knowledge-desc" v-if="kb.description">
                {{ kb.description }}
              </div>
              <div class="knowledge-meta" v-if="kb.docCount">
                文档数量: {{ kb.docCount }}
              </div>
            </div>
            <div class="knowledge-status">
              <el-radio
                :model-value="config.selectedKnowledgeBaseId"
                :value="kb.id"
                @change="selectKnowledgeBase(kb.id)"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 内容选择 -->
    <div class="config-section" v-if="config.selectedKnowledgeBaseId">
      <div class="section-header">
        <el-icon><Files /></el-icon>
        <span>选择内容</span>
        <span class="selected-count" v-if="config.selectedContentId">
          (已选择)
        </span>
        <el-button 
          size="small" 
          text 
          @click="refreshContent"
          :loading="contentLoading"
          style="margin-left: auto;"
        >
          刷新
        </el-button>
      </div>
      
      <div v-loading="contentLoading" class="section-content">
        <div v-if="contentList.length === 0" class="empty-hint">
          该知识库暂无内容
        </div>
        <div v-else class="content-list">
          <div
            class="content-item none-option"
            :class="{ active: !config.selectedContentId }"
            @click="selectContent(undefined)"
          >
            <div class="content-info">
              <div class="content-name">不选择任何内容</div>
              <div class="content-desc">使用整个知识库进行检索</div>
            </div>
            <div class="content-status">
              <el-radio
                :model-value="config.selectedContentId"
                :value="undefined"
                @change="selectContent(undefined)"
              />
            </div>
          </div>
          <div
            v-for="content in contentList"
            :key="content.id"
            class="content-item"
            :class="{ active: config.selectedContentId === content.id }"
            @click="selectContent(content.id)"
          >
            <div class="content-info">
              <div class="content-name">{{ content.title }}</div>
              <div class="content-desc">{{ content.contentType }}</div>
              <div class="content-meta">状态: {{ content.status }}</div>
            </div>
            <div class="content-status">
              <el-radio
                :model-value="config.selectedContentId"
                :value="content.id"
                @change="selectContent(content.id)"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 会话管理 -->
    <div class="config-section">
      <div class="section-header">
        <el-icon><ChatDotRound /></el-icon>
        <span>会话管理</span>
      </div>
      
      <div class="section-content">
        <div class="session-actions">
          <el-button 
            type="primary" 
            size="small" 
            @click="createNewSession"
            style="width: 100%"
          >
            创建新会话
          </el-button>
        </div>
        
        <div v-if="sessions.length === 0" class="empty-hint">
          暂无会话记录
        </div>
        <div v-else class="session-list">
          <div
            v-for="session in sessions"
            :key="session.sessionId"
            class="session-item"
            :class="{ active: currentSessionId === session.sessionId }"
            @click="selectSession(session)"
          >
            <div class="session-info">
              <div class="session-name">{{ session.sessionName || session.name }}</div>
              <div class="session-meta">
                <span class="session-time">
                  {{ session.lastMsgTime ? formatShortTime(session.lastMsgTime) : formatShortTime(session.createdAt) }}
                </span>
                <span class="session-bot" v-if="session.botName">
                  {{ session.botName }}
                </span>
              </div>
              <div class="session-knowledge" v-if="session.knowledgeBaseName">
                知识库: {{ session.knowledgeBaseName }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.config-panel {
  padding: 16px;
  height: 100%;
  overflow-y: auto;
}

.config-section {
  margin-bottom: 24px;
}

.config-section:last-child {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-weight: 500;
  color: #303133;
  position: relative;
}

.selected-count {
  color: #409eff;
  font-size: 12px;
}

.section-content {
  min-height: 60px;
}

.empty-hint {
  text-align: center;
  color: #909399;
  font-size: 14px;
  padding: 20px;
}

/* 机器人列表样式 */
.bot-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.bot-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.bot-item:hover {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.bot-item.active {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.bot-info {
  flex: 1;
}

.bot-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.bot-model {
  font-size: 12px;
  color: #606266;
  margin-bottom: 6px;
}

.bot-tags {
  display: flex;
  gap: 4px;
}

.bot-status {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 知识库列表样式 */
.knowledge-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.knowledge-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.knowledge-item:hover {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.knowledge-item.active {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.knowledge-item.none-option {
  border-style: dashed;
  background-color: #f9f9f9;
}

.knowledge-item.none-option.active {
  background-color: #ecf5ff;
}

.knowledge-info {
  flex: 1;
  margin-right: 12px;
}

.knowledge-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.knowledge-desc {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
  line-height: 1.4;
}

.knowledge-meta {
  font-size: 11px;
  color: #909399;
}

.knowledge-status {
  display: flex;
  align-items: center;
}

/* 内容列表样式 */
.content-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.content-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.content-item:hover {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.content-item.active {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.content-item.none-option {
  border-style: dashed;
  background-color: #f9f9f9;
}

.content-item.none-option.active {
  background-color: #ecf5ff;
}

.content-info {
  flex: 1;
  margin-right: 12px;
}

.content-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.content-desc {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
  line-height: 1.4;
}

.content-meta {
  font-size: 11px;
  color: #909399;
}

.content-status {
  display: flex;
  align-items: center;
}

/* 会话列表样式 */
.session-actions {
  margin-bottom: 16px;
}

.session-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.session-item {
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.session-item:hover {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.session-item.active {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.session-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.session-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.session-time {
  font-size: 11px;
  color: #909399;
}

.session-bot {
  font-size: 11px;
  color: #606266;
}

.session-knowledge {
  font-size: 11px;
  color: #909399;
  line-height: 1.4;
}
</style> 