<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    title="详细审核"
    width="900px"
    :close-on-click-modal="false"
  >
    <div class="detailed-review" v-if="record">
      <el-row :gutter="20">
        <!-- 左侧：内容展示 -->
        <el-col :span="14">
          <el-card class="content-card">
            <template #header>
              <span>审核内容</span>
            </template>
            
            <!-- 基本信息 -->
            <div class="basic-info">
              <el-descriptions :column="2" size="small" border>
                <el-descriptions-item label="记录ID">{{ record.id }}</el-descriptions-item>
                <el-descriptions-item label="内容类型">
                  <el-tag :type="getContentTypeTag(record.contentType)" size="small">
                    {{ getContentTypeLabel(record.contentType) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="风险等级">
                  <el-tag 
                    v-if="record.riskLevel" 
                    :type="getRiskTagType(record.riskLevel)" 
                    size="small"
                  >
                    {{ getRiskLabel(record.riskLevel) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="置信度">
                  <span v-if="record.confidenceScore">
                    {{ (record.confidenceScore * 100).toFixed(1) }}%
                  </span>
                </el-descriptions-item>
                <el-descriptions-item label="用户ID">{{ record.userId || '匿名' }}</el-descriptions-item>
                <el-descriptions-item label="创建时间">
                  {{ formatDateTime(record.createdAt) }}
                </el-descriptions-item>
              </el-descriptions>
            </div>

            <!-- 原始内容 -->
            <div class="content-section">
              <h4>原始内容</h4>
              <div class="content-display">
                <pre class="content-text">{{ record.originalContent }}</pre>
                <div class="content-actions">
                  <el-button type="text" size="small" @click="copyContent">
                    <el-icon><DocumentCopy /></el-icon>
                    复制
                  </el-button>
                  <el-button type="text" size="small" @click="analyzeContent">
                    <el-icon><Search /></el-icon>
                    深度分析
                  </el-button>
                </div>
              </div>
            </div>

            <!-- 违规详情 -->
            <div v-if="record.violationCategories?.length" class="violation-section">
              <h4>检测到的违规 ({{ record.violationCategories.length }})</h4>
              <div class="violations-list">
                <div 
                  v-for="(violation, index) in record.violationCategories" 
                  :key="index"
                  class="violation-card"
                >
                  <div class="violation-header">
                    <el-tag type="danger" size="default">{{ violation.categoryName }}</el-tag>
                    <span class="confidence">置信度: {{ (violation.confidence * 100).toFixed(1) }}%</span>
                  </div>
                  <div class="violation-details">
                    <p><strong>分类编码:</strong> {{ violation.categoryCode }}</p>
                    <p v-if="violation.triggerRule"><strong>触发规则:</strong> {{ violation.triggerRule }}</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- 关键词匹配 -->
            <div v-if="record.keywordMatches?.length" class="keyword-section">
              <h4>匹配的关键词</h4>
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

            <!-- AI分析结果 -->
            <div v-if="aiAnalysis" class="ai-analysis-section">
              <h4>AI深度分析</h4>
              <div class="analysis-result">
                <el-alert
                  :title="aiAnalysis.title"
                  :type="aiAnalysis.type"
                  :description="aiAnalysis.description"
                  show-icon
                  :closable="false"
                />
                <div v-if="aiAnalysis.details" class="analysis-details">
                  <ul>
                    <li v-for="detail in aiAnalysis.details" :key="detail">{{ detail }}</li>
                  </ul>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：审核操作 -->
        <el-col :span="10">
          <el-card class="review-card">
            <template #header>
              <span>审核决定</span>
            </template>
            
            <el-form
              ref="formRef"
              :model="reviewForm"
              :rules="reviewRules"
              label-width="100px"
              class="review-form"
            >
              <el-form-item label="审核结果" prop="result" required>
                <el-radio-group v-model="reviewForm.result" class="result-group">
                  <el-radio-button label="APPROVED">
                    <el-icon><Check /></el-icon>
                    通过
                  </el-radio-button>
                  <el-radio-button label="REJECTED">
                    <el-icon><Close /></el-icon>
                    拒绝
                  </el-radio-button>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="处理动作" prop="action" required>
                <el-select v-model="reviewForm.action" placeholder="选择处理动作" style="width: 100%">
                  <el-option
                    v-for="action in availableActions"
                    :key="action.value"
                    :label="action.label"
                    :value="action.value"
                    :disabled="!isActionAvailable(action.value)"
                  >
                    <div class="action-option">
                      <span class="action-name">{{ action.label }}</span>
                      <span class="action-desc">{{ action.description }}</span>
                    </div>
                  </el-option>
                </el-select>
              </el-form-item>

              <el-form-item label="审核备注" prop="notes">
                <el-input
                  v-model="reviewForm.notes"
                  type="textarea"
                  :rows="4"
                  placeholder="请详细说明审核理由和决定依据"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="严重程度">
                <el-radio-group v-model="reviewForm.severity">
                  <el-radio label="LOW">轻微</el-radio>
                  <el-radio label="MEDIUM">一般</el-radio>
                  <el-radio label="HIGH">严重</el-radio>
                  <el-radio label="CRITICAL">极严重</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="后续跟进">
                <el-checkbox-group v-model="reviewForm.followUps">
                  <el-checkbox label="monitor_user">监控用户行为</el-checkbox>
                  <el-checkbox label="update_rules">更新审核规则</el-checkbox>
                  <el-checkbox label="escalate">上报管理员</el-checkbox>
                  <el-checkbox label="notify_user">通知用户</el-checkbox>
                </el-checkbox-group>
              </el-form-item>

              <!-- AI建议 -->
              <el-form-item label="AI建议" v-if="aiRecommendation">
                <el-alert
                  :title="aiRecommendation.title"
                  :type="aiRecommendation.type"
                  :description="aiRecommendation.description"
                  show-icon
                  :closable="false"
                />
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 相似案例 -->
          <el-card class="similar-cases-card">
            <template #header>
              <div class="similar-header">
                <span>相似案例参考</span>
                <el-button type="text" size="small" @click="loadSimilarCases">
                  <el-icon><Refresh /></el-icon>
                  刷新
                </el-button>
              </div>
            </template>
            
            <div v-loading="loadingSimilar" class="similar-cases">
              <div v-if="similarCases.length === 0" class="no-cases">
                <el-empty description="暂无相似案例" :image-size="60" />
              </div>
              
              <div
                v-for="(similarCase, index) in similarCases"
                :key="index"
                class="similar-case"
              >
                <div class="case-header">
                  <span class="case-id">案例 #{{ similarCase.id }}</span>
                  <el-tag :type="getResultTagType(similarCase.result)" size="small">
                    {{ getResultLabel(similarCase.result) }}
                  </el-tag>
                </div>
                <div class="case-content">
                  <p class="case-text">{{ truncateText(similarCase.content, 80) }}</p>
                  <div class="case-meta">
                    <span class="similarity">相似度: {{ (similarCase.similarity * 100).toFixed(0) }}%</span>
                    <span class="reviewer">审核员: {{ similarCase.reviewer }}</span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button @click="saveAsDraft" :loading="saving">
          <el-icon><Document /></el-icon>
          保存草稿
        </el-button>
        <el-button type="primary" @click="submitReview" :loading="submitting">
          <el-icon><Check /></el-icon>
          提交审核
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { 
  DocumentCopy, Search, Check, Close, Refresh, Document 
} from '@element-plus/icons-vue'
import moderationApi, { type ModerationRecord } from '@/api/smartcs/moderation'
import { formatDateTime } from '@/utils/dateFormat'

// Props 和 Emits
interface Props {
  modelValue: boolean
  record: ModerationRecord | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

// 响应式数据
const formRef = ref<FormInstance>()
const submitting = ref(false)
const saving = ref(false)
const loadingSimilar = ref(false)
const aiAnalysis = ref<any>(null)
const similarCases = ref<any[]>([])

const reviewForm = reactive({
  result: '' as 'APPROVED' | 'REJECTED',
  action: '' as 'WARN' | 'REVIEW' | 'BLOCK' | 'ESCALATE',
  notes: '',
  severity: 'MEDIUM',
  followUps: [] as string[]
})

// 计算属性
const availableActions = computed(() => [
  {
    value: 'WARN',
    label: '警告',
    description: '发出警告，记录违规行为'
  },
  {
    value: 'REVIEW',
    label: '人工复审',
    description: '标记需要进一步人工审核'
  },
  {
    value: 'BLOCK',
    label: '阻断',
    description: '阻止内容显示或传播'
  },
  {
    value: 'ESCALATE',
    label: '升级处理',
    description: '升级到高级管理员处理'
  }
])

const aiRecommendation = computed(() => {
  if (!props.record) return null
  
  const { moderationResult, riskLevel, confidenceScore } = props.record
  
  if (riskLevel === 'CRITICAL' || (confidenceScore && confidenceScore > 0.9)) {
    return {
      title: 'AI建议: 建议拒绝',
      type: 'error',
      description: '检测到高风险违规内容，建议拒绝并采取阻断措施'
    }
  } else if (riskLevel === 'HIGH' || (confidenceScore && confidenceScore > 0.7)) {
    return {
      title: 'AI建议: 需要谨慎审核',
      type: 'warning',
      description: '检测到中高风险内容，建议仔细审核后决定'
    }
  } else {
    return {
      title: 'AI建议: 可考虑通过',
      type: 'info',
      description: '风险相对较低，可根据具体情况考虑通过'
    }
  }
})

// 表单验证规则
const reviewRules: FormRules = {
  result: [
    { required: true, message: '请选择审核结果', trigger: 'change' }
  ],
  action: [
    { required: true, message: '请选择处理动作', trigger: 'change' }
  ],
  notes: [
    { required: true, message: '请输入审核备注', trigger: 'blur' },
    { min: 10, message: '审核备注至少10个字符', trigger: 'blur' }
  ]
}

// 监听器
watch(
  () => props.record,
  (newRecord) => {
    if (newRecord && props.modelValue) {
      resetForm()
      loadSimilarCases()
    }
  },
  { immediate: true }
)

watch(
  () => props.modelValue,
  (show) => {
    if (show) {
      nextTick(() => {
        formRef.value?.clearValidate()
      })
    }
  }
)

// 方法定义
const resetForm = () => {
  Object.assign(reviewForm, {
    result: '',
    action: '',
    notes: '',
    severity: 'MEDIUM',
    followUps: []
  })
  aiAnalysis.value = null
}

const isActionAvailable = (action: string) => {
  if (reviewForm.result === 'APPROVED') {
    return ['WARN'].includes(action)
  } else if (reviewForm.result === 'REJECTED') {
    return ['REVIEW', 'BLOCK', 'ESCALATE'].includes(action)
  }
  return true
}

const copyContent = () => {
  if (!props.record?.originalContent) return
  
  navigator.clipboard.writeText(props.record.originalContent)
    .then(() => {
      ElMessage.success('内容已复制到剪贴板')
    })
    .catch(() => {
      ElMessage.error('复制失败')
    })
}

const analyzeContent = async () => {
  if (!props.record?.originalContent) return
  
  try {
    // 模拟AI深度分析
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    aiAnalysis.value = {
      title: 'AI深度分析结果',
      type: 'warning',
      description: '检测到可能的违规内容，建议仔细审核',
      details: [
        '检测到敏感词汇：涉及不当内容',
        '语义分析：存在隐含违规表达',
        '上下文分析：可能误导用户',
        '建议：建议进行人工审核确认'
      ]
    }
    
    ElMessage.success('深度分析完成')
  } catch (error) {
    console.error('Analysis failed:', error)
    ElMessage.error('分析失败')
  }
}

const loadSimilarCases = async () => {
  if (!props.record) return
  
  loadingSimilar.value = true
  try {
    // 模拟加载相似案例
    await new Promise(resolve => setTimeout(resolve, 800))
    
    similarCases.value = [
      {
        id: 'R001',
        content: '这是一个相似的违规内容示例，包含类似的敏感词汇',
        result: 'REJECTED',
        similarity: 0.85,
        reviewer: '审核员A'
      },
      {
        id: 'R002',
        content: '另一个相似案例，处理方式可以作为参考',
        result: 'APPROVED',
        similarity: 0.72,
        reviewer: '审核员B'
      },
      {
        id: 'R003',
        content: '具有相似特征的内容，但处理结果不同',
        result: 'REJECTED',
        similarity: 0.68,
        reviewer: '审核员C'
      }
    ]
  } catch (error) {
    console.error('Failed to load similar cases:', error)
  } finally {
    loadingSimilar.value = false
  }
}

const handleCancel = () => {
  emit('update:modelValue', false)
}

const saveAsDraft = async () => {
  // 保存为草稿功能
  saving.value = true
  try {
    // 这里可以调用保存草稿的API
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('草稿已保存')
  } catch (error) {
    console.error('Save draft failed:', error)
    ElMessage.error('保存草稿失败')
  } finally {
    saving.value = false
  }
}

const submitReview = async () => {
  if (!formRef.value || !props.record?.id) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    await moderationApi.manualReview(props.record.id, {
      result: reviewForm.result,
      action: reviewForm.action,
      notes: reviewForm.notes
    })

    ElMessage.success('审核提交成功')
    emit('success')
  } catch (error: any) {
    console.error('Review submission failed:', error)
    const message = error.response?.data?.message || '审核提交失败'
    ElMessage.error(message)
  } finally {
    submitting.value = false
  }
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

const truncateText = (text: string, maxLength: number) => {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}
</script>

<style scoped lang="scss">
.detailed-review {
  .content-card,
  .review-card,
  .similar-cases-card {
    margin-bottom: 16px;
    
    &:last-child {
      margin-bottom: 0;
    }
  }

  .basic-info {
    margin-bottom: 20px;
  }

  .content-section,
  .violation-section,
  .keyword-section,
  .ai-analysis-section {
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 0;
    }

    h4 {
      margin: 0 0 12px 0;
      color: #303133;
      font-size: 14px;
    }

    .content-display {
      position: relative;

      .content-text {
        margin: 0;
        padding: 16px;
        background-color: #f5f7fa;
        border-radius: 6px;
        font-family: 'Courier New', monospace;
        font-size: 12px;
        line-height: 1.6;
        white-space: pre-wrap;
        word-wrap: break-word;
        max-height: 200px;
        overflow-y: auto;
      }

      .content-actions {
        position: absolute;
        top: 8px;
        right: 8px;
        display: flex;
        gap: 8px;
      }
    }

    .violations-list {
      .violation-card {
        border: 1px solid #dcdfe6;
        border-radius: 6px;
        padding: 12px;
        margin-bottom: 12px;

        &:last-child {
          margin-bottom: 0;
        }

        .violation-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;

          .confidence {
            font-size: 12px;
            color: #909399;
          }
        }

        .violation-details {
          font-size: 12px;
          color: #606266;

          p {
            margin: 4px 0;
          }
        }
      }
    }

    .keywords {
      .keyword-tag {
        margin: 4px 8px 4px 0;
      }
    }

    .analysis-result {
      .analysis-details {
        margin-top: 12px;

        ul {
          margin: 0;
          padding-left: 20px;

          li {
            margin: 4px 0;
            color: #606266;
            font-size: 12px;
          }
        }
      }
    }
  }

  .review-form {
    .result-group {
      width: 100%;

      :deep(.el-radio-button) {
        flex: 1;

        .el-radio-button__inner {
          width: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 6px;
        }
      }
    }

    .action-option {
      display: flex;
      flex-direction: column;
      
      .action-name {
        font-weight: 500;
      }
      
      .action-desc {
        font-size: 12px;
        color: #909399;
        margin-top: 2px;
      }
    }
  }

  .similar-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .similar-cases {
    .no-cases {
      text-align: center;
      padding: 20px 0;
    }

    .similar-case {
      border: 1px solid #ebeef5;
      border-radius: 4px;
      padding: 12px;
      margin-bottom: 12px;

      &:last-child {
        margin-bottom: 0;
      }

      .case-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .case-id {
          font-size: 12px;
          color: #909399;
          font-family: 'Courier New', monospace;
        }
      }

      .case-content {
        .case-text {
          font-size: 12px;
          color: #606266;
          line-height: 1.4;
          margin: 0 0 8px 0;
        }

        .case-meta {
          display: flex;
          justify-content: space-between;
          font-size: 11px;
          color: #909399;
        }
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

// 响应式设计
@media (max-width: 768px) {
  .result-group {
    :deep(.el-radio-button) {
      display: block;
      margin-bottom: 8px;

      .el-radio-button__inner {
        width: 100%;
      }
    }
  }

  .case-meta {
    flex-direction: column;
    gap: 4px;
  }
}
</style>