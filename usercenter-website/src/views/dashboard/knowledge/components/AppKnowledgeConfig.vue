<template>
  <div class="knowledge-config">
    <div class="section-header">
      <div class="title-row">
        <span class="title">知识库</span>
        <el-switch 
          v-model="knowledge.enabled" 
          @change="handleToggle"
        />
      </div>
      <div class="section-description">
        您可以通过上传文本文件作为上下文
      </div>
    </div>

    <div v-if="knowledge.enabled" class="knowledge-content">
      <!-- 知识库选择 -->
      <div class="config-item">
        <div class="item-label">选择知识库</div>
        <el-button 
          @click="showKnowledgeDialog" 
          class="add-knowledge-btn"
          :class="{ 'has-selection': knowledge.knowledgeBases.length > 0 }"
        >
          <el-icon><Plus /></el-icon>
          {{ knowledge.knowledgeBases.length > 0 ? `已选择 ${knowledge.knowledgeBases.length} 个知识库` : '添加' }}
        </el-button>
      </div>

      <!-- 已选择的知识库列表 -->
      <div v-if="selectedKnowledgeBases.length > 0" class="selected-knowledge">
        <div class="knowledge-list">
          <div 
            v-for="kb in selectedKnowledgeBases" 
            :key="kb.id"
            class="knowledge-item"
          >
            <div class="kb-info">
              <span class="kb-name">{{ kb.name }}</span>
              <span class="kb-description">{{ kb.description || '暂无描述' }}</span>
            </div>
            <el-button 
              text 
              type="danger" 
              @click="removeKnowledgeBase(kb.id!)"
              size="small"
            >
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 检索设置 -->
      <div class="config-item">
        <div class="item-label">检索设置</div>
        <div class="retrieval-settings">
          <div class="setting-row">
            <span class="setting-label">Top K</span>
            <el-input-number
              v-model="knowledge.retrievalSettings.topK"
              :min="1"
              :max="10"
              size="small"
              @change="handleSettingChange"
            />
          </div>
          <div class="setting-description">
            检索相关性最高的 {{ knowledge.retrievalSettings.topK }} 个文本片段
          </div>

          <div class="setting-row">
            <span class="setting-label">相似度阈值</span>
            <el-input-number
              v-model="knowledge.retrievalSettings.scoreThreshold"
              :min="0"
              :max="1"
              :step="0.1"
              :precision="1"
              size="small"
              @change="handleSettingChange"
            />
          </div>
          <div class="setting-description">
            只返回相似度高于 {{ knowledge.retrievalSettings.scoreThreshold }} 的文本片段
          </div>
        </div>
      </div>
    </div>

    <!-- 知识库选择对话框 -->
    <el-dialog
      v-model="knowledgeDialogVisible"
      title="选择知识库"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="knowledge-selector">
        <div class="search-bar">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索知识库"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <div class="knowledge-list" v-loading="knowledgeLoading">
          <div 
            v-for="kb in filteredKnowledgeBases"
            :key="kb.id"
            class="knowledge-option"
            :class="{ selected: isKnowledgeBaseSelected(kb.id!) }"
            @click="toggleKnowledgeBase(kb)"
          >
            <div class="kb-info">
              <div class="kb-name">{{ kb.name }}</div>
              <div class="kb-description">{{ kb.description || '暂无描述' }}</div>
              <div class="kb-meta">
                <span class="kb-visibility">{{ kb.visibility === 'public' ? '公开' : '私有' }}</span>
                <span class="kb-created">{{ formatTime(kb.createdAt) }}</span>
              </div>
            </div>
            <div class="selection-indicator">
              <el-icon v-if="isKnowledgeBaseSelected(kb.id!)" color="#3b82f6">
                <Check />
              </el-icon>
            </div>
          </div>

          <div v-if="filteredKnowledgeBases.length === 0 && !knowledgeLoading" class="empty-state">
            <div class="empty-text">{{ searchKeyword ? '未找到匹配的知识库' : '暂无知识库' }}</div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="knowledgeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmKnowledgeSelection">
            确定 ({{ tempSelectedIds.size }})
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Close, Search, Check } from '@element-plus/icons-vue'
import { listKnowledgeBases, type KnowledgeBaseDTO } from '@/api/smartcs/knowledgeBase'
import { formatTime } from '@/utils/format'

interface KnowledgeConfig {
  enabled: boolean
  knowledgeBases: number[]
  retrievalSettings: {
    topK: number
    scoreThreshold: number
  }
}

interface Props {
  knowledge: KnowledgeConfig
}

