<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    title="人工审核"
    width="700px"
    :close-on-click-modal="false"
  >
    <div class="review-dialog" v-if="record">
      <!-- 审核内容预览 -->
      <el-card class="content-preview-card">
        <template #header>
          <span>待审核内容</span>
          <div class="content-info">
            <el-tag :type="getContentTypeTag(record.contentType)" size="small">
              {{ getContentTypeLabel(record.contentType) }}
            </el-tag>
            <el-tag 
              v-if="record.riskLevel" 
              :type="getRiskTagType(record.riskLevel)" 
              size="small"
            >
              风险: {{ getRiskLabel(record.riskLevel) }}
            </el-tag>
          </div>
        </template>
        
        <div class="content-display">
          <div class="original-content">
            <h4>原始内容:</h4>
            <pre class="content-text">{{ record.originalContent }}</pre>
          </div>
          
          <!-- 违规信息 -->
          <div class="violation-info" v-if="record.violationCategories?.length">
            <h4>检测到的违规:</h4>
            <div class="violations-list">
              <div 
                v-for="(violation, index) in record.violationCategories" 
                :key="index"
                class="violation-item"
              >
                <el-tag type="danger" size="small">{{ violation.categoryName }}</el-tag>
                <span class="confidence">置信度: {{ (violation.confidence * 100).toFixed(1) }}%</span>
              </div>
            </div>
          </div>

          <!-- 关键词匹配 -->
          <div class="keyword-info" v-if="record.keywordMatches?.length">
            <h4>匹配的关键词:</h4>
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
      </el-card>

      <!-- 审核表单 -->
      <el-card class="review-form-card">
        <template #header>
          <span>审核决定</span>
        </template>

        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="120px"
          class="review-form"
        >
          <el-form-item label="审核结果" prop="result" required>
            <el-radio-group v-model="formData.result" class="result-radio-group">
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
            <el-select v-model="formData.action" placeholder="请选择处理动作" style="width: 100%">
              <el-option
                v-for="action in actionOptions"
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
              v-model="formData.notes"
              type="textarea"
              :rows="4"
              placeholder="请输入审核备注（可选），说明审核理由或需要注意的问题"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <!-- 审核建议 -->
          <el-form-item label="AI建议" v-if="aiSuggestion">
            <div class="ai-suggestion">
              <el-alert
                :title="aiSuggestion.title"
                :type="aiSuggestion.type"
                :description="aiSuggestion.description"
                show-icon
                :closable="false"
              />
            </div>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 历史审核记录 -->
      <el-card class="history-card" v-if="historyReviews?.length">
        <template #header>
          <span>历史审核记录</span>
        </template>
        
        <div class="history-list">
          <div 
            v-for="(history, index) in historyReviews" 
            :key="index"
            class="history-item"
          >
            <div class="history-header">
              <div class="reviewer">{{ history.reviewer || '系统' }}</div>
              <div class="time">{{ formatDateTime(history.reviewedAt) }}</div>
            </div>
            <div class="history-content">
              <el-tag :type="history.result === 'APPROVED' ? 'success' : 'danger'" size="small">
                {{ history.result === 'APPROVED' ? '通过' : '拒绝' }}
              </el-tag>
              <span class="action">动作: {{ getActionLabel(history.action) }}</span>
            </div>
            <div class="history-notes" v-if="history.notes">
              备注: {{ history.notes }}
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          提交审核
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Check, Close } from '@element-plus/icons-vue'
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
const historyReviews = ref<any[]>([])

const formData = reactive({
  result: '' as 'APPROVED' | 'REJECTED',
  action: '' as 'WARN' | 'REVIEW' | 'BLOCK' | 'ESCALATE',
  notes: ''
})

