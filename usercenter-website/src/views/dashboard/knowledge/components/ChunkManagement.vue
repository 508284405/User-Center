<template>
  <div class="chunk-management">
    <!-- 工具栏 -->
    <div class="toolbar" v-if="chunks.length > 0">
      <div class="toolbar-left">
        <el-checkbox 
          v-model="allSelected"
          :indeterminate="indeterminate"
          @change="handleSelectAll"
        >
          全选
        </el-checkbox>
        <span class="selection-info" v-if="selectedChunks.length > 0">
          已选择 {{ selectedChunks.length }} 项
        </span>
      </div>
      
      <div class="toolbar-right" v-if="selectedChunks.length > 0">
        <el-button 
          size="small" 
          @click="handleBatchEnable"
          :disabled="processing"
        >
          批量启用
        </el-button>
        <el-button 
          size="small" 
          @click="handleBatchDisable"
          :disabled="processing"
        >
          批量禁用
        </el-button>
        <el-button 
          size="small" 
          type="danger"
          @click="handleBatchDelete"
          :disabled="processing"
        >
          批量删除
        </el-button>
      </div>
    </div>

    <!-- 分块列表 -->
    <div class="chunks-container" v-loading="loading">
      <div v-if="chunks.length === 0 && !loading" class="empty-state">
        <el-empty 
          description="暂无分块数据" 
          :image-size="80"
        />
      </div>

      <div v-else class="chunks-list">
        <div 
          v-for="(chunk, index) in chunks" 
          :key="chunk.id"
          class="chunk-item"
          :class="{ 
            'selected': selectedChunks.includes(chunk.id),
            'disabled': chunk.status === 'disabled' 
          }"
        >
          <!-- 分块头部 -->
          <div class="chunk-header">
            <div class="chunk-info">
              <el-checkbox 
                v-model="selectedChunks"
                :value="chunk.id"
                @change="handleChunkSelect"
              />
              <div class="chunk-index">
                <span class="index-label">块分段-{{ String(index + 1).padStart(2, '0') }}</span>
                <span class="chunk-size">{{ chunk.content?.length || 0 }} 字符</span>
              </div>
            </div>
            
            <div class="chunk-actions">
              <el-tag 
                :type="chunk.status === 'enabled' ? 'success' : 'danger'" 
                size="small"
                effect="plain"
              >
                {{ chunk.status === 'enabled' ? '已启用' : '已禁用' }}
              </el-tag>
              
              <el-dropdown 
                trigger="click"
                @command="(command) => handleChunkAction(command, chunk)"
              >
                <el-button 
                  text 
                  size="small"
                  :disabled="processing"
                >
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="edit">
                      <el-icon><Edit /></el-icon>
                      编辑
                    </el-dropdown-item>
                    <el-dropdown-item 
                      :command="chunk.status === 'enabled' ? 'disable' : 'enable'"
                    >
                      <el-icon>
                        <component :is="chunk.status === 'enabled' ? 'Close' : 'Check'" />
                      </el-icon>
                      {{ chunk.status === 'enabled' ? '禁用' : '启用' }}
                    </el-dropdown-item>
                    <el-dropdown-item command="delete" divided>
                      <el-icon><Delete /></el-icon>
                      <span style="color: #f56c6c">删除</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <!-- 分块内容 -->
          <div class="chunk-content">
            <p class="content-text">{{ chunk.content }}</p>
          </div>

          <!-- 分块元数据 -->
          <div class="chunk-metadata" v-if="chunk.metadata">
            <el-collapse>
              <el-collapse-item title="元数据" name="metadata">
                <pre class="metadata-content">{{ formatMetadata(chunk.metadata) }}</pre>
              </el-collapse-item>
            </el-collapse>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="totalCount > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalCount"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 编辑分块对话框 -->
    <el-dialog
      v-model="showEditDialog"
      :title="`编辑分块 ${editingChunk ? editingChunk.chunkIndex : ''}`"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form 
        v-if="editingChunk"
        :model="editForm" 
        label-width="100px"
        class="edit-form"
      >
        <el-form-item label="分块内容">
          <el-input
            v-model="editForm.content"
            type="textarea"
            :rows="10"
            placeholder="请输入分块内容"
            show-word-limit
            maxlength="2000"
          />
        </el-form-item>
        
        <el-form-item label="元数据">
          <el-input
            v-model="editForm.metadata"
            type="textarea"
            :rows="5"
            placeholder="请输入JSON格式的元数据（可选）"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleSaveEdit"
            :loading="saving"
          >
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  MoreFilled,
  Edit,
  Delete,
  Check,
  Close
} from '@element-plus/icons-vue'
import { 
  getDocumentChunks, 
  updateChunkStatus, 
  updateChunk, 
  deleteChunk,
  type ChunkStatusUpdateRequest,
  type ChunkUpdateRequest
} from '@/api/smartcs/knowledgeBase'

