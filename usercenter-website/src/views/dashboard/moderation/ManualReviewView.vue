<template>
  <div class="manual-review">
    <div class="header-section">
      <h2>人工审核处理</h2>
      <div class="header-actions">
        <el-badge :value="pendingCount" class="badge">
          <el-button type="primary" @click="refreshPending">
            <el-icon><Refresh /></el-icon>
            刷新待审核
          </el-button>
        </el-badge>
        <el-button @click="showBatchDialog = true" :disabled="selectedRecords.length === 0">
          <el-icon><Operation /></el-icon>
          批量处理 ({{ selectedRecords.length }})
        </el-button>
        <el-switch
          v-model="autoRefresh"
          active-text="自动刷新"
          inactive-text="手动刷新"
          @change="handleAutoRefreshChange"
        />
      </div>
    </div>

    <!-- 筛选和排序 -->
    <el-card class="filter-card">
      <el-form inline class="filter-form">
        <el-form-item label="风险等级">
          <el-select v-model="riskFilter" placeholder="全部" clearable style="width: 120px" @change="loadPendingRecords">
            <el-option label="极高风险" value="CRITICAL" />
            <el-option label="高风险" value="HIGH" />
            <el-option label="中风险" value="MEDIUM" />
            <el-option label="低风险" value="LOW" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="内容类型">
          <el-select v-model="typeFilter" placeholder="全部" clearable style="width: 120px" @change="loadPendingRecords">
            <el-option label="聊天消息" value="MESSAGE" />
            <el-option label="知识库" value="KNOWLEDGE" />
            <el-option label="文档" value="DOCUMENT" />
            <el-option label="FAQ" value="FAQ" />
          </el-select>
        </el-form-item>

        <el-form-item label="排序方式">
          <el-select v-model="sortBy" style="width: 150px" @change="loadPendingRecords">
            <el-option label="创建时间" value="createdAt" />
            <el-option label="风险等级" value="riskLevel" />
            <el-option label="置信度" value="confidenceScore" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="loadPendingRecords">
            <el-icon><Search /></el-icon>
            应用筛选
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 待审核列表 -->
    <el-card class="review-list-card">
      <template #header>
        <div class="list-header">
          <span>待审核记录 ({{ pendingCount }})</span>
          <div class="header-stats">
            <el-tag type="danger" size="small">极高风险: {{ stats.criticalCount }}</el-tag>
            <el-tag type="warning" size="small">高风险: {{ stats.highCount }}</el-tag>
            <el-tag type="info" size="small">中低风险: {{ stats.otherCount }}</el-tag>
          </div>
        </div>
      </template>

      <div v-loading="loading" class="review-list">
        <div v-if="pendingRecords.length === 0" class="empty-state">
          <el-empty description="暂无待审核记录" />
        </div>

        <div
          v-for="record in pendingRecords"
          :key="record.id"
          class="review-item"
          :class="{ 
            'selected': selectedRecords.includes(record.id!),
            'urgent': record.riskLevel === 'CRITICAL' || record.riskLevel === 'HIGH'
          }"
        >
          <!-- 选择和基本信息 -->
          <div class="item-header">
            <el-checkbox
              :model-value="selectedRecords.includes(record.id!)"
              @change="handleRecordSelect(record.id!, $event)"
            />
            <div class="item-meta">
              <div class="item-id">ID: {{ record.id }}</div>
              <div class="item-time">{{ formatDateTime(record.createdAt) }}</div>
            </div>
            <div class="item-tags">
              <el-tag :type="getContentTypeTag(record.contentType)" size="small">
                {{ getContentTypeLabel(record.contentType) }}
              </el-tag>
              <el-tag 
                v-if="record.riskLevel" 
                :type="getRiskTagType(record.riskLevel)" 
                size="small"
              >
                {{ getRiskLabel(record.riskLevel) }}
              </el-tag>
              <el-tag 
                v-if="record.confidenceScore" 
                type="info" 
                size="small"
              >
                置信度: {{ (record.confidenceScore * 100).toFixed(1) }}%
              </el-tag>
            </div>
          </div>

          <!-- 内容展示 -->
          <div class="item-content">
            <div class="content-section">
              <h4>原始内容</h4>
              <div class="content-display">
                <pre class="content-text">{{ record.originalContent }}</pre>
              </div>
            </div>

            <!-- 违规信息 -->
            <div v-if="record.violationCategories?.length" class="violation-section">
              <h4>检测到的违规 ({{ record.violationCategories.length }})</h4>
              <div class="violations">
                <div 
                  v-for="(violation, index) in record.violationCategories" 
                  :key="index"
                  class="violation-item"
                >
                  <el-tag type="danger" size="small">{{ violation.categoryName }}</el-tag>
                  <span class="confidence">{{ (violation.confidence * 100).toFixed(1) }}%</span>
                  <span v-if="violation.triggerRule" class="trigger">{{ violation.triggerRule }}</span>
                </div>
              </div>
            </div>

            <!-- 关键词匹配 -->
            <div v-if="record.keywordMatches?.length" class="keyword-section">
              <h4>匹配关键词</h4>
              <div class="keywords">
                <el-tag 
                  v-for="keyword in record.keywordMatches" 
                  :key="keyword"
                  type="warning" 
                  size="small"
                  class="keyword-tag"
                >
                  {{ keyword }}
                </el-tag>
              </div>
            </div>
          </div>

          <!-- 审核操作 -->
          <div class="item-actions">
            <div class="quick-actions">
              <el-button 
                type="success" 
                size="small" 
                @click="quickReview(record, 'APPROVED')"
                :loading="record.processing"
              >
                <el-icon><Check /></el-icon>
                通过
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="quickReview(record, 'REJECTED')"
                :loading="record.processing"
              >
                <el-icon><Close /></el-icon>
                拒绝
              </el-button>
              <el-button 
                type="primary" 
                size="small" 
                @click="detailedReview(record)"
              >
                <el-icon><Edit /></el-icon>
                详细审核
              </el-button>
            </div>
            
            <div class="additional-actions">
              <el-button type="text" size="small" @click="viewFullDetail(record)">
                <el-icon><View /></el-icon>
                查看详情
              </el-button>
              <el-button type="text" size="small" @click="viewSimilarCases(record)">
                <el-icon><Connection /></el-icon>
                相似案例
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 加载更多 -->
      <div v-if="hasMore" class="load-more">
        <el-button @click="loadMore" :loading="loadingMore">
          加载更多记录
        </el-button>
      </div>
    </el-card>

    <!-- 审核统计 -->
    <el-row :gutter="20" class="stats-section">
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-value">{{ reviewStats.todayReviewed }}</div>
            <div class="stat-label">今日已审核</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-value">{{ reviewStats.avgProcessingTime }}ms</div>
            <div class="stat-label">平均处理时间</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-value">{{ reviewStats.approvalRate }}%</div>
            <div class="stat-label">通过率</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-value">{{ reviewStats.efficiency }}</div>
            <div class="stat-label">审核效率</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细审核对话框 -->
    <DetailedReviewDialog
      v-model="showDetailDialog"
      :record="currentRecord"
      @success="handleReviewSuccess"
    />

    <!-- 批量处理对话框 -->
    <BatchProcessDialog
      v-model="showBatchDialog"
      :records="selectedRecords"
      @success="handleBatchSuccess"
    />

    <!-- 记录详情对话框 -->
    <RecordDetailDialog
      v-model="showRecordDialog"
      :record="currentRecord"
    />

    <!-- 相似案例对话框 -->
    <SimilarCasesDialog
      v-model="showSimilarDialog"
      :record="currentRecord"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Refresh, Operation, Search, Check, Close, Edit, View, Connection 
} from '@element-plus/icons-vue'
import moderationApi, { type ModerationRecord } from '@/api/smartcs/moderation'
import DetailedReviewDialog from './components/DetailedReviewDialog.vue'
import BatchProcessDialog from './components/BatchProcessDialog.vue'
import RecordDetailDialog from './components/RecordDetailDialog.vue'
import SimilarCasesDialog from './components/SimilarCasesDialog.vue'
import { formatDateTime } from '@/utils/dateFormat'

