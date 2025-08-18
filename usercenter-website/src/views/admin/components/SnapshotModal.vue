<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="$emit('update:visible', $event)"
    title="快照详情"
    width="1200px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <div v-if="snapshotData" class="snapshot-detail">
      <!-- 基本信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
            <el-tag 
              :type="getStatusTagType(snapshotData.status)"
              size="large"
            >
              {{ getStatusText(snapshotData.status) }}
            </el-tag>
          </div>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="info-item">
              <span class="info-label">快照代码:</span>
              <span class="info-value">{{ snapshotData.code }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <span class="info-label">快照名称:</span>
              <span class="info-value">{{ snapshotData.name }}</span>
            </div>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="info-item">
              <span class="info-label">创建时间:</span>
              <span class="info-value">{{ formatDate(snapshotData.createdAt) }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <span class="info-label">作用域:</span>
              <el-tag type="info" size="small">{{ snapshotData.scope || 'global' }}</el-tag>
            </div>
          </el-col>
        </el-row>
        
        <div class="info-item">
          <span class="info-label">描述:</span>
          <span class="info-value">{{ snapshotData.description || '无描述' }}</span>
        </div>
        
        <div v-if="snapshotData.etag" class="info-item">
          <span class="info-label">ETag:</span>
          <el-text class="etag-text" size="small" type="info">{{ snapshotData.etag }}</el-text>
        </div>
      </el-card>

      <!-- 意图列表 -->
      <el-card class="intents-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>包含的意图 ({{ (snapshotData.items || []).length }})</span>
            <div class="header-actions">
              <el-button 
                size="small" 
                @click="exportConfig"
                :icon="Download"
              >
                导出配置
              </el-button>
              <el-button 
                size="small" 
                @click="previewRuntimeConfig"
                :icon="View"
              >
                预览运行时配置
              </el-button>
            </div>
          </div>
        </template>

        <el-table 
          :data="snapshotData.items || []" 
          style="width: 100%"
          max-height="400"
        >
          <el-table-column prop="intentCode" label="意图代码" width="150" />
          <el-table-column prop="intentName" label="意图名称" width="200" />
          <el-table-column prop="version" label="版本" width="100" align="center">
            <template #default="scope">
              <el-tag size="small">v{{ scope.row.version || '1.0' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="标签数量" width="100" align="center">
            <template #default="scope">
              <el-badge 
                :value="(scope.row.labels || []).length" 
                type="primary"
              >
                <el-button 
                  v-if="(scope.row.labels || []).length > 0"
                  size="small" 
                  text 
                  @click="showLabels(scope.row)"
                >
                  查看
                </el-button>
                <span v-else>-</span>
              </el-badge>
            </template>
          </el-table-column>
          <el-table-column label="边界数量" width="100" align="center">
            <template #default="scope">
              <el-badge 
                :value="(scope.row.boundaries || []).length" 
                type="info"
              >
                <el-button 
                  v-if="(scope.row.boundaries || []).length > 0"
                  size="small" 
                  text 
                  @click="showBoundaries(scope.row)"
                >
                  查看
                </el-button>
                <span v-else>-</span>
              </el-badge>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template #default="scope">
              <el-button 
                size="small" 
                type="primary" 
                text
                @click="editIntent(scope.row)"
                :icon="Edit"
              >
                编辑
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-empty 
          v-if="!(snapshotData.items || []).length" 
          description="暂无意图数据"
          :image-size="80"
        />
      </el-card>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="$emit('update:visible', false)">关闭</el-button>
        <el-button
          v-if="snapshotData?.status !== 'ACTIVE'"
          type="primary"
          @click="publishSnapshot"
          :loading="publishing"
          :icon="VideoPlay"
        >
          发布快照
        </el-button>
      </span>
    </template>

    <!-- 运行时配置预览模态框 -->
    <el-dialog
      v-model="runtimeConfigVisible"
      title="运行时配置预览"
      width="800px"
      :close-on-click-modal="false"
    >
      <div class="config-preview">
        <div class="config-header">
          <span>配置JSON</span>
          <el-button 
            size="small" 
            @click="copyConfig"
            :icon="CopyDocument"
          >
            复制
          </el-button>
        </div>
        <el-input
          v-model="runtimeConfigJson"
          type="textarea"
          :rows="20"
          readonly
          class="config-textarea"
        />
      </div>
    </el-dialog>

    <!-- 标签/边界展示模态框 -->
    <el-dialog
      v-model="labelsVisible"
      :title="labelsTitle"
      width="600px"
    >
      <div class="labels-display">
        <el-tag
          v-for="(item, index) in displayItems"
          :key="index"
          :type="labelsType"
          size="large"
          class="display-tag"
        >
          {{ item }}
        </el-tag>
      </div>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Download, 
  View, 
  Edit, 
  VideoPlay,
  CopyDocument
} from '@element-plus/icons-vue'
import { useSmartCSAdminStore } from '@/stores/admin/admin.js'
import { classificationApi } from '@/api/smartcs/intent'

// Props & Emits
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  snapshotData: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:visible', 'refresh'])

// Store
const store = useSmartCSAdminStore()

// 响应式数据
const publishing = ref(false)
const runtimeConfigVisible = ref(false)
const runtimeConfigJson = ref('')
const labelsVisible = ref(false)
const labelsTitle = ref('')
const labelsType = ref('primary')
const displayItems = ref([])

// 方法
const getStatusTagType = (status) => {
  switch (status) {
    case 'ACTIVE':
      return 'success'
    case 'DRAFT':
      return 'warning'
    case 'INACTIVE':
      return 'info'
    default:
      return 'info'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 'ACTIVE':
      return '激活'
    case 'DRAFT':
      return '草稿'
    case 'INACTIVE':
      return '停用'
    default:
      return '未知'
  }
}

const formatDate = (timestamp) => {
  if (!timestamp) return ''
  return new Date(timestamp).toLocaleString('zh-CN')
}

const publishSnapshot = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要发布快照 "${props.snapshotData.name}" 吗？发布后将替换当前激活的快照。`,
      '确认发布',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    publishing.value = true
    await store.publishSnapshot(props.snapshotData.id)
    ElMessage.success('快照发布成功')
    emit('refresh')
    emit('update:visible', false)
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('快照发布失败: ' + error.message)
    }
  } finally {
    publishing.value = false
  }
}

const exportConfig = async () => {
  try {
    const { snapshotApi } = await import('@/api/smartcs/intent')
    const response = await snapshotApi.exportSnapshotConfig(props.snapshotData.id)
    if (response.success) {
      // 创建下载链接
      const blob = new Blob([JSON.stringify(response.data, null, 2)], {
        type: 'application/json'
      })
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `snapshot-${props.snapshotData.code}-config.json`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      window.URL.revokeObjectURL(url)
      
      ElMessage.success('配置导出成功')
    }
  } catch (error) {
    ElMessage.error('配置导出失败: ' + error.message)
  }
}

const previewRuntimeConfig = async () => {
  try {
    const response = await classificationApi.getRuntimeConfig({
      channel: 'web',
      tenant: 'default',
      region: 'cn',
      env: 'prod'
    })
    
    runtimeConfigJson.value = JSON.stringify(response, null, 2)
    runtimeConfigVisible.value = true
  } catch (error) {
    ElMessage.error('获取运行时配置失败: ' + error.message)
  }
}

const copyConfig = async () => {
  try {
    await navigator.clipboard.writeText(runtimeConfigJson.value)
    ElMessage.success('配置已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败，请手动复制')
  }
}

const showLabels = (intent) => {
  labelsTitle.value = `${intent.intentCode} 的标签`
  labelsType.value = 'primary'
  displayItems.value = intent.labels || []
  labelsVisible.value = true
}

const showBoundaries = (intent) => {
  labelsTitle.value = `${intent.intentCode} 的边界`
  labelsType.value = 'info'
  displayItems.value = intent.boundaries || []
  labelsVisible.value = true
}

const editIntent = (intent) => {
  // TODO: 实现意图编辑功能
  ElMessage.info('意图编辑功能开发中...')
}
</script>

<style scoped>
.snapshot-detail {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.info-card,
.intents-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  color: #2c3e50;
}

.header-actions {
  display: flex;
  gap: 0.5rem;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
  min-height: 32px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-label {
  font-weight: 500;
  color: #6c757d;
  width: 100px;
  flex-shrink: 0;
}

.info-value {
  color: #2c3e50;
  flex: 1;
}

.etag-text {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  word-break: break-all;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.config-preview {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.config-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  color: #2c3e50;
}

.config-textarea {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.labels-display {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.display-tag {
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    gap: 0.5rem;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .info-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .info-label {
    width: auto;
    margin-bottom: 0.25rem;
  }
}
</style>