// 分块数据接口
interface ChunkData {
  id: number
  contentId: number
  chunkIndex: number
  content: string
  tokenSize?: number
  metadata?: any
  status: 'enabled' | 'disabled'
  createTime?: number
  updateTime?: number
}

interface Props {
  contentId: number
}

interface Emits {
  (e: 'chunk-updated'): void
  (e: 'chunk-count-changed', count: number): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 状态管理
const loading = ref(false)
const processing = ref(false)
const saving = ref(false)
const chunks = ref<ChunkData[]>([])
const selectedChunks = ref<number[]>([])
const totalCount = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

// 编辑相关
const showEditDialog = ref(false)
const editingChunk = ref<ChunkData | null>(null)
const editForm = reactive({
  content: '',
  metadata: ''
})

// 全选状态
const allSelected = computed({
  get: () => {
    return chunks.value.length > 0 && selectedChunks.value.length === chunks.value.length
  },
  set: (value: boolean) => {
    if (value) {
      selectedChunks.value = chunks.value.map(chunk => chunk.id)
    } else {
      selectedChunks.value = []
    }
  }
})

const indeterminate = computed(() => {
  return selectedChunks.value.length > 0 && selectedChunks.value.length < chunks.value.length
})

// 加载分块数据
const loadChunks = async () => {
  try {
    loading.value = true
    const response = await getDocumentChunks(props.contentId, {
      pageIndex: currentPage.value,
      pageSize: pageSize.value
    })
    
    if (response.success && response.data) {
      chunks.value = response.data.map((chunk: any) => ({
        id: chunk.id,
        contentId: chunk.contentId,
        chunkIndex: chunk.chunkIndex || 0,
        content: chunk.content || '',
        tokenSize: chunk.tokenSize,
        metadata: chunk.metadata,
        status: chunk.status || 'enabled',
        createTime: chunk.createTime,
        updateTime: chunk.updateTime
      }))
      
      totalCount.value = response.totalCount || chunks.value.length
      emit('chunk-count-changed', totalCount.value)
    } else {
      throw new Error(response.errMessage || '获取分块数据失败')
    }
  } catch (error: any) {
    console.error('加载分块数据失败:', error)
    ElMessage.error(error.message || '加载分块数据失败')
    chunks.value = []
    totalCount.value = 0
  } finally {
    loading.value = false
  }
}

// 处理全选
const handleSelectAll = (value: boolean) => {
  allSelected.value = value
}

// 处理单个分块选择
const handleChunkSelect = () => {
  // 选择状态由 v-model 自动处理
}

// 处理分块操作
const handleChunkAction = async (command: string, chunk: ChunkData) => {
  switch (command) {
    case 'edit':
      handleEditChunk(chunk)
      break
    case 'enable':
    case 'disable':
      await handleToggleChunkStatus(chunk, command as 'enable' | 'disable')
      break
    case 'delete':
      await handleDeleteChunk(chunk)
      break
  }
}

// 编辑分块
const handleEditChunk = (chunk: ChunkData) => {
  editingChunk.value = chunk
  editForm.content = chunk.content
  editForm.metadata = chunk.metadata ? JSON.stringify(chunk.metadata, null, 2) : ''
  showEditDialog.value = true
}

// 保存编辑
const handleSaveEdit = async () => {
  if (!editingChunk.value) return
  
  try {
    saving.value = true
    
    // 验证元数据JSON格式
    let metadata
    if (editForm.metadata.trim()) {
      try {
        metadata = JSON.parse(editForm.metadata)
      } catch {
        throw new Error('元数据格式错误，请输入有效的JSON格式')
      }
    }
    
    const request: ChunkUpdateRequest = {
      chunkId: editingChunk.value.id,
      content: editForm.content,
      metadata
    }
    
    const response = await updateChunk(request)
    
    if (response.success) {
      showEditDialog.value = false
      ElMessage.success('分块更新成功')
      await loadChunks()
      emit('chunk-updated')
    } else {
      throw new Error(response.errMessage || '更新分块失败')
    }
  } catch (error: any) {
    console.error('保存分块失败:', error)
    ElMessage.error(error.message || '保存分块失败')
  } finally {
    saving.value = false
  }
}

// 切换分块状态
const handleToggleChunkStatus = async (chunk: ChunkData, action: 'enable' | 'disable') => {
  const newStatus = action === 'enable' ? 'enabled' : 'disabled'
  const actionText = action === 'enable' ? '启用' : '禁用'
  
  try {
    processing.value = true
    
    const request: ChunkStatusUpdateRequest = {
      chunkId: chunk.id,
      status: newStatus
    }
    
    const response = await updateChunkStatus(request)
    
    if (response.success) {
      ElMessage.success(`分块${actionText}成功`)
      await loadChunks()
      emit('chunk-updated')
    } else {
      throw new Error(response.errMessage || `${actionText}分块失败`)
    }
  } catch (error: any) {
    console.error(`${actionText}分块失败:`, error)
    ElMessage.error(error.message || `${actionText}分块失败`)
  } finally {
    processing.value = false
  }
}

// 删除分块
const handleDeleteChunk = async (chunk: ChunkData) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分块 ${chunk.chunkIndex} 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    processing.value = true
    const response = await deleteChunk(chunk.id)
    