interface Emits {
  (e: 'update:knowledge', value: KnowledgeConfig): void
  (e: 'change'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 响应式数据
const knowledgeDialogVisible = ref(false)
const knowledgeLoading = ref(false)
const searchKeyword = ref('')
const allKnowledgeBases = ref<KnowledgeBaseDTO[]>([])
const tempSelectedIds = ref(new Set<number>())

// 计算属性
const knowledge = computed({
  get: () => props.knowledge,
  set: (value) => emit('update:knowledge', value)
})

const filteredKnowledgeBases = computed(() => {
  if (!searchKeyword.value) {
    return allKnowledgeBases.value
  }
  
  const keyword = searchKeyword.value.toLowerCase()
  return allKnowledgeBases.value.filter(kb => 
    kb.name.toLowerCase().includes(keyword) ||
    (kb.description && kb.description.toLowerCase().includes(keyword))
  )
})

const selectedKnowledgeBases = computed(() => {
  return allKnowledgeBases.value.filter(kb => 
    knowledge.value.knowledgeBases.includes(kb.id!)
  )
})

// 获取知识库列表
const fetchKnowledgeBases = async () => {
  try {
    knowledgeLoading.value = true
    const response = await listKnowledgeBases({
      pageIndex: 1,
      pageSize: 100
    })
    
    if (response.success) {
      allKnowledgeBases.value = response.data
    } else {
      throw new Error(response.errMessage || '获取知识库列表失败')
    }
  } catch (error) {
    console.error('获取知识库列表失败:', error)
    ElMessage.error('获取知识库列表失败')
  } finally {
    knowledgeLoading.value = false
  }
}

// 处理开关切换
const handleToggle = () => {
  emit('change')
}

// 处理设置变更
const handleSettingChange = () => {
  emit('change')
}

// 显示知识库选择对话框
const showKnowledgeDialog = () => {
  tempSelectedIds.value = new Set(knowledge.value.knowledgeBases)
  knowledgeDialogVisible.value = true
  fetchKnowledgeBases()
}

// 检查知识库是否已选择
const isKnowledgeBaseSelected = (id: number) => {
  return tempSelectedIds.value.has(id)
}

// 切换知识库选择状态
const toggleKnowledgeBase = (kb: KnowledgeBaseDTO) => {
  if (tempSelectedIds.value.has(kb.id!)) {
    tempSelectedIds.value.delete(kb.id!)
  } else {
    tempSelectedIds.value.add(kb.id!)
  }
}

// 移除知识库
const removeKnowledgeBase = (id: number) => {
  const newKnowledgeBases = knowledge.value.knowledgeBases.filter(kbId => kbId !== id)
  const newKnowledge = {
    ...knowledge.value,
    knowledgeBases: newKnowledgeBases
  }
  
  emit('update:knowledge', newKnowledge)
  emit('change')
}

// 确认知识库选择
const confirmKnowledgeSelection = () => {
  const newKnowledge = {
    ...knowledge.value,
    knowledgeBases: Array.from(tempSelectedIds.value)
  }
  
  emit('update:knowledge', newKnowledge)
  emit('change')
  knowledgeDialogVisible.value = false
  
  ElMessage.success(`已选择 ${tempSelectedIds.value.size} 个知识库`)
}

onMounted(() => {
  // 组件挂载时可以预加载知识库列表
})
</script>

<style scoped lang="scss">
.knowledge-config {
  .section-header {
    margin-bottom: 16px;

    .title-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .title {
        font-weight: 500;
        color: #374151;
        font-size: 14px;
      }
    }

    .section-description {
      font-size: 12px;
      color: #6b7280;
      line-height: 1.4;
    }
  }

  .knowledge-content {
    .config-item {
      margin-bottom: 20px;

      .item-label {
        font-size: 13px;
        font-weight: 500;
        color: #374151;
        margin-bottom: 8px;
      }

      .add-knowledge-btn {
        width: 100%;
        height: 40px;
        border: 2px dashed #d1d5db;
        background: #f9fafb;
        color: #6b7280;
        border-radius: 8px;

        &:hover {
          border-color: #3b82f6;
          color: #3b82f6;
        }

        &.has-selection {
          border-color: #3b82f6;
          background: #eff6ff;
          color: #3b82f6;
          border-style: solid;
        }
      }
    }

    .selected-knowledge {
      margin-bottom: 20px;

      .knowledge-list {
        .knowledge-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 12px;
          border: 1px solid #e5e7eb;
          border-radius: 8px;
          margin-bottom: 8px;
          background: #fff;

          .kb-info {
            flex: 1;

            .kb-name {
              font-weight: 500;
              color: #1f2937;
              font-size: 14px;
              display: block;
              margin-bottom: 4px;
            }

            .kb-description {
              font-size: 12px;
              color: #6b7280;
              line-height: 1.4;
            }
          }
        }
      }
    }

    .retrieval-settings {
      .setting-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .setting-label {
          font-size: 13px;
          color: #374151;
        }
      }

      .setting-description {
        font-size: 12px;
        color: #6b7280;
        margin-bottom: 16px;
        line-height: 1.4;
      }
    }
  }

  .knowledge-selector {
    .search-bar {
      margin-bottom: 16px;
    }

    .knowledge-list {
      max-height: 400px;
      overflow-y: auto;

      .knowledge-option {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 16px;
        border: 1px solid #e5e7eb;
        border-radius: 8px;
        margin-bottom: 8px;
        cursor: pointer;
        transition: all 0.2s;

        &:hover {
          border-color: #3b82f6;
          background: #f8fafc;
        }

        &.selected {
          border-color: #3b82f6;
          background: #eff6ff;
        }

        .kb-info {
          flex: 1;

          .kb-name {
            font-weight: 500;
            color: #1f2937;
            font-size: 14px;
            margin-bottom: 4px;
          }

          .kb-description {
            font-size: 12px;
            color: #6b7280;
            line-height: 1.4;
            margin-bottom: 8px;
          }

          .kb-meta {
            display: flex;
            gap: 12px;
            font-size: 11px;
            color: #9ca3af;

            .kb-visibility {
              padding: 2px 6px;
              background: #f3f4f6;
              border-radius: 4px;
            }
          }
        }

        .selection-indicator {
          width: 24px;
          height: 24px;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }

      .empty-state {
        text-align: center;
        padding: 40px 20px;
        color: #6b7280;
      }
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}
</style>