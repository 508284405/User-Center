<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    title="违规详情"
    width="600px"
    :close-on-click-modal="false"
  >
    <div class="violation-detail" v-if="violations?.length">
      <div class="violations-overview">
        <el-alert
          title="检测到违规内容"
          :description="`共检测到 ${violations.length} 项违规，请查看详细信息`"
          type="warning"
          show-icon
          :closable="false"
        />
      </div>

      <div class="violations-list">
        <div 
          v-for="(violation, index) in violations" 
          :key="index"
          class="violation-card"
        >
          <el-card>
            <template #header>
              <div class="violation-header">
                <div class="category-info">
                  <el-tag type="danger" size="default">
                    {{ violation.categoryName }}
                  </el-tag>
                  <span class="category-code">{{ violation.categoryCode }}</span>
                </div>
                <div class="confidence-score">
                  <span class="score-label">置信度</span>
                  <el-progress
                    :percentage="Math.round(violation.confidence * 100)"
                    :stroke-width="8"
                    :show-text="true"
                    :color="getConfidenceColor(violation.confidence)"
                    class="confidence-progress"
                  />
                </div>
              </div>
            </template>

            <div class="violation-content">
              <!-- 基本信息 -->
              <el-descriptions :column="1" size="small" border>
                <el-descriptions-item label="违规分类">
                  {{ violation.categoryName }}
                </el-descriptions-item>
                <el-descriptions-item label="分类编码">
                  <el-tag type="info" size="small">{{ violation.categoryCode }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="置信度">
                  <div class="confidence-detail">
                    <span class="confidence-value">{{ (violation.confidence * 100).toFixed(2) }}%</span>
                    <el-tag 
                      :type="getConfidenceLevel(violation.confidence)" 
                      size="small"
                      class="confidence-tag"
                    >
                      {{ getConfidenceLevelText(violation.confidence) }}
                    </el-tag>
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="触发规则" v-if="violation.triggerRule">
                  <div class="trigger-rule">
                    <el-tag type="warning" size="small">{{ violation.triggerRule }}</el-tag>
                  </div>
                </el-descriptions-item>
              </el-descriptions>

              <!-- 风险级别指示 -->
              <div class="risk-indicator">
                <div class="risk-level">
                  <span class="risk-label">风险级别:</span>
                  <el-tag 
                    :type="getRiskTagType(violation.confidence)" 
                    size="small"
                    class="risk-tag"
                  >
                    {{ getRiskLevelText(violation.confidence) }}
                  </el-tag>
                </div>
                <div class="risk-description">
                  {{ getRiskDescription(violation.confidence) }}
                </div>
              </div>

              <!-- 建议处理方式 -->
              <div class="recommended-actions">
                <h5>建议处理方式:</h5>
                <div class="actions-list">
                  <el-tag 
                    v-for="action in getRecommendedActions(violation.confidence)"
                    :key="action.type"
                    :type="action.tagType"
                    size="small"
                    class="action-tag"
                  >
                    {{ action.label }}
                  </el-tag>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 总体分析 -->
      <el-card class="summary-card">
        <template #header>
          <span>总体分析</span>
        </template>
        
        <div class="summary-content">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="summary-item">
                <div class="summary-value">{{ violations.length }}</div>
                <div class="summary-label">违规项目</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="summary-item">
                <div class="summary-value">{{ getMaxConfidence() }}%</div>
                <div class="summary-label">最高置信度</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="summary-item">
                <div class="summary-value">{{ getOverallRisk() }}</div>
                <div class="summary-label">综合风险</div>
              </div>
            </el-col>
          </el-row>

          <div class="overall-recommendation">
            <h5>综合建议:</h5>
            <el-alert
              :title="getOverallRecommendation().title"
              :type="getOverallRecommendation().type"
              :description="getOverallRecommendation().description"
              show-icon
              :closable="false"
            />
          </div>
        </div>
      </el-card>
    </div>

    <div v-else class="no-violations">
      <el-empty description="暂无违规信息" />
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="$emit('update:modelValue', false)">关闭</el-button>
        <el-button type="primary" @click="exportViolationReport">
          导出报告
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { type ViolationCategory } from '@/api/smartcs/moderation'

// Props 和 Emits
interface Props {
  modelValue: boolean
  violations: ViolationCategory[] | null
}

const props = defineProps<Props>()
defineEmits<{
  'update:modelValue': [value: boolean]
}>()

// 计算属性
const getMaxConfidence = () => {
  if (!props.violations?.length) return 0
  return Math.round(Math.max(...props.violations.map(v => v.confidence)) * 100)
}

const getOverallRisk = () => {
  if (!props.violations?.length) return '无'
  
  const maxConfidence = Math.max(...props.violations.map(v => v.confidence))
  if (maxConfidence >= 0.9) return '极高'
  if (maxConfidence >= 0.7) return '高'
  if (maxConfidence >= 0.5) return '中'
  return '低'
}

const getOverallRecommendation = () => {
  if (!props.violations?.length) {
    return {
      title: '无违规检测',
      type: 'success',
      description: '内容未检测到违规问题'
    }
  }

  const maxConfidence = Math.max(...props.violations.map(v => v.confidence))
  const violationCount = props.violations.length

  if (maxConfidence >= 0.9 || violationCount >= 3) {
    return {
      title: '建议立即阻断',
      type: 'error',
      description: '检测到高置信度违规或多项违规，建议立即阻断并进行人工审核'
    }
  } else if (maxConfidence >= 0.7 || violationCount >= 2) {
    return {
      title: '建议人工审核',
      type: 'warning',
      description: '检测到中等风险违规，建议进行人工审核确认'
    }
  } else {
    return {
      title: '建议警告处理',
      type: 'info',
      description: '检测到低风险违规，可以警告处理并持续监控'
    }
  }
}

// 方法定义
const getConfidenceColor = (confidence: number) => {
  if (confidence >= 0.8) return '#f56565'
  if (confidence >= 0.6) return '#ed8936'
  if (confidence >= 0.4) return '#ecc94b'
  return '#48bb78'
}

const getConfidenceLevel = (confidence: number) => {
  if (confidence >= 0.8) return 'danger'
  if (confidence >= 0.6) return 'warning'
  if (confidence >= 0.4) return 'info'
  return 'success'
}

const getConfidenceLevelText = (confidence: number) => {
  if (confidence >= 0.8) return '高置信度'
  if (confidence >= 0.6) return '中置信度'
  if (confidence >= 0.4) return '低置信度'
  return '极低置信度'
}

const getRiskTagType = (confidence: number) => {
  if (confidence >= 0.8) return 'danger'
  if (confidence >= 0.6) return 'warning'
  if (confidence >= 0.4) return 'info'
  return 'success'
}

const getRiskLevelText = (confidence: number) => {
  if (confidence >= 0.8) return '高风险'
  if (confidence >= 0.6) return '中风险'
  if (confidence >= 0.4) return '低风险'
  return '极低风险'
}

const getRiskDescription = (confidence: number) => {
  if (confidence >= 0.8) return '存在明显违规内容，建议立即处理'
  if (confidence >= 0.6) return '可能存在违规内容，建议人工确认'
  if (confidence >= 0.4) return '疑似违规内容，可以继续监控'
  return '违规可能性较低，无需特殊处理'
}

const getRecommendedActions = (confidence: number) => {
  if (confidence >= 0.8) {
    return [
      { type: 'BLOCK', label: '立即阻断', tagType: 'danger' },
      { type: 'ESCALATE', label: '升级处理', tagType: 'danger' }
    ]
  } else if (confidence >= 0.6) {
    return [
      { type: 'REVIEW', label: '人工审核', tagType: 'warning' },
      { type: 'BLOCK', label: '临时阻断', tagType: 'warning' }
    ]
  } else if (confidence >= 0.4) {
    return [
      { type: 'WARN', label: '发出警告', tagType: 'info' },
      { type: 'REVIEW', label: '人工审核', tagType: 'info' }
    ]
  } else {
    return [
      { type: 'WARN', label: '记录警告', tagType: 'success' }
    ]
  }
}

const exportViolationReport = () => {
  if (!props.violations?.length) {
    ElMessage.warning('暂无违规信息可导出')
    return
  }

  try {
    const reportData = generateReport()
    downloadReport(reportData)
    ElMessage.success('报告导出成功')
  } catch (error) {
    console.error('Export failed:', error)
    ElMessage.error('导出失败')
  }
}

const generateReport = () => {
  const violations = props.violations || []
  const timestamp = new Date().toLocaleString('zh-CN')
  
  let report = `违规检测报告\n`
  report += `生成时间: ${timestamp}\n`
  report += `违规项目数: ${violations.length}\n`
  report += `最高置信度: ${getMaxConfidence()}%\n`
  report += `综合风险: ${getOverallRisk()}\n`
  report += `\n详细信息:\n`
  
  violations.forEach((violation, index) => {
    report += `\n${index + 1}. ${violation.categoryName}\n`
    report += `   分类编码: ${violation.categoryCode}\n`
    report += `   置信度: ${(violation.confidence * 100).toFixed(2)}%\n`
    if (violation.triggerRule) {
      report += `   触发规则: ${violation.triggerRule}\n`
    }
  })
  
  const recommendation = getOverallRecommendation()
  report += `\n处理建议: ${recommendation.title}\n`
  report += `建议说明: ${recommendation.description}\n`
  
  return report
}

const downloadReport = (content: string) => {
  const blob = new Blob(['\uFEFF' + content], { type: 'text/plain;charset=utf-8' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `violation_report_${Date.now()}.txt`
  link.click()
  URL.revokeObjectURL(link.href)
}
</script>

<style scoped lang="scss">
.violation-detail {
  .violations-overview {
    margin-bottom: 20px;
  }

  .violations-list {
    .violation-card {
      margin-bottom: 16px;
      
      &:last-child {
        margin-bottom: 0;
      }

      .violation-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .category-info {
          display: flex;
          align-items: center;
          gap: 12px;

          .category-code {
            font-size: 12px;
            color: #909399;
            font-family: 'Courier New', monospace;
          }
        }

        .confidence-score {
          display: flex;
          align-items: center;
          gap: 8px;
          min-width: 180px;

          .score-label {
            font-size: 12px;
            color: #606266;
            white-space: nowrap;
          }

          .confidence-progress {
            flex: 1;
          }
        }
      }

      .violation-content {
        .confidence-detail {
          display: flex;
          align-items: center;
          gap: 8px;

          .confidence-value {
            font-weight: bold;
            color: #303133;
          }

          .confidence-tag {
            margin-left: 4px;
          }
        }

        .trigger-rule {
          .el-tag {
            font-family: 'Courier New', monospace;
          }
        }

        .risk-indicator {
          margin: 16px 0;
          padding: 12px;
          background-color: #f5f7fa;
          border-radius: 4px;

          .risk-level {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 4px;

            .risk-label {
              font-weight: 500;
              color: #303133;
            }
          }

          .risk-description {
            font-size: 12px;
            color: #606266;
            line-height: 1.4;
          }
        }

        .recommended-actions {
          margin-top: 16px;

          h5 {
            margin: 0 0 8px 0;
            font-size: 14px;
            color: #303133;
          }

          .actions-list {
            .action-tag {
              margin: 4px 8px 4px 0;
            }
          }
        }
      }
    }
  }

  .summary-card {
    margin-top: 20px;

    .summary-content {
      .summary-item {
        text-align: center;
        padding: 16px;

        .summary-value {
          font-size: 24px;
          font-weight: bold;
          color: #409eff;
          margin-bottom: 8px;
        }

        .summary-label {
          font-size: 12px;
          color: #909399;
        }
      }

      .overall-recommendation {
        margin-top: 20px;

        h5 {
          margin: 0 0 12px 0;
          font-size: 14px;
          color: #303133;
        }
      }
    }
  }
}

.no-violations {
  padding: 40px 0;
  text-align: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

// 响应式设计
@media (max-width: 768px) {
  .violation-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;

    .confidence-score {
      min-width: auto;
    }
  }

  .confidence-detail {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .summary-content {
    :deep(.el-row) {
      .el-col {
        margin-bottom: 12px;
      }
    }
  }
}
</style>