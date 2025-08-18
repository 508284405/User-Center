<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    title="相似案例参考"
    width="900px"
    :close-on-click-modal="false"
  >
    <div class="similar-cases" v-if="record">
      <!-- 当前案例信息 -->
      <el-card class="current-case-card">
        <template #header>
          <span>当前案例</span>
        </template>
        
        <div class="current-case">
          <div class="case-meta">
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
            <span class="case-id">ID: {{ record.id }}</span>
          </div>
          <div class="case-content">
            <pre class="content-text">{{ record.originalContent }}</pre>
          </div>
          <div v-if="record.violationCategories?.length" class="case-violations">
            <span class="violations-label">检测违规:</span>
            <el-tag 
              v-for="violation in record.violationCategories" 
              :key="violation.categoryCode"
              type="danger" 
              size="small"
              class="violation-tag"
            >
              {{ violation.categoryName }}
            </el-tag>
          </div>
        </div>
      </el-card>

      <!-- 搜索和筛选 -->
      <el-card class="search-card">
        <template #header>
          <span>查找相似案例</span>
        </template>
        
        <el-form inline class="search-form">
          <el-form-item label="相似度阈值">
            <el-slider
              v-model="searchParams.similarityThreshold"
              :min="0.5"
              :max="1.0"
              :step="0.05"
              :format-tooltip="formatSimilarity"
              style="width: 200px"
            />
          </el-form-item>
          
          <el-form-item label="时间范围">
            <el-select v-model="searchParams.timeRange" style="width: 120px">
              <el-option label="最近7天" value="7d" />
              <el-option label="最近30天" value="30d" />
              <el-option label="最近3个月" value="3m" />
              <el-option label="全部" value="all" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="审核结果">
            <el-select v-model="searchParams.resultFilter" placeholder="全部" clearable style="width: 100px">
              <el-option label="通过" value="APPROVED" />
              <el-option label="拒绝" value="REJECTED" />
            </el-select>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="searchSimilarCases" :loading="searching">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 相似案例列表 -->
      <el-card class="cases-list-card">
        <template #header>
          <div class="list-header">
            <span>相似案例 ({{ similarCases.length }})</span>
            <div class="sort-options">
              <el-select v-model="sortBy" size="small" style="width: 120px" @change="sortCases">
                <el-option label="相似度" value="similarity" />
                <el-option label="时间" value="time" />
                <el-option label="置信度" value="confidence" />
              </el-select>
            </div>
          </div>
        </template>
        
        <div v-loading="searching" class="cases-list">
          <div v-if="similarCases.length === 0" class="no-cases">
            <el-empty description="暂无相似案例" :image-size="80" />
          </div>
          
          <div
            v-for="(similarCase, index) in similarCases"
            :key="index"
            class="similar-case"
            :class="{ 'highlighted': selectedCase?.id === similarCase.id }"
            @click="selectCase(similarCase)"
          >
            <div class="case-header">
              <div class="case-basic-info">
                <span class="case-id">案例 #{{ similarCase.id }}</span>
                <el-tag :type="getResultTagType(similarCase.result)" size="small">
                  {{ getResultLabel(similarCase.result) }}
                </el-tag>
                <el-tag :type="getRiskTagType(similarCase.riskLevel)" size="small">
                  {{ getRiskLabel(similarCase.riskLevel) }}
                </el-tag>
              </div>
              <div class="case-similarity">
                <div class="similarity-score">
                  <span class="score-label">相似度</span>
                  <div class="score-bar">
                    <el-progress
                      :percentage="Math.round(similarCase.similarity * 100)"
                      :stroke-width="6"
                      :show-text="false"
                      :color="getSimilarityColor(similarCase.similarity)"
                    />
                  </div>
                  <span class="score-value">{{ (similarCase.similarity * 100).toFixed(0) }}%</span>
                </div>
              </div>
            </div>
            
            <div class="case-content">
              <div class="content-section">
                <div class="content-text">{{ truncateText(similarCase.content, 150) }}</div>
                <el-button type="text" size="small" @click.stop="showFullContent(similarCase)">
                  查看完整内容
                </el-button>
              </div>
              
              <div v-if="similarCase.violations?.length" class="violations-section">
                <span class="violations-label">违规类型:</span>
                <el-tag 
                  v-for="violation in similarCase.violations" 
                  :key="violation"
                  type="warning" 
                  size="small"
                  class="violation-tag"
                >
                  {{ violation }}
                </el-tag>
              </div>
            </div>
            
            <div class="case-footer">
              <div class="case-meta">
                <span class="reviewer">审核员: {{ similarCase.reviewer }}</span>
                <span class="review-time">{{ formatDateTime(similarCase.reviewTime) }}</span>
                <span v-if="similarCase.confidence" class="confidence">
                  置信度: {{ (similarCase.confidence * 100).toFixed(1) }}%
                </span>
              </div>
              <div class="case-actions">
                <el-button type="text" size="small" @click.stop="viewCaseDetail(similarCase)">
                  <el-icon><View /></el-icon>
                  详情
                </el-button>
                <el-button type="text" size="small" @click.stop="applyCaseDecision(similarCase)">
                  <el-icon><DocumentCopy /></el-icon>
                  应用决定
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 加载更多 -->
        <div v-if="hasMore" class="load-more">
          <el-button @click="loadMoreCases" :loading="loadingMore">
            加载更多案例
          </el-button>
        </div>
      </el-card>

      <!-- 案例详情 -->
      <el-card v-if="selectedCase" class="case-detail-card">
        <template #header>
          <div class="detail-header">
            <span>案例详情 - #{{ selectedCase.id }}</span>
            <el-button type="text" size="small" @click="selectedCase = null">
              <el-icon><Close /></el-icon>
              关闭
            </el-button>
          </div>
        </template>
        
        <div class="case-detail">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="案例ID">{{ selectedCase.id }}</el-descriptions-item>
            <el-descriptions-item label="相似度">
              {{ (selectedCase.similarity * 100).toFixed(1) }}%
            </el-descriptions-item>
            <el-descriptions-item label="审核结果">
              <el-tag :type="getResultTagType(selectedCase.result)" size="small">
                {{ getResultLabel(selectedCase.result) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="风险等级">
              <el-tag :type="getRiskTagType(selectedCase.riskLevel)" size="small">
                {{ getRiskLabel(selectedCase.riskLevel) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="审核员">{{ selectedCase.reviewer }}</el-descriptions-item>
            <el-descriptions-item label="审核时间">
              {{ formatDateTime(selectedCase.reviewTime) }}
            </el-descriptions-item>
          </el-descriptions>
          
          <div class="detail-content">
            <h4>完整内容</h4>
            <pre class="full-content">{{ selectedCase.content }}</pre>
          </div>
          
          <div v-if="selectedCase.reviewNotes" class="detail-notes">
            <h4>审核备注</h4>
            <div class="notes-content">{{ selectedCase.reviewNotes }}</div>
          </div>
          
          <div class="detail-actions">
            <el-button type="primary" @click="applyCaseDecision(selectedCase)">
              <el-icon><DocumentCopy /></el-icon>
              应用此案例的审核决定
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="$emit('update:modelValue', false)">关闭</el-button>
        <el-button v-if="selectedCase" type="primary" @click="applyCaseDecision(selectedCase)">
          应用选中案例的决定
        </el-button>
      </div>
    </template>

    <!-- 内容详情对话框 -->
    <el-dialog
      v-model="showContentDialog"
      title="完整内容"
      width="600px"
    >
      <div class="full-content-dialog">
        <pre class="content-text">{{ fullContentText }}</pre>
      </div>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, View, DocumentCopy, Close } from '@element-plus/icons-vue'
import { type ModerationRecord } from '@/api/smartcs/moderation'
import { formatDateTime } from '@/utils/dateFormat'

// Props 和 Emits
interface Props {
  modelValue: boolean
  record: ModerationRecord | null
}

defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'applyDecision': [decision: any]
}>()

// 相似案例接口
interface SimilarCase {
  id: string
  content: string
  result: 'APPROVED' | 'REJECTED'
  riskLevel: string
  similarity: number
  confidence?: number
  violations?: string[]
  reviewer: string
  reviewTime: number
  reviewNotes?: string
  action: string
}

// 响应式数据
const searching = ref(false)
const loadingMore = ref(false)
const showContentDialog = ref(false)
const hasMore = ref(true)
const page = ref(1)
const sortBy = ref('similarity')
const selectedCase = ref<SimilarCase | null>(null)
const similarCases = ref<SimilarCase[]>([])
const fullContentText = ref('')

const searchParams = reactive({
  similarityThreshold: 0.7,
  timeRange: '30d',
  resultFilter: ''
})

// 生命周期
onMounted(() => {
  searchSimilarCases()
})

// 方法定义
const searchSimilarCases = async () => {
  searching.value = true
  page.value = 1
  hasMore.value = true
  
  try {
    // 模拟搜索相似案例
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    const mockCases: SimilarCase[] = [
      {
        id: 'SC001',
        content: '这是一个包含敏感词汇的示例内容，涉及政治敏感话题，需要谨慎处理。',
        result: 'REJECTED',
        riskLevel: 'HIGH',
        similarity: 0.92,
        confidence: 0.88,
        violations: ['政治敏感', '不当言论'],
        reviewer: '审核员A',
        reviewTime: Date.now() - 86400000,
        reviewNotes: '内容涉及政治敏感话题，根据平台规则予以拒绝',
        action: 'BLOCK'
      },
      {
        id: 'SC002',
        content: '类似的内容表达，但语境相对温和，经过仔细审核后予以通过。',
        result: 'APPROVED',
        riskLevel: 'MEDIUM',
        similarity: 0.78,
        confidence: 0.72,
        violations: ['疑似敏感'],
        reviewer: '审核员B',
        reviewTime: Date.now() - 172800000,
        reviewNotes: '虽有疑似敏感内容，但整体表达合理，予以通过',
        action: 'WARN'
      },
      {
        id: 'SC003',
        content: '相似的表达方式，但内容更加激进，明显违反社区准则。',
        result: 'REJECTED',
        riskLevel: 'CRITICAL',
        similarity: 0.85,
        confidence: 0.95,
        violations: ['极端言论', '煽动仇恨'],
        reviewer: '审核员C',
        reviewTime: Date.now() - 259200000,
        reviewNotes: '内容极端，存在煽动性，立即阻断并上报',
        action: 'ESCALATE'
      },
      {
        id: 'SC004',
        content: '表达方式接近，但经过修正后的版本，符合平台标准。',
        result: 'APPROVED',
        riskLevel: 'LOW',
        similarity: 0.65,
        confidence: 0.58,
        violations: [],
        reviewer: '审核员D',
        reviewTime: Date.now() - 345600000,
        reviewNotes: '内容经过适当调整，符合社区规范',
        action: 'WARN'
      }
    ]
    
    // 根据搜索参数过滤
    let filteredCases = mockCases.filter(c => 
      c.similarity >= searchParams.similarityThreshold
    )
    
    if (searchParams.resultFilter) {
      filteredCases = filteredCases.filter(c => c.result === searchParams.resultFilter)
    }
    
    similarCases.value = filteredCases
    hasMore.value = false // 模拟数据没有更多
    
  } catch (error) {
    console.error('Failed to search similar cases:', error)
    ElMessage.error('搜索相似案例失败')
  } finally {
    searching.value = false
  }
}

const loadMoreCases = async () => {
  loadingMore.value = true
  try {
    // 模拟加载更多
    await new Promise(resolve => setTimeout(resolve, 500))
    hasMore.value = false
  } finally {
    loadingMore.value = false
  }
}

const sortCases = () => {
  similarCases.value.sort((a, b) => {
    switch (sortBy.value) {
      case 'similarity':
        return b.similarity - a.similarity
      case 'time':
        return b.reviewTime - a.reviewTime
      case 'confidence':
        return (b.confidence || 0) - (a.confidence || 0)
      default:
        return 0
    }
  })
}

const selectCase = (caseItem: SimilarCase) => {
  selectedCase.value = selectedCase.value?.id === caseItem.id ? null : caseItem
}

const showFullContent = (caseItem: SimilarCase) => {
  fullContentText.value = caseItem.content
  showContentDialog.value = true
}

const viewCaseDetail = (caseItem: SimilarCase) => {
  selectedCase.value = caseItem
}

const applyCaseDecision = async (caseItem: SimilarCase) => {
  try {
    await ElMessageBox.confirm(
      `确认应用案例 #${caseItem.id} 的审核决定吗？\n结果: ${getResultLabel(caseItem.result)}\n动作: ${caseItem.action}`,
      '应用案例决定',
      { type: 'warning' }
    )
    
    const decision = {
      result: caseItem.result,
      action: caseItem.action,
      notes: `参考相似案例 #${caseItem.id}: ${caseItem.reviewNotes || '无备注'}`,
      referenceCase: caseItem.id
    }
    
    emit('applyDecision', decision)
    ElMessage.success('已应用案例决定')
    
  } catch (error) {
    // 用户取消操作
  }
}

// 辅助函数
const formatSimilarity = (value: number) => {
  return `${(value * 100).toFixed(0)}%`
}

const getSimilarityColor = (similarity: number) => {
  if (similarity >= 0.8) return '#f56565'
  if (similarity >= 0.6) return '#ed8936'
  return '#48bb78'
}

const truncateText = (text: string, maxLength: number) => {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
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

const getRiskTagType = (risk: string) => {
  const tags: Record<string, string> = {
    LOW: 'success',
    MEDIUM: 'info',
    HIGH: 'warning',
    CRITICAL: 'danger'
  }
  return tags[risk] || 'info'
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

const getResultTagType = (result: string) => {
  const tags: Record<string, string> = {
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return tags[result] || 'info'
}

const getResultLabel = (result: string) => {
  const labels: Record<string, string> = {
    APPROVED: '通过',
    REJECTED: '拒绝'
  }
  return labels[result] || result
}
</script>

<style scoped lang="scss">
.similar-cases {
  .current-case-card,
  .search-card,
  .cases-list-card,
  .case-detail-card {
    margin-bottom: 16px;
    
    &:last-child {
      margin-bottom: 0;
    }
  }

  .current-case {
    .case-meta {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 12px;

      .case-id {
        font-size: 12px;
        color: #909399;
        font-family: 'Courier New', monospace;
      }
    }

    .case-content {
      margin-bottom: 12px;

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
        max-height: 100px;
        overflow-y: auto;
      }
    }

    .case-violations {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;

      .violations-label {
        font-size: 12px;
        color: #606266;
      }

      .violation-tag {
        margin: 2px 0;
      }
    }
  }

  .search-form {
    .el-form-item {
      margin-bottom: 0;
    }
  }

  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .cases-list {
    .no-cases {
      text-align: center;
      padding: 40px 0;
    }

    .similar-case {
      border: 1px solid #ebeef5;
      border-radius: 6px;
      padding: 16px;
      margin-bottom: 12px;
      cursor: pointer;
      transition: all 0.3s;

      &:last-child {
        margin-bottom: 0;
      }

      &:hover {
        border-color: #c6e2ff;
        background-color: #f0f9ff;
      }

      &.highlighted {
        border-color: #409eff;
        background-color: #ecf5ff;
      }

      .case-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;

        .case-basic-info {
          display: flex;
          align-items: center;
          gap: 8px;

          .case-id {
            font-size: 12px;
            color: #909399;
            font-family: 'Courier New', monospace;
          }
        }

        .case-similarity {
          .similarity-score {
            display: flex;
            align-items: center;
            gap: 8px;
            min-width: 120px;

            .score-label {
              font-size: 12px;
              color: #606266;
              white-space: nowrap;
            }

            .score-bar {
              flex: 1;
              min-width: 60px;
            }

            .score-value {
              font-size: 12px;
              color: #303133;
              font-weight: 500;
              white-space: nowrap;
            }
          }
        }
      }

      .case-content {
        margin-bottom: 12px;

        .content-section {
          margin-bottom: 8px;

          .content-text {
            font-size: 12px;
            color: #606266;
            line-height: 1.4;
            margin-bottom: 4px;
          }
        }

        .violations-section {
          display: flex;
          align-items: center;
          gap: 6px;
          flex-wrap: wrap;

          .violations-label {
            font-size: 11px;
            color: #909399;
          }

          .violation-tag {
            font-size: 11px;
          }
        }
      }

      .case-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .case-meta {
          display: flex;
          gap: 12px;
          font-size: 11px;
          color: #909399;
        }

        .case-actions {
          display: flex;
          gap: 8px;
        }
      }
    }
  }

  .load-more {
    text-align: center;
    padding: 16px 0;
  }

  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .case-detail {
    .detail-content,
    .detail-notes {
      margin: 16px 0;

      h4 {
        margin: 0 0 8px 0;
        color: #303133;
        font-size: 14px;
      }

      .full-content {
        margin: 0;
        padding: 12px;
        background-color: #f5f7fa;
        border-radius: 4px;
        font-family: 'Courier New', monospace;
        font-size: 12px;
        line-height: 1.5;
        white-space: pre-wrap;
        word-wrap: break-word;
        max-height: 200px;
        overflow-y: auto;
      }

      .notes-content {
        padding: 12px;
        background-color: #fafafa;
        border-radius: 4px;
        font-size: 12px;
        color: #606266;
        line-height: 1.5;
      }
    }

    .detail-actions {
      text-align: center;
      padding: 16px 0;
    }
  }
}

.full-content-dialog {
  .content-text {
    margin: 0;
    padding: 16px;
    background-color: #f5f7fa;
    border-radius: 4px;
    font-family: 'Courier New', monospace;
    font-size: 12px;
    line-height: 1.5;
    white-space: pre-wrap;
    word-wrap: break-word;
    max-height: 400px;
    overflow-y: auto;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

// 响应式设计
@media (max-width: 768px) {
  .case-header {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
  }

  .case-footer {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;

    .case-meta {
      justify-content: space-between;
    }
  }

  .search-form {
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