// 扩展记录接口
interface ReviewRecord extends ModerationRecord {
  processing?: boolean
}

// 响应式数据
const loading = ref(false)
const loadingMore = ref(false)
const autoRefresh = ref(false)
const showDetailDialog = ref(false)
const showBatchDialog = ref(false)
const showRecordDialog = ref(false)
const showSimilarDialog = ref(false)
const pendingRecords = ref<ReviewRecord[]>([])
const selectedRecords = ref<number[]>([])
const currentRecord = ref<ReviewRecord | null>(null)
const pendingCount = ref(0)
const hasMore = ref(true)
const page = ref(1)

// 筛选条件
const riskFilter = ref<string>('')
const typeFilter = ref<string>('')
const sortBy = ref('createdAt')

// 统计数据
const stats = reactive({
  criticalCount: 0,
  highCount: 0,
  otherCount: 0
})

const reviewStats = reactive({
  todayReviewed: 0,
  avgProcessingTime: 0,
  approvalRate: 0,
  efficiency: 'A+'
})

// 自动刷新定时器
let refreshTimer: NodeJS.Timeout | null = null

// 生命周期
onMounted(() => {
  loadPendingRecords()
  loadReviewStats()
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})

// 方法定义
const loadPendingRecords = async (loadMore = false) => {
  if (!loadMore) {
    loading.value = true
    page.value = 1
    pendingRecords.value = []
  } else {
    loadingMore.value = true
  }

  try {
    const query = {
      riskLevel: riskFilter.value || undefined,
      contentType: typeFilter.value || undefined,
      sortBy: sortBy.value,
      sortOrder: 'DESC' as const,
      pageNumber: page.value,
      pageSize: 20
    }

    const response = await moderationApi.getPendingReviews(20)
    const newRecords = (response.data || []).map(record => ({
      ...record,
      processing: false
    }))

    if (loadMore) {
      pendingRecords.value.push(...newRecords)
    } else {
      pendingRecords.value = newRecords
    }

    pendingCount.value = newRecords.length
    hasMore.value = newRecords.length === 20
    
    if (hasMore.value) {
      page.value++
    }

    calculateStats()
  } catch (error) {
    console.error('Failed to load pending records:', error)
    ElMessage.error('加载待审核记录失败')
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const loadReviewStats = async () => {
  try {
    // 模拟审核统计数据
    Object.assign(reviewStats, {
      todayReviewed: 156,
      avgProcessingTime: 245,
      approvalRate: 78.5,
      efficiency: 'A+'
    })
  } catch (error) {
    console.error('Failed to load review stats:', error)
  }
}

const calculateStats = () => {
  stats.criticalCount = pendingRecords.value.filter(r => r.riskLevel === 'CRITICAL').length
  stats.highCount = pendingRecords.value.filter(r => r.riskLevel === 'HIGH').length
  stats.otherCount = pendingRecords.value.length - stats.criticalCount - stats.highCount
}

const refreshPending = () => {
  loadPendingRecords()
}

const loadMore = () => {
  loadPendingRecords(true)
}

const handleAutoRefreshChange = (enabled: boolean) => {
  if (enabled) {
    refreshTimer = setInterval(() => {
      loadPendingRecords()
    }, 30000) // 30秒刷新一次
  } else {
    if (refreshTimer) {
      clearInterval(refreshTimer)
      refreshTimer = null
    }
  }
}

const handleRecordSelect = (recordId: number, selected: boolean) => {
  if (selected) {
    if (!selectedRecords.value.includes(recordId)) {
      selectedRecords.value.push(recordId)
    }
  } else {
    const index = selectedRecords.value.indexOf(recordId)
    if (index > -1) {
      selectedRecords.value.splice(index, 1)
    }
  }
}

const quickReview = async (record: ReviewRecord, result: 'APPROVED' | 'REJECTED') => {
  if (!record.id) return

  try {
    await ElMessageBox.confirm(
      `确认${result === 'APPROVED' ? '通过' : '拒绝'}此记录吗？`,
      '快速审核确认',
      { type: 'warning' }
    )

    record.processing = true

    const action = result === 'APPROVED' ? 'WARN' : 'BLOCK'
    await moderationApi.manualReview(record.id, {
      result,
      action,
      notes: `快速审核 - ${result === 'APPROVED' ? '通过' : '拒绝'}`
    })

    // 从列表中移除已审核的记录
    const index = pendingRecords.value.findIndex(r => r.id === record.id)
    if (index > -1) {
      pendingRecords.value.splice(index, 1)
      pendingCount.value--
    }

    ElMessage.success('审核完成')
    updateReviewStats()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('Quick review failed:', error)
      ElMessage.error('审核失败')
    }
  } finally {
    record.processing = false
  }
}

const detailedReview = (record: ReviewRecord) => {
  currentRecord.value = record
  showDetailDialog.value = true
}

const viewFullDetail = (record: ReviewRecord) => {
  currentRecord.value = record
  showRecordDialog.value = true
}

const viewSimilarCases = (record: ReviewRecord) => {
  currentRecord.value = record
  showSimilarDialog.value = true
}

const handleReviewSuccess = () => {
  showDetailDialog.value = false
  loadPendingRecords()
  updateReviewStats()
}

const handleBatchSuccess = () => {
  showBatchDialog.value = false
  selectedRecords.value = []
  loadPendingRecords()
  updateReviewStats()
}

const updateReviewStats = () => {
  reviewStats.todayReviewed++
  // 可以根据实际情况更新其他统计数据
}

// 辅助函数
const getContentTypeTag = (type: string) => {
  const tags: Record<string, string> = {
    MESSAGE: 'primary',
    KNOWLEDGE: 'success',
    DOCUMENT: 'info',
    FAQ: 'warning',
    RAG_QUERY: 'danger'
  }
  return tags[type] || ''
}

const getContentTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    MESSAGE: '消息',
    KNOWLEDGE: '知识库',
    DOCUMENT: '文档',
    FAQ: 'FAQ',
    RAG_QUERY: 'RAG查询'
  }
  return labels[type] || type
}

