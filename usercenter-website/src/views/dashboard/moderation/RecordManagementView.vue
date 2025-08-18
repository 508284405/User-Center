<template>
  <div class="record-management">
    <div class="header-section">
      <h2>审核记录管理</h2>
      <div class="header-actions">
        <el-button @click="refreshRecords">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="primary" @click="showBatchActions = !showBatchActions">
          <el-icon><Operation /></el-icon>
          批量操作
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form
        :model="queryForm"
        inline
        label-width="80px"
        class="filter-form"
      >
        <el-form-item label="内容类型">
          <el-select v-model="queryForm.contentType" placeholder="全部" clearable style="width: 120px">
            <el-option label="聊天消息" value="MESSAGE" />
            <el-option label="知识库" value="KNOWLEDGE" />
            <el-option label="文档" value="DOCUMENT" />
            <el-option label="FAQ" value="FAQ" />
            <el-option label="RAG查询" value="RAG_QUERY" />
          </el-select>
        </el-form-item>

        <el-form-item label="审核结果">
          <el-select v-model="queryForm.moderationResult" placeholder="全部" clearable style="width: 120px">
            <el-option label="通过" value="APPROVED" />
            <el-option label="拒绝" value="REJECTED" />
            <el-option label="需审核" value="NEEDS_REVIEW" />
            <el-option label="处理中" value="PENDING" />
          </el-select>
        </el-form-item>

        <el-form-item label="风险等级">
          <el-select v-model="queryForm.riskLevel" placeholder="全部" clearable style="width: 120px">
            <el-option label="低风险" value="LOW" />
            <el-option label="中风险" value="MEDIUM" />
            <el-option label="高风险" value="HIGH" />
            <el-option label="极高风险" value="CRITICAL" />
          </el-select>
        </el-form-item>

        <el-form-item label="是否阻断">
          <el-select v-model="queryForm.isBlocked" placeholder="全部" clearable style="width: 100px">
            <el-option label="已阻断" :value="true" />
            <el-option label="未阻断" :value="false" />
          </el-select>
        </el-form-item>

        <el-form-item label="用户ID">
          <el-input v-model="queryForm.userId" placeholder="用户ID" style="width: 140px" clearable />
        </el-form-item>

        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="x"
            style="width: 360px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 批量操作栏 -->
    <el-card v-show="showBatchActions" class="batch-actions-card">
      <div class="batch-actions">
        <span>已选择 {{ selectedRecords.length }} 条记录</span>
        <div class="actions">
          <el-button type="success" size="small" @click="handleBatchAction('APPROVE')" :disabled="selectedRecords.length === 0">
            批量通过
          </el-button>
          <el-button type="danger" size="small" @click="handleBatchAction('REJECT')" :disabled="selectedRecords.length === 0">
            批量拒绝
          </el-button>
          <el-button type="warning" size="small" @click="exportRecords" :disabled="selectedRecords.length === 0">
            导出选中
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 记录表格 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="records"
        @selection-change="handleSelectionChange"
        row-key="id"
        class="records-table"
      >
        <el-table-column type="selection" width="50" v-if="showBatchActions" />
        
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column prop="contentType" label="内容类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getContentTypeTag(row.contentType)">
              {{ getContentTypeLabel(row.contentType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="originalContent" label="原始内容" width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="content-preview" @click="showContentDetail(row)">
              {{ truncateText(row.originalContent, 50) }}
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="moderationResult" label="审核结果" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getResultTagType(row.moderationResult)">
              {{ getResultLabel(row.moderationResult) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="riskLevel" label="风险等级" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getRiskTagType(row.riskLevel)" v-if="row.riskLevel">
              {{ getRiskLabel(row.riskLevel) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="confidenceScore" label="置信度" width="80">
          <template #default="{ row }">
            <span v-if="row.confidenceScore">
              {{ (row.confidenceScore * 100).toFixed(1) }}%
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="isBlocked" label="是否阻断" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.isBlocked ? 'danger' : 'success'">
              {{ row.isBlocked ? '已阻断' : '未阻断' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="processingTimeMs" label="处理时间" width="100">
          <template #default="{ row }">
            <span v-if="row.processingTimeMs">
              {{ formatProcessingTime(row.processingTimeMs) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="userId" label="用户" width="120" show-overflow-tooltip />

        <el-table-column prop="createdAt" label="审核时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="text" size="small" @click="viewDetail(row)">
                <el-icon><View /></el-icon>
                详情
              </el-button>
              <el-button
                v-if="row.moderationResult === 'NEEDS_REVIEW'"
                type="text"
                size="small"
                @click="reviewRecord(row)"
              >
                <el-icon><Edit /></el-icon>
                审核
              </el-button>
              <el-button type="text" size="small" @click="showViolations(row)" v-if="row.violationCategories?.length">
                <el-icon><Warning /></el-icon>
                违规详情
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <RecordDetailDialog
      v-model="showDetailDialog"
      :record="selectedRecord"
    />

    <!-- 审核对话框 -->
    <ReviewDialog
      v-model="showReviewDialog"
      :record="selectedRecord"
      @success="handleReviewSuccess"
    />

    <!-- 违规详情对话框 -->
    <ViolationDetailDialog
      v-model="showViolationDialog"
      :violations="selectedViolations"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Refresh, Operation, Search, RefreshRight, View, Edit, Warning 
} from '@element-plus/icons-vue'
import moderationApi, { type ModerationRecord, type RecordQuery } from '@/api/smartcs/moderation'
import RecordDetailDialog from './components/RecordDetailDialog.vue'
import ReviewDialog from './components/ReviewDialog.vue'
import ViolationDetailDialog from './components/ViolationDetailDialog.vue'
import { formatDateTime } from '@/utils/dateFormat'

// 响应式数据
const loading = ref(false)
const showBatchActions = ref(false)
const showDetailDialog = ref(false)
const showReviewDialog = ref(false)
const showViolationDialog = ref(false)
const selectedRecord = ref<ModerationRecord | null>(null)
const selectedViolations = ref([])
const selectedRecords = ref<ModerationRecord[]>([])
const records = ref<ModerationRecord[]>([])
const dateRange = ref<[string, string] | null>(null)

// 查询表单
const queryForm = reactive<RecordQuery>({
  contentType: undefined,
  moderationResult: undefined,
  riskLevel: undefined,
  userId: '',
  isBlocked: undefined,
  startTime: undefined,
  endTime: undefined,
  pageNumber: 1,
  pageSize: 20
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 生命周期
onMounted(() => {
  loadRecords()
})

// 监听器
watch(dateRange, (newRange) => {
  if (newRange && newRange.length === 2) {
    queryForm.startTime = Number(newRange[0])
    queryForm.endTime = Number(newRange[1])
  } else {
    queryForm.startTime = undefined
    queryForm.endTime = undefined
  }
})

// 方法定义
const loadRecords = async () => {
  loading.value = true
  try {
    const query = {
      ...queryForm,
      pageNumber: pagination.page,
      pageSize: pagination.size
    }
    
    const response = await moderationApi.getRecords(query)
    records.value = response.data.data || []
    pagination.total = response.data.total || 0
  } catch (error) {
    console.error('Failed to load records:', error)
    ElMessage.error('加载记录失败')
  } finally {
    loading.value = false
  }
}

const refreshRecords = () => {
  pagination.page = 1
  loadRecords()
}

const handleSearch = () => {
  pagination.page = 1
  loadRecords()
}

const handleReset = () => {
  Object.assign(queryForm, {
    contentType: undefined,
    moderationResult: undefined,
    riskLevel: undefined,
    userId: '',
    isBlocked: undefined,
    startTime: undefined,
    endTime: undefined
  })
  dateRange.value = null
  pagination.page = 1
  loadRecords()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadRecords()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadRecords()
}

const handleSelectionChange = (selection: ModerationRecord[]) => {
  selectedRecords.value = selection
}

const handleBatchAction = async (action: string) => {
  if (selectedRecords.value.length === 0) {
    ElMessage.warning('请先选择记录')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认对选中的 ${selectedRecords.value.length} 条记录执行 ${action === 'APPROVE' ? '批量通过' : '批量拒绝'} 操作吗？`,
      '批量操作确认',
      { type: 'warning' }
    )

    const ids = selectedRecords.value.map(r => r.id!).filter(Boolean)
    await moderationApi.batchProcess(ids, action)
    
    ElMessage.success('批量操作成功')
    loadRecords()
    selectedRecords.value = []
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('Batch action failed:', error)
      ElMessage.error('批量操作失败')
    }
  }
}

const viewDetail = (record: ModerationRecord) => {
  selectedRecord.value = record
  showDetailDialog.value = true
}

const reviewRecord = (record: ModerationRecord) => {
  selectedRecord.value = record
  showReviewDialog.value = true
}

const showViolations = (record: ModerationRecord) => {
  selectedViolations.value = record.violationCategories || []
  showViolationDialog.value = true
}

const showContentDetail = (record: ModerationRecord) => {
  viewDetail(record)
}

const handleReviewSuccess = () => {
  showReviewDialog.value = false
  loadRecords()
}

const exportRecords = async () => {
  if (selectedRecords.value.length === 0) {
    ElMessage.warning('请先选择要导出的记录')
    return
  }

  try {
    // 实现导出逻辑
    const csvData = generateCSV(selectedRecords.value)
    downloadCSV(csvData, `moderation_records_${Date.now()}.csv`)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('Export failed:', error)
    ElMessage.error('导出失败')
  }
}

// 辅助函数
const truncateText = (text: string, maxLength: number) => {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

const formatProcessingTime = (timeMs: number) => {
  if (timeMs < 1000) {
    return `${timeMs}ms`
  } else {
    return `${(timeMs / 1000).toFixed(1)}s`
  }
}

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

const getResultTagType = (result: string) => {
  const tags: Record<string, string> = {
    APPROVED: 'success',
    REJECTED: 'danger',
    NEEDS_REVIEW: 'warning',
    PENDING: 'info'
  }
  return tags[result] || ''
}

const getResultLabel = (result: string) => {
  const labels: Record<string, string> = {
    APPROVED: '通过',
    REJECTED: '拒绝',
    NEEDS_REVIEW: '需审核',
    PENDING: '处理中'
  }
  return labels[result] || result
}

const getRiskTagType = (risk: string) => {
  const tags: Record<string, string> = {
    LOW: 'success',
    MEDIUM: 'warning',
    HIGH: 'danger',
    CRITICAL: 'danger'
  }
  return tags[risk] || ''
}

const getRiskLabel = (risk: string) => {
  const labels: Record<string, string> = {
    LOW: '低',
    MEDIUM: '中',
    HIGH: '高',
    CRITICAL: '极高'
  }
  return labels[risk] || risk
}

const generateCSV = (records: ModerationRecord[]) => {
  const headers = [
    'ID', '内容类型', '原始内容', '审核结果', '风险等级', '置信度', 
    '是否阻断', '处理时间(ms)', '用户ID', '审核时间'
  ]
  
  const rows = records.map(record => [
    record.id,
    getContentTypeLabel(record.contentType),
    `"${record.originalContent?.replace(/"/g, '""')}"`,
    getResultLabel(record.moderationResult),
    record.riskLevel ? getRiskLabel(record.riskLevel) : '',
    record.confidenceScore ? (record.confidenceScore * 100).toFixed(1) + '%' : '',
    record.isBlocked ? '是' : '否',
    record.processingTimeMs || '',
    record.userId || '',
    record.createdAt ? formatDateTime(record.createdAt) : ''
  ])

  return [headers, ...rows].map(row => row.join(',')).join('\n')
}

const downloadCSV = (csvData: string, filename: string) => {
  const blob = new Blob(['\uFEFF' + csvData], { type: 'text/csv;charset=utf-8' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = filename
  link.click()
  URL.revokeObjectURL(link.href)
}
</script>

<style scoped lang="scss">
.record-management {
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
    }
  }

  .filter-card,
  .batch-actions-card,
  .table-card {
    margin-bottom: 20px;
  }

  .filter-form {
    .el-form-item {
      margin-bottom: 12px;
    }
  }

  .batch-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;

    .actions {
      display: flex;
      gap: 12px;
    }
  }

  .records-table {
    .content-preview {
      cursor: pointer;
      color: #409eff;

      &:hover {
        text-decoration: underline;
      }
    }

    .action-buttons {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }
  }

  .pagination-container {
    display: flex;
    justify-content: center;
    padding: 20px 0;
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .filter-form {
    :deep(.el-form-item) {
      margin-right: 8px;
    }
  }

  .records-table {
    font-size: 14px;
  }
}

@media (max-width: 768px) {
  .record-management {
    padding: 12px;
  }

  .filter-form {
    :deep(.el-form-item) {
      width: 100%;
      margin-right: 0;
    }
  }

  .batch-actions {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }
}
</style>