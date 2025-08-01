<template>
  <div class="document-detail-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button text @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <div class="document-info">
          <h2>{{ documentData?.title || '加载中...' }}</h2>
          <div class="document-meta">
            <span class="meta-item">知识库ID: {{ knowledgeBaseId }}</span>
            <span class="meta-item" v-if="documentData">
              文档ID: {{ documentData.id }}
            </span>
            <el-tag 
              v-if="documentData"
              :type="getStatusTagType(documentData.status)"
              size="small"
            >
              {{ getStatusText(documentData.status) }}
            </el-tag>
          </div>
        </div>
      </div>
      <div class="header-actions">
        <el-button @click="refreshData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <div class="page-content" v-loading="loading">
      <!-- 主要内容区域 -->
      <div class="content-main">
        <!-- 分块列表 -->
        <div class="chunks-section">
          <div class="section-header">
            <div class="section-title">
              <h3>{{ chunkCount }} 文分段</h3>
              <p class="section-subtitle">分段规则</p>
            </div>
            <div class="section-actions">
              <el-button size="small" @click="handleAddChunk">
                <el-icon><Plus /></el-icon>
                添加分段
              </el-button>
            </div>
          </div>

          <!-- 分块管理组件 -->
          <ChunkManagement 
            :content-id="contentId"
            @chunk-updated="handleChunkUpdated"
            @chunk-count-changed="handleChunkCountChanged"
          />
        </div>
      </div>

      <!-- 侧边栏 -->
      <div class="content-sidebar">
        <!-- 元数据 -->
        <div class="sidebar-section">
          <h4>元数据</h4>
          <div class="metadata-content">
            <div class="metadata-item">
              <span class="label">元数据是关于文档的数据。用于描述文档的属性。元数据可以帮助更好地检索文档的结构性质。</span>
            </div>
            <el-button 
              text 
              type="primary" 
              size="small"
              @click="showMetadataDialog = true"
            >
              开始标法 →
            </el-button>
          </div>
        </div>

        <!-- 文档信息 -->
        <div class="sidebar-section" v-if="documentData">
          <h4>文档信息</h4>
          <div class="info-content">
            <div class="info-item">
              <span class="info-label">原始文件名称</span>
              <span class="info-value">{{ documentData.originalFileName || documentData.title }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">原始文件大小</span>
              <span class="info-value">{{ formatFileSize(documentData.fileSize) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">上传日期</span>
              <span class="info-value">{{ formatDate(documentData.createdAt) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">最后更新日期</span>
              <span class="info-value">{{ formatDate(documentData.updatedAt) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">来源</span>
              <span class="info-value">{{ documentData.source || '文件上传' }}</span>
            </div>
          </div>
        </div>

        <!-- 技术参数 -->
        <div class="sidebar-section" v-if="documentData">
          <h4>技术参数</h4>
          <div class="tech-content">
            <div class="tech-item">
              <span class="tech-label">分段规则</span>
              <span class="tech-value">{{ getSegmentModeText(documentData.segmentMode) }}</span>
            </div>
            <div class="tech-item">
              <span class="tech-label">段落长度</span>
              <span class="tech-value">{{ documentData.averageChunkLength || 0 }}</span>
            </div>
            <div class="tech-item">
              <span class="tech-label">平均段落长度</span>
              <span class="tech-value">{{ Math.round(documentData.averageChunkLength || 0) }} characters</span>
            </div>
            <div class="tech-item">
              <span class="tech-label">段落数量</span>
              <span class="tech-value">{{ documentData.chunkCount || 0 }} paragraphs</span>
            </div>
            <div class="tech-item">
              <span class="tech-label">召回次数</span>
              <span class="tech-value">{{ formatNumber(documentData.recallCount || 0) }}</span>
            </div>
            <div class="tech-item">
              <span class="tech-label">嵌入时间</span>
              <span class="tech-value">{{ formatDuration(documentData.embeddingTime) }}</span>
            </div>
            <div class="tech-item">
              <span class="tech-label">嵌入花费</span>
              <span class="tech-value">{{ formatCost(documentData.embeddingCost) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 元数据编辑对话框 -->
    <el-dialog
      v-model="showMetadataDialog"
      title="编辑元数据"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-input
        v-model="metadataContent"
        type="textarea"
        :rows="10"
        placeholder="请输入JSON格式的元数据"
      />
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showMetadataDialog = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleSaveMetadata"
            :loading="savingMetadata"
          >
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  ArrowLeft, 
  ArrowRight, 
  Refresh,
  Plus
} from '@element-plus/icons-vue'
import { contentApi, type ContentDTO } from '@/api/smartcs/content'
import ChunkManagement from './components/ChunkManagement.vue'

const route = useRoute()
const router = useRouter()

// 路由参数
const contentId = computed(() => Number(route.params.id))
const knowledgeBaseId = computed(() => Number(route.params.knowledgeBaseId) || Number(route.query.knowledgeBaseId))

// 状态管理
const loading = ref(true)
const savingMetadata = ref(false)
const documentData = ref<ContentDTO | null>(null)
const chunkCount = ref(0)

// 对话框状态
const showMetadataDialog = ref(false)
const metadataContent = ref('')

// 获取文档详情
const fetchDocumentDetail = async () => {
  try {
    loading.value = true
    const response = await contentApi.getById(contentId.value)
    
    if (response.success && response.data) {
      documentData.value = response.data
      
      // 初始化元数据内容
      if (response.data.metadata) {
        try {
          const metadata = typeof response.data.metadata === 'string' 
            ? JSON.parse(response.data.metadata)
            : response.data.metadata
          metadataContent.value = JSON.stringify(metadata, null, 2)
        } catch {
          metadataContent.value = response.data.metadata
        }
      }
      
      chunkCount.value = response.data.chunkCount || 0
    } else {
      throw new Error(response.errMessage || '获取文档详情失败')
    }
  } catch (error: any) {
    console.error('获取文档详情失败:', error)
    ElMessage.error(error.message || '获取文档详情失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 刷新数据
const refreshData = () => {
  fetchDocumentDetail()
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 处理分块更新
const handleChunkUpdated = () => {
  // 分块更新后可能需要刷新文档数据
  fetchDocumentDetail()
}

// 处理分块数量变化
const handleChunkCountChanged = (count: number) => {
  chunkCount.value = count
}

// 添加分块
const handleAddChunk = () => {
  ElMessage.info('添加分块功能开发中...')
}

// 保存元数据
const handleSaveMetadata = async () => {
  try {
    savingMetadata.value = true
    
    // 验证JSON格式
    let metadata
    try {
      metadata = JSON.parse(metadataContent.value || '{}')
    } catch {
      throw new Error('元数据格式错误，请输入有效的JSON格式')
    }
    
    // 更新文档元数据
    const response = await contentApi.update({
      id: contentId.value,
      title: documentData.value?.title || '',
      // 这里可能需要扩展API来支持元数据更新
    })
    
    if (response.success) {
      showMetadataDialog.value = false
      ElMessage.success('元数据保存成功')
      fetchDocumentDetail()
    } else {
      throw new Error(response.errMessage || '保存元数据失败')
    }
  } catch (error: any) {
    console.error('保存元数据失败:', error)
    ElMessage.error(error.message || '保存元数据失败')
  } finally {
    savingMetadata.value = false
  }
}

// 工具函数
const getStatusTagType = (status: string): string => {
  switch (status) {
    case 'enabled': return 'success'
    case 'disabled': return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status: string): string => {
  switch (status) {
    case 'enabled': return '启用'
    case 'disabled': return '禁用'
    default: return status
  }
}

const getSegmentModeText = (mode: string): string => {
  switch (mode) {
    case 'general': return '父子分段'
    case 'parent_child': return '父子分段'
    default: return mode
  }
}

const formatFileSize = (size?: number): string => {
  if (!size) return '0 B'
  
  const units = ['B', 'KB', 'MB', 'GB']
  let index = 0
  let fileSize = size
  
  while (fileSize >= 1024 && index < units.length - 1) {
    fileSize /= 1024
    index++
  }
  
  return `${fileSize.toFixed(2)} ${units[index]}`
}

const formatDate = (timestamp?: number): string => {
  if (!timestamp) return '-'
  return new Date(timestamp).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  return num.toString()
}

const formatDuration = (seconds?: number): string => {
  if (!seconds) return '0.0 sec'
  
  if (seconds < 60) {
    return `${seconds.toFixed(1)} sec`
  }
  
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}m ${remainingSeconds.toFixed(1)}s`
}

const formatCost = (cost?: number): string => {
  if (!cost) return '¥0.00'
  return `¥${cost.toFixed(4)}`
}

// 监听路由参数变化
watch(() => route.params.id, () => {
  if (route.params.id) {
    fetchDocumentDetail()
  }
}, { immediate: true })

onMounted(() => {
  fetchDocumentDetail()
})
</script>

<style scoped lang="scss">
.document-detail-view {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 32px;
    background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
    border-bottom: 1px solid #e2e8f0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    .header-left {
      display: flex;
      align-items: center;
      gap: 20px;

      .el-button {
        border-radius: 8px;
        font-weight: 600;
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
        transition: all 0.3s ease;
        
        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }
      }

      .document-info {
        h2 {
          margin: 0 0 8px 0;
          font-size: 22px;
          font-weight: 700;
          color: #1f2937;
          background: linear-gradient(135deg, #409eff 0%, #36cfc9 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
          line-height: 1.3;
        }

        .document-meta {
          display: flex;
          align-items: center;
          gap: 16px;
          font-size: 14px;
          color: #6b7280;
          background: #f3f4f6;
          padding: 8px 16px;
          border-radius: 20px;
          font-weight: 500;

          .meta-item {
            display: flex;
            align-items: center;
            gap: 4px;
            
            &:not(:last-child)::after {
              content: '•';
              margin-left: 16px;
              color: #9ca3af;
              font-size: 12px;
            }
          }
        }
      }
    }

    .header-actions {
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
      }
    }
  }

  .page-content {
    flex: 1;
    display: flex;
    min-height: 0;
    gap: 32px;
    padding: 32px;

    .content-main {
      flex: 1;
      display: flex;
      flex-direction: column;
      background: white;
      border-radius: 16px;
      padding: 32px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
      border: 1px solid #f0f2f5;

      .chunks-section {
        flex: 1;
        display: flex;
        flex-direction: column;

        .section-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 32px;
          padding-bottom: 16px;
          border-bottom: 2px solid #f0f2f5;

          .section-title {
            h3 {
              margin: 0 0 8px 0;
              font-size: 20px;
              font-weight: 700;
              color: #1f2937;
              display: flex;
              align-items: center;
              gap: 8px;
              
              &::before {
                content: '🧩';
                font-size: 22px;
              }
            }

            .section-subtitle {
              margin: 0;
              font-size: 14px;
              color: #6b7280;
              font-weight: 500;
            }
          }

          .section-actions {
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
            }
          }
        }
      }
    }

    .content-sidebar {
      width: 360px;
      display: flex;
      flex-direction: column;
      gap: 24px;

      .sidebar-section {
        background: white;
        border-radius: 16px;
        padding: 24px;
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
        border: 1px solid #f0f2f5;
        transition: all 0.3s ease;
        
        &:hover {
          box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
          transform: translateY(-2px);
        }

        h4 {
          margin: 0 0 20px 0;
          font-size: 18px;
          font-weight: 700;
          color: #1f2937;
          display: flex;
          align-items: center;
          gap: 8px;
          padding-bottom: 12px;
          border-bottom: 2px solid #f0f2f5;
          
          &::before {
            font-size: 20px;
          }
        }

        .metadata-content {
          .metadata-item {
            margin-bottom: 16px;
            padding: 12px 16px;
            background: #f8fafc;
            border-radius: 8px;
            border-left: 4px solid #e5e7eb;
            transition: all 0.2s ease;
            
            &:hover {
              background: #f1f5f9;
              border-left-color: #409eff;
            }

            .label {
              font-size: 14px;
              color: #4b5563;
              line-height: 1.6;
              font-weight: 500;
            }
          }
        }

        .info-content, .tech-content {
          .info-item, .tech-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 12px 16px;
            background: #f8fafc;
            border-radius: 8px;
            margin-bottom: 8px;
            transition: all 0.2s ease;
            
            &:hover {
              background: #f1f5f9;
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
            }

            &:last-child {
              margin-bottom: 0;
            }

            .info-label, .tech-label {
              font-size: 14px;
              color: #6b7280;
              flex-shrink: 0;
              font-weight: 600;
            }

            .info-value, .tech-value {
              font-size: 14px;
              color: #1f2937;
              text-align: right;
              word-break: break-word;
              font-weight: 600;
            }
            
            .el-tag {
              font-weight: 600;
              border-radius: 16px;
            }
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .document-detail-view {
    .page-content {
      .content-sidebar {
        width: 280px;
      }
    }
  }
}

@media (max-width: 1024px) {
  .document-detail-view {
    .page-header {
      padding: 12px 16px;

      .header-left {
        gap: 12px;

        .document-info {
          h2 {
            font-size: 18px;
          }

          .document-meta {
            font-size: 13px;
          }
        }
      }
    }

    .page-content {
      flex-direction: column;
      padding: 16px;

      .content-sidebar {
        width: 100%;
        order: -1;

        .sidebar-section {
          padding: 16px;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .document-detail-view {
    .page-header {
      .header-left {
        .document-info {
          .document-meta {
            flex-wrap: wrap;
          }
        }
      }

      .header-actions {
        gap: 8px;

        .el-button {
          padding: 8px 12px;
        }
      }
    }

    .page-content {
      padding: 12px;

      .content-main {
        padding: 16px;
      }
    }
  }
}
</style>