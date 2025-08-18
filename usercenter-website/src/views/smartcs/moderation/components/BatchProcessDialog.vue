<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    title="批量处理审核"
    width="800px"
    :close-on-click-modal="false"
  >
    <div class="batch-process" v-if="records.length > 0">
      <!-- 批量操作概览 -->
      <el-card class="overview-card">
        <template #header>
          <span>批量操作概览</span>
        </template>
        
        <div class="batch-overview">
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="overview-item">
                <div class="overview-value">{{ records.length }}</div>
                <div class="overview-label">选中记录</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="overview-item">
                <div class="overview-value">{{ riskDistribution.critical }}</div>
                <div class="overview-label">极高风险</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="overview-item">
                <div class="overview-value">{{ riskDistribution.high }}</div>
                <div class="overview-label">高风险</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="overview-item">
                <div class="overview-value">{{ riskDistribution.other }}</div>
                <div class="overview-label">中低风险</div>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>

      <!-- 批量操作配置 -->
      <el-card class="config-card">
        <template #header>
          <span>批量操作配置</span>
        </template>
        
        <el-form
          ref="formRef"
          :model="batchForm"
          :rules="batchRules"
          label-width="120px"
          class="batch-form"
        >
          <el-form-item label="操作类型" prop="operationType" required>
            <el-radio-group v-model="batchForm.operationType" @change="handleOperationChange">
              <el-radio-button label="APPROVE_ALL">
                <el-icon><Check /></el-icon>
                全部通过
              </el-radio-button>
              <el-radio-button label="REJECT_ALL">
                <el-icon><Close /></el-icon>
                全部拒绝
              </el-radio-button>
              <el-radio-button label="CONDITIONAL">
                <el-icon><Filter /></el-icon>
                条件处理
              </el-radio-button>
            </el-radio-group>
          </el-form-item>

          <!-- 条件处理配置 -->
          <template v-if="batchForm.operationType === 'CONDITIONAL'">
            <el-form-item label="处理规则" prop="conditionalRules">
              <div class="conditional-rules">
                <div
                  v-for="(rule, index) in batchForm.conditionalRules"
                  :key="index"
                  class="rule-item"
                >
                  <el-select v-model="rule.condition" placeholder="选择条件" style="width: 150px">
                    <el-option label="风险等级" value="riskLevel" />
                    <el-option label="置信度" value="confidence" />
                    <el-option label="违规分类" value="category" />
                    <el-option label="内容类型" value="contentType" />
                  </el-select>
                  
                  <el-select v-model="rule.operator" placeholder="操作符" style="width: 100px">
                    <el-option label="等于" value="eq" />
                    <el-option label="大于" value="gt" />
                    <el-option label="小于" value="lt" />
                    <el-option label="包含" value="contains" />
                  </el-select>
                  
                  <el-input 
                    v-model="rule.value" 
                    placeholder="值" 
                    style="width: 120px"
                  />
                  
                  <el-select v-model="rule.action" placeholder="执行动作" style="width: 100px">
                    <el-option label="通过" value="APPROVED" />
                    <el-option label="拒绝" value="REJECTED" />
                  </el-select>
                  
                  <el-button 
                    type="text" 
                    @click="removeRule(index)"
                    :disabled="batchForm.conditionalRules.length === 1"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
                
                <el-button type="text" @click="addRule">
                  <el-icon><Plus /></el-icon>
                  添加规则
                </el-button>
              </div>
            </el-form-item>
          </template>

          <el-form-item label="默认动作" prop="defaultAction" required>
            <el-select v-model="batchForm.defaultAction" style="width: 200px">
              <el-option label="警告" value="WARN" />
              <el-option label="审核" value="REVIEW" />
              <el-option label="阻断" value="BLOCK" />
              <el-option label="升级" value="ESCALATE" />
            </el-select>
          </el-form-item>

          <el-form-item label="批量备注" prop="batchNotes" required>
            <el-input
              v-model="batchForm.batchNotes"
              type="textarea"
              :rows="3"
              placeholder="请输入批量处理的备注说明"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="处理选项">
            <el-checkbox-group v-model="batchForm.options">
              <el-checkbox label="notify_users">通知相关用户</el-checkbox>
              <el-checkbox label="update_rules">自动更新规则</el-checkbox>
              <el-checkbox label="create_report">生成处理报告</el-checkbox>
              <el-checkbox label="backup_data">备份处理数据</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 预览结果 -->
      <el-card class="preview-card">
        <template #header>
          <div class="preview-header">
            <span>处理预览</span>
            <el-button type="text" size="small" @click="previewResults">
              <el-icon><View /></el-icon>
              刷新预览
            </el-button>
          </div>
        </template>
        
        <div v-loading="previewing" class="preview-results">
          <div v-if="previewData.length === 0" class="no-preview">
            <el-empty description="点击刷新预览查看处理结果" :image-size="60" />
          </div>
          
          <div v-else class="preview-list">
            <div class="preview-summary">
              <el-tag type="success" size="small">通过: {{ previewSummary.approved }}</el-tag>
              <el-tag type="danger" size="small">拒绝: {{ previewSummary.rejected }}</el-tag>
              <el-tag type="info" size="small">跳过: {{ previewSummary.skipped }}</el-tag>
            </div>
            
            <el-table :data="previewData" size="small" max-height="300">
              <el-table-column prop="id" label="记录ID" width="80" />
              <el-table-column prop="riskLevel" label="风险等级" width="100">
                <template #default="{ row }">
                  <el-tag :type="getRiskTagType(row.riskLevel)" size="small">
                    {{ getRiskLabel(row.riskLevel) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="predictedResult" label="预测结果" width="100">
                <template #default="{ row }">
                  <el-tag :type="getResultTagType(row.predictedResult)" size="small">
                    {{ getResultLabel(row.predictedResult) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="predictedAction" label="预测动作" width="100" />
              <el-table-column prop="reason" label="处理原因" show-overflow-tooltip />
            </el-table>
          </div>
        </div>
      </el-card>

      <!-- 处理进度 -->
      <el-card v-if="processing" class="progress-card">
        <template #header>
          <span>批量处理进度</span>
        </template>
        
        <div class="batch-progress">
          <el-progress 
            :percentage="progress.percentage" 
            :status="progress.status"
            :stroke-width="10"
          />
          <div class="progress-info">
            <span>{{ progress.message }}</span>
            <span class="progress-detail">
              {{ progress.processed }} / {{ progress.total }}
            </span>
          </div>
          <div v-if="progress.errors.length > 0" class="progress-errors">
            <el-alert
              title="处理过程中出现错误"
              type="warning"
              :description="`${progress.errors.length} 条记录处理失败`"
              show-icon
              :closable="false"
            />
          </div>
        </div>
      </el-card>
    </div>

    <div v-else class="empty-selection">
      <el-empty description="请先选择要批量处理的记录" />
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel" :disabled="processing">取消</el-button>
        <el-button @click="previewResults" :loading="previewing">
          <el-icon><View /></el-icon>
          预览结果
        </el-button>
        <el-button 
          type="primary" 
          @click="executeBatchProcess" 
          :loading="processing"
          :disabled="records.length === 0 || !batchForm.operationType"
        >
          <el-icon><Check /></el-icon>
          执行批量处理
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Check, Close, Filter, Delete, Plus, View } from '@element-plus/icons-vue'
import moderationApi from '@/api/smartcs/moderation'

// Props 和 Emits
interface Props {
  modelValue: boolean
  records: number[] // 选中的记录ID列表
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

// 条件规则接口
interface ConditionalRule {
  condition: string
  operator: string
  value: string
  action: 'APPROVED' | 'REJECTED'
}

// 响应式数据
const formRef = ref<FormInstance>()
const processing = ref(false)
const previewing = ref(false)
const previewData = ref<any[]>([])

const batchForm = reactive({
  operationType: '' as 'APPROVE_ALL' | 'REJECT_ALL' | 'CONDITIONAL',
  conditionalRules: [
    { condition: 'riskLevel', operator: 'eq', value: 'HIGH', action: 'REJECTED' as const }
  ] as ConditionalRule[],
  defaultAction: 'BLOCK' as 'WARN' | 'REVIEW' | 'BLOCK' | 'ESCALATE',
  batchNotes: '',
  options: [] as string[]
})

const progress = reactive({
  percentage: 0,
  status: 'success' as 'success' | 'exception' | 'warning',
  message: '',
  processed: 0,
  total: 0,
  errors: [] as string[]
})

// 计算属性
const riskDistribution = computed(() => {
  // 模拟风险分布统计
  return {
    critical: Math.floor(props.records.length * 0.1),
    high: Math.floor(props.records.length * 0.2),
    other: props.records.length - Math.floor(props.records.length * 0.3)
  }
})

const previewSummary = computed(() => {
  const approved = previewData.value.filter(item => item.predictedResult === 'APPROVED').length
  const rejected = previewData.value.filter(item => item.predictedResult === 'REJECTED').length
  const skipped = previewData.value.length - approved - rejected
  
  return { approved, rejected, skipped }
})

// 表单验证规则
const batchRules: FormRules = {
  operationType: [
    { required: true, message: '请选择操作类型', trigger: 'change' }
  ],
  defaultAction: [
    { required: true, message: '请选择默认动作', trigger: 'change' }
  ],
  batchNotes: [
    { required: true, message: '请输入批量备注', trigger: 'blur' },
    { min: 10, message: '备注至少10个字符', trigger: 'blur' }
  ]
}

// 监听器
watch(
  () => props.modelValue,
  (show) => {
    if (show) {
      resetForm()
    }
  }
)

// 方法定义
const resetForm = () => {
  Object.assign(batchForm, {
    operationType: '',
    conditionalRules: [
      { condition: 'riskLevel', operator: 'eq', value: 'HIGH', action: 'REJECTED' as const }
    ],
    defaultAction: 'BLOCK',
    batchNotes: '',
    options: []
  })
  
  previewData.value = []
  
  Object.assign(progress, {
    percentage: 0,
    status: 'success',
    message: '',
    processed: 0,
    total: 0,
    errors: []
  })
}

const handleOperationChange = () => {
  // 根据操作类型设置默认值
  if (batchForm.operationType === 'APPROVE_ALL') {
    batchForm.defaultAction = 'WARN'
    batchForm.batchNotes = '批量通过处理'
  } else if (batchForm.operationType === 'REJECT_ALL') {
    batchForm.defaultAction = 'BLOCK'
    batchForm.batchNotes = '批量拒绝处理'
  }
}

const addRule = () => {
  batchForm.conditionalRules.push({
    condition: 'riskLevel',
    operator: 'eq',
    value: '',
    action: 'REJECTED'
  })
}

const removeRule = (index: number) => {
  if (batchForm.conditionalRules.length > 1) {
    batchForm.conditionalRules.splice(index, 1)
  }
}

const previewResults = async () => {
  if (!batchForm.operationType) {
    ElMessage.warning('请先选择操作类型')
    return
  }
  
  previewing.value = true
  try {
    // 模拟预览计算
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    previewData.value = props.records.map(id => {
      const mockRisk = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'][Math.floor(Math.random() * 4)]
      let predictedResult = 'APPROVED'
      let reason = '符合通过条件'
      
      if (batchForm.operationType === 'REJECT_ALL') {
        predictedResult = 'REJECTED'
        reason = '批量拒绝'
      } else if (batchForm.operationType === 'CONDITIONAL') {
        // 根据条件判断
        for (const rule of batchForm.conditionalRules) {
          if (rule.condition === 'riskLevel' && rule.operator === 'eq' && mockRisk === rule.value) {
            predictedResult = rule.action
            reason = `匹配规则: ${rule.condition} ${rule.operator} ${rule.value}`
            break
          }
        }
      }
      
      return {
        id,
        riskLevel: mockRisk,
        predictedResult,
        predictedAction: batchForm.defaultAction,
        reason
      }
    })
    
    ElMessage.success('预览计算完成')
  } catch (error) {
    console.error('Preview failed:', error)
    ElMessage.error('预览失败')
  } finally {
    previewing.value = false
  }
}

const executeBatchProcess = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认批量处理 ${props.records.length} 条记录吗？此操作不可撤销！`,
      '批量处理确认',
      { type: 'warning' }
    )

    processing.value = true
    progress.total = props.records.length
    progress.processed = 0
    progress.percentage = 0
    progress.errors = []
    progress.message = '开始批量处理...'

    // 模拟批量处理
    for (let i = 0; i < props.records.length; i++) {
      const recordId = props.records[i]
      
      try {
        // 模拟处理单个记录
        await new Promise(resolve => setTimeout(resolve, 200))
        
        // 这里应该调用实际的批量处理API
        // await moderationApi.batchProcess([recordId], batchForm.operationType)
        
        progress.processed++
        progress.percentage = Math.round((progress.processed / progress.total) * 100)
        progress.message = `正在处理 ${progress.processed}/${progress.total}`
        
      } catch (error) {
        progress.errors.push(`记录 ${recordId} 处理失败`)
        console.error(`Failed to process record ${recordId}:`, error)
      }
    }

    if (progress.errors.length === 0) {
      progress.status = 'success'
      progress.message = '批量处理完成'
      ElMessage.success('批量处理成功')
      emit('success')
    } else {
      progress.status = 'warning'
      progress.message = `处理完成，${progress.errors.length} 条记录失败`
      ElMessage.warning(`部分记录处理失败: ${progress.errors.length}/${progress.total}`)
    }

  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('Batch process failed:', error)
      ElMessage.error('批量处理失败')
      progress.status = 'exception'
      progress.message = '批量处理失败'
    }
  } finally {
    processing.value = false
  }
}

const handleCancel = () => {
  emit('update:modelValue', false)
}

// 辅助函数
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
    LOW: '低',
    MEDIUM: '中',
    HIGH: '高',
    CRITICAL: '极高'
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
.batch-process {
  .overview-card,
  .config-card,
  .preview-card,
  .progress-card {
    margin-bottom: 16px;
    
    &:last-child {
      margin-bottom: 0;
    }
  }

  .batch-overview {
    .overview-item {
      text-align: center;
      padding: 12px;

      .overview-value {
        font-size: 24px;
        font-weight: bold;
        color: #409eff;
        margin-bottom: 4px;
      }

      .overview-label {
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .batch-form {
    .conditional-rules {
      .rule-item {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;

        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }

  .preview-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .preview-results {
    .no-preview {
      text-align: center;
      padding: 20px 0;
    }

    .preview-list {
      .preview-summary {
        display: flex;
        gap: 8px;
        margin-bottom: 16px;
      }
    }
  }

  .batch-progress {
    .progress-info {
      display: flex;
      justify-content: space-between;
      margin: 12px 0;
      font-size: 14px;
      color: #606266;

      .progress-detail {
        color: #909399;
      }
    }

    .progress-errors {
      margin-top: 16px;
    }
  }
}

.empty-selection {
  text-align: center;
  padding: 40px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

// 响应式设计
@media (max-width: 768px) {
  .batch-overview {
    .overview-item {
      margin-bottom: 12px;
    }
  }

  .rule-item {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;

    > * {
      width: 100% !important;
    }
  }

  .preview-summary {
    flex-direction: column;
    gap: 4px;
  }
}
</style>