// 计算属性
const actionOptions = computed(() => [
  {
    value: 'WARN',
    label: '警告',
    description: '发出警告，记录违规行为'
  },
  {
    value: 'REVIEW',
    label: '人工审核',
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

const aiSuggestion = computed(() => {
  if (!props.record) return null
  
  const { moderationResult, riskLevel, confidenceScore } = props.record
  
  if (moderationResult === 'REJECTED' && riskLevel === 'HIGH') {
    return {
      title: 'AI建议: 建议拒绝',
      type: 'error',
      description: `检测到高风险违规内容，置信度 ${((confidenceScore || 0) * 100).toFixed(1)}%，建议拒绝并阻断`
    }
  } else if (moderationResult === 'APPROVED' && riskLevel === 'LOW') {
    return {
      title: 'AI建议: 建议通过',
      type: 'success',
      description: `内容风险较低，置信度 ${((confidenceScore || 0) * 100).toFixed(1)}%，建议通过`
    }
  } else {
    return {
      title: 'AI建议: 需要人工判断',
      type: 'warning',
      description: '系统无法确定，请根据内容和违规情况人工判断'
    }
  }
})

// 表单验证规则
const formRules: FormRules = {
  result: [
    { required: true, message: '请选择审核结果', trigger: 'change' }
  ],
  action: [
    { required: true, message: '请选择处理动作', trigger: 'change' }
  ]
}

// 监听器
watch(
  () => props.record,
  (newRecord) => {
    if (newRecord && props.modelValue) {
      resetForm()
      loadHistoryReviews()
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
    } else {
      resetForm()
    }
  }
)

// 方法定义
const resetForm = () => {
  Object.assign(formData, {
    result: '',
    action: '',
    notes: ''
  })
  formRef.value?.clearValidate()
}

const loadHistoryReviews = async () => {
  if (!props.record?.id) return
  
  try {
    // 模拟历史审核记录，实际应该调用API
    historyReviews.value = []
  } catch (error) {
    console.error('Failed to load history reviews:', error)
  }
}

const isActionAvailable = (action: string) => {
  // 根据审核结果限制可用的动作
  if (formData.result === 'APPROVED') {
    return ['WARN'].includes(action)
  } else if (formData.result === 'REJECTED') {
    return ['REVIEW', 'BLOCK', 'ESCALATE'].includes(action)
  }
  return true
}

const handleCancel = () => {
  emit('update:modelValue', false)
}

const handleSubmit = async () => {
  if (!formRef.value || !props.record?.id) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    await moderationApi.manualReview(props.record.id, {
      result: formData.result,
      action: formData.action,
      notes: formData.notes || undefined
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
    MEDIUM: 'warning',
    HIGH: 'danger',
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

const getActionLabel = (action: string) => {
  const labels: Record<string, string> = {
    WARN: '警告',
    REVIEW: '审核',
    BLOCK: '阻断',
    ESCALATE: '升级'
  }
  return labels[action] || action
}
</script>

<style scoped lang="scss">
.review-dialog {
  .content-preview-card,
  .review-form-card,
  .history-card {
    margin-bottom: 16px;
    
    &:last-child {
      margin-bottom: 0;
    }

    :deep(.el-card__header) {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .content-info {
    display: flex;
    gap: 8px;
  }

  .content-display {
    .original-content {
      margin-bottom: 16px;

      h4 {
        margin: 0 0 8px 0;
        color: #303133;
        font-size: 14px;
      }

      .content-text {
        margin: 0;
        padding: 12px;
        background-color: #f5f7fa;
        border-radius: 4px;
        font-family: 'Courier New', monospace;
        white-space: pre-wrap;
        word-wrap: break-word;
        line-height: 1.5;
        max-height: 200px;
        overflow-y: auto;
      }
    }

    .violation-info,
    .keyword-info {
      margin-bottom: 16px;

      &:last-child {
        margin-bottom: 0;
      }

      h4 {
        margin: 0 0 8px 0;
        color: #303133;
        font-size: 14px;
      }

      .violations-list {
        .violation-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 8px 12px;
          border: 1px solid #dcdfe6;
          border-radius: 4px;
          margin-bottom: 8px;

          &:last-child {
            margin-bottom: 0;
          }

          .confidence {
            font-size: 12px;
            color: #909399;
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

  .review-form {
    .result-radio-group {
      :deep(.el-radio-button) {
        margin-right: 12px;
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

    .ai-suggestion {
      :deep(.el-alert) {
        margin: 0;
      }
    }
  }

  .history-list {
    .history-item {
      padding: 12px;
      border: 1px solid #ebeef5;
      border-radius: 4px;
      margin-bottom: 12px;

      &:last-child {
        margin-bottom: 0;
      }

      .history-header {
        display: flex;
        justify-content: space-between;
        margin-bottom: 8px;

        .reviewer {
          font-weight: 500;
          color: #303133;
        }

        .time {
          font-size: 12px;
          color: #909399;
        }
      }

      .history-content {
        display: flex;
        gap: 12px;
        align-items: center;
        margin-bottom: 4px;

        .action {
          font-size: 12px;
          color: #606266;
        }
      }

      .history-notes {
        font-size: 12px;
        color: #909399;
        line-height: 1.4;
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
  .content-info {
    flex-direction: column;
    gap: 4px;
  }

  .result-radio-group {
    :deep(.el-radio-button) {
      display: block;
      margin-bottom: 8px;
      margin-right: 0;
    }
  }
}
</style>