const getRiskTagType = (risk: string) => {
  const tags: Record<string, string> = {
    LOW: 'success',
    MEDIUM: 'info',
    HIGH: 'warning',
    CRITICAL: 'danger'
  }
  return tags[risk] || ''
}

const getRiskLabel = (risk: string) => {
  const labels: Record<string, string> = {
    LOW: '低风险',
    MEDIUM: '中风险',
    HIGH: '高风险',
    CRITICAL: '极高风险'
  }
  return labels[risk] || risk
}
</script>

<style scoped lang="scss">
.manual-review {
  padding: 20px;

  .header-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
      color: #303133;
    }

    .header-actions {
      display: flex;
      gap: 12px;
      align-items: center;

      .badge {
        :deep(.el-badge__content) {
          top: 8px;
          right: 15px;
        }
      }
    }
  }

  .filter-card,
  .review-list-card {
    margin-bottom: 20px;
  }

  .filter-form {
    .el-form-item {
      margin-bottom: 0;
    }
  }

  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-stats {
      display: flex;
      gap: 8px;
    }
  }

  .review-list {
    .empty-state {
      padding: 40px 0;
      text-align: center;
    }

    .review-item {
      border: 1px solid #ebeef5;
      border-radius: 8px;
      margin-bottom: 16px;
      transition: all 0.3s;

      &:last-child {
        margin-bottom: 0;
      }

      &:hover {
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
      }

      &.selected {
        border-color: #409eff;
        background-color: #f0f9ff;
      }

      &.urgent {
        border-color: #f56565;
        background-color: #fef2f2;
      }

      .item-header {
        display: flex;
        align-items: center;
        padding: 16px 20px;
        border-bottom: 1px solid #ebeef5;
        background-color: #fafafa;

        .item-meta {
          margin-left: 12px;
          margin-right: auto;

          .item-id {
            font-size: 12px;
            color: #909399;
            font-family: 'Courier New', monospace;
          }

          .item-time {
            font-size: 12px;
            color: #606266;
            margin-top: 2px;
          }
        }

        .item-tags {
          display: flex;
          gap: 6px;
        }
      }

      .item-content {
        padding: 20px;

        .content-section,
        .violation-section,
        .keyword-section {
          margin-bottom: 16px;

          &:last-child {
            margin-bottom: 0;
          }

          h4 {
            margin: 0 0 8px 0;
            color: #303133;
            font-size: 14px;
          }

          .content-display {
            .content-text {
              margin: 0;
              padding: 12px;
              background-color: #f5f7fa;
              border-radius: 4px;
              font-family: 'Courier New', monospace;
              font-size: 12px;
              line-height: 1.5;
              white-space: pre-wrap;
              word-wrap: break-word;
              max-height: 150px;
              overflow-y: auto;
            }
          }

          .violations {
            .violation-item {
              display: flex;
              align-items: center;
              gap: 8px;
              padding: 6px 12px;
              border: 1px solid #dcdfe6;
              border-radius: 4px;
              margin-bottom: 6px;

              &:last-child {
                margin-bottom: 0;
              }

              .confidence {
                font-size: 12px;
                color: #606266;
              }

              .trigger {
                font-size: 12px;
                color: #909399;
                font-family: 'Courier New', monospace;
              }
            }
          }

          .keywords {
            .keyword-tag {
              margin: 4px 8px 4px 0;
            }
          }
        }
      }

      .item-actions {
        padding: 16px 20px;
        border-top: 1px solid #ebeef5;
        background-color: #fafafa;
        display: flex;
        justify-content: space-between;
        align-items: center;

        .quick-actions {
          display: flex;
          gap: 8px;
        }

        .additional-actions {
          display: flex;
          gap: 8px;
        }
      }
    }
  }

  .load-more {
    text-align: center;
    padding: 20px 0;
  }

  .stats-section {
    .stat-item {
      text-align: center;
      padding: 20px;

      .stat-value {
        font-size: 24px;
        font-weight: bold;
        color: #409eff;
        margin-bottom: 8px;
      }

      .stat-label {
        font-size: 14px;
        color: #909399;
      }
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .item-header {
    flex-wrap: wrap;
    gap: 8px;

    .item-tags {
      width: 100%;
      margin-top: 8px;
    }
  }

  .item-actions {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
}

@media (max-width: 768px) {
  .manual-review {
    padding: 12px;
  }

  .header-section {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .filter-form {
    :deep(.el-form-item) {
      width: 100%;
      margin-bottom: 12px;
    }
  }

  .list-header {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
  }
}
</style>