    if (response.success) {
      ElMessage.success('分块删除成功')
      await loadChunks()
      emit('chunk-updated')
    } else {
      throw new Error(response.errMessage || '删除分块失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除分块失败:', error)
      ElMessage.error(error.message || '删除分块失败')
    }
  } finally {
    processing.value = false
  }
}

// 批量启用
const handleBatchEnable = async () => {
  await batchUpdateStatus('enabled', '启用')
}

// 批量禁用
const handleBatchDisable = async () => {
  await batchUpdateStatus('disabled', '禁用')
}

// 批量更新状态
const batchUpdateStatus = async (status: 'enabled' | 'disabled', actionText: string) => {
  if (selectedChunks.value.length === 0) {
    ElMessage.warning('请选择要操作的分块')
    return
  }
  
  try {
    processing.value = true
    
    const promises = selectedChunks.value.map(chunkId => 
      updateChunkStatus({ chunkId, status })
    )
    
    await Promise.all(promises)
    
    ElMessage.success(`批量${actionText}成功`)
    selectedChunks.value = []
    await loadChunks()
    emit('chunk-updated')
  } catch (error: any) {
    console.error(`批量${actionText}失败:`, error)
    ElMessage.error(`批量${actionText}失败`)
  } finally {
    processing.value = false
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedChunks.value.length === 0) {
    ElMessage.warning('请选择要删除的分块')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedChunks.value.length} 个分块吗？此操作不可恢复。`,
      '批量删除确认',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    processing.value = true
    
    const promises = selectedChunks.value.map(chunkId => 
      deleteChunk(chunkId)
    )
    
    await Promise.all(promises)
    
    ElMessage.success('批量删除成功')
    selectedChunks.value = []
    await loadChunks()
    emit('chunk-updated')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  } finally {
    processing.value = false
  }
}

// 分页处理
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  selectedChunks.value = []
  loadChunks()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  selectedChunks.value = []
  loadChunks()
}

// 格式化元数据
const formatMetadata = (metadata: any) => {
  if (typeof metadata === 'string') {
    try {
      return JSON.stringify(JSON.parse(metadata), null, 2)
    } catch {
      return metadata
    }
  }
  return JSON.stringify(metadata, null, 2)
}

// 监听contentId变化
watch(() => props.contentId, () => {
  if (props.contentId) {
    currentPage.value = 1
    selectedChunks.value = []
    loadChunks()
  }
}, { immediate: true })

onMounted(() => {
  loadChunks()
})

// 暴露刷新方法给父组件
defineExpose({
  refresh: loadChunks
})
</script>

<style scoped lang="scss">
.chunk-management {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px;
  background: #fafbfc;
  border-radius: 12px;

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    margin-bottom: 24px;
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    border: 1px solid #f0f2f5;

    .toolbar-left {
      display: flex;
      align-items: center;
      gap: 20px;

      .el-checkbox {
        transform: scale(1.1);
      }

      .selection-info {
        font-size: 14px;
        color: #6b7280;
        font-weight: 500;
        background: #f3f4f6;
        padding: 6px 12px;
        border-radius: 16px;
      }
    }

    .toolbar-right {
      display: flex;
      gap: 12px;
      
      .el-button {
        border-radius: 8px;
        font-weight: 600;
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
        transition: all 0.3s ease;
        
        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }
        
        &.el-button--primary {
          box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
          
          &:hover {
            box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
          }
        }
        
        &.el-button--success {
          box-shadow: 0 2px 6px rgba(103, 194, 58, 0.3);
          
          &:hover {
            box-shadow: 0 4px 12px rgba(103, 194, 58, 0.4);
          }
        }
        
        &.el-button--danger {
          box-shadow: 0 2px 6px rgba(245, 108, 108, 0.3);
          
          &:hover {
            box-shadow: 0 4px 12px rgba(245, 108, 108, 0.4);
          }
        }
      }
    }
  }

  .chunks-container {
    flex: 1;
    display: flex;
    flex-direction: column;

    .empty-state {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 300px;
      background: white;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
      border: 1px solid #f0f2f5;
    }

    .chunks-list {
      flex: 1;

      .chunk-item {
        margin-bottom: 20px;
        padding: 24px;
        border: 1px solid #e2e8f0;
        border-radius: 12px;
        background: white;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        position: relative;
        overflow: hidden;

        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          height: 3px;
          background: linear-gradient(90deg, #409eff 0%, #36cfc9 100%);
          opacity: 0;
          transition: opacity 0.3s ease;
        }

        &:hover {
          border-color: #409eff;
          box-shadow: 0 6px 20px rgba(64, 158, 255, 0.15);
          transform: translateY(-2px);
          
          &::before {
            opacity: 0.3;
          }
        }

        &.selected {
          border-color: #409eff;
          background: linear-gradient(135deg, rgba(64, 158, 255, 0.05) 0%, rgba(54, 207, 201, 0.05) 100%);
          box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
          
          &::before {
            opacity: 1;
          }
        }

        &.disabled {
          background: linear-gradient(135deg, #f5f7fa 0%, #f1f5f9 100%);
          opacity: 0.7;
          
          &::before {
            background: #9ca3af;
          }
        }

        &:last-child {
          margin-bottom: 0;
        }

        .chunk-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 16px;
          flex-wrap: wrap;
          gap: 12px;

          .chunk-info {
            display: flex;
            align-items: center;
            gap: 16px;
            flex: 1;

            .el-checkbox {
              transform: scale(1.1);
            }

            .chunk-index {
              display: flex;
              flex-direction: column;
              gap: 4px;

              .index-label {
                font-size: 15px;
                font-weight: 600;
                color: #1f2937;
                background: #f3f4f6;
                padding: 4px 12px;
                border-radius: 16px;
                width: fit-content;
              }

              .chunk-size {
                font-size: 12px;
                color: #6b7280;
                font-weight: 500;
              }
            }
          }

          .chunk-actions {
            display: flex;
            align-items: center;
            gap: 8px;
            
            .el-button {
              border-radius: 6px;
              font-weight: 500;
              transition: all 0.2s ease;
              
              &:hover {
                transform: scale(1.05);
              }
            }
          }
        }

        .chunk-content {
          margin-bottom: 16px;

          .content-text {
            margin: 0;
            font-size: 14px;
            line-height: 1.7;
            color: #4b5563;
            word-break: break-word;
            background: #f9fafb;
            padding: 16px;
            border-radius: 8px;
            border-left: 4px solid #e5e7eb;
            transition: all 0.2s ease;
            
            &:hover {
              border-left-color: #409eff;
              background: #f0f9ff;
            }
          }
        }

        .chunk-metadata {
          :deep(.el-collapse) {
            border: none;
            border-radius: 8px;
            overflow: hidden;

            .el-collapse-item__header {
              height: 40px;
              line-height: 40px;
              background: #f8fafc;
              border: none;
              font-size: 13px;
              color: #6b7280;
              padding: 0 16px;
              font-weight: 600;
              transition: all 0.2s ease;
              
              &:hover {
                background: #f1f5f9;
                color: #409eff;
              }
            }

            .el-collapse-item__content {
              padding: 0;
              background: #f8fafc;
            }
          }

          .metadata-content {
            margin: 0;
            padding: 16px;
            background: #f5f7fa;
            font-size: 12px;
            color: #4b5563;
            white-space: pre-wrap;
            word-break: break-word;
            line-height: 1.5;
            font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
          }
        }
      }
    }

    .pagination {
      display: flex;
      justify-content: center;
      margin-top: 32px;
      padding: 20px;
      background: white;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
      border: 1px solid #f0f2f5;
    }
  }

  .edit-form {
    :deep(.el-form-item) {
      margin-bottom: 24px;
      
      .el-form-item__label {
        font-weight: 600;
        color: #374151;
      }
      
      .el-input,
      .el-textarea {
        border-radius: 8px;
        
        .el-input__inner,
        .el-textarea__inner {
          border-radius: 8px;
          transition: all 0.3s ease;
          
          &:focus {
            box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .chunk-management {
    .toolbar {
      flex-direction: column;
      gap: 12px;
      align-items: flex-start;

      .toolbar-right {
        width: 100%;
        justify-content: flex-start;
      }
    }

    .chunks-list {
      .chunk-item {
        padding: 12px;

        .chunk-header {
          flex-direction: column;
          align-items: flex-start;
          gap: 12px;

          .chunk-actions {
            width: 100%;
            justify-content: space-between;
          }
        }
      }
    }
  }
}
</style>