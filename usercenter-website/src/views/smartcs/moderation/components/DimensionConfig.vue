<template>
  <div class="dimension-config">
    <div class="config-header">
      <div class="policy-info">
        <h3 class="policy-name">{{ policy.name }}</h3>
        <p class="policy-desc">{{ policy.description || '暂无描述' }}</p>
        <div class="policy-tags">
          <el-tag :type="getScenarioTagType(policy.scenario)">
            {{ getScenarioText(policy.scenario) }}
          </el-tag>
          <el-tag :type="getPolicyTypeTagType(policy.policyType)">
            {{ getPolicyTypeText(policy.policyType) }}
          </el-tag>
        </div>
      </div>
    </div>

    <div class="config-content">
      <div class="dimension-selection">
        <div class="selection-header">
          <div class="title-section">
            <h4>选择审核维度</h4>
            <p class="subtitle">选择适用于此策略的审核维度，并配置相应的权重和阈值</p>
          </div>
          <div class="batch-actions">
            <el-button size="small" @click="handleSelectAll">全选</el-button>
            <el-button size="small" @click="handleSelectNone">取消全选</el-button>
            <el-button size="small" type="primary" @click="handleAddRecommended">
              添加推荐维度
            </el-button>
          </div>
        </div>

        <div class="dimensions-grid">
          <div
            v-for="dimension in availableDimensions"
            :key="dimension.id"
            class="dimension-card"
            :class="{ 'selected': isSelected(dimension.id!) }"
            @click="toggleDimension(dimension)"
          >
            <div class="card-header">
              <div class="card-title">
                <el-checkbox
                  :model-value="isSelected(dimension.id!)"
                  @change="toggleDimension(dimension)"
                  @click.stop
                />
                <span class="dimension-name">{{ dimension.name }}</span>
                <el-tag
                  size="small"
                  :type="getSeverityTagType(dimension.severityLevel)"
                >
                  {{ getSeverityText(dimension.severityLevel) }}
                </el-tag>
              </div>
            </div>
            
            <div class="card-content">
              <p class="dimension-desc">{{ dimension.description || '暂无描述' }}</p>
              <div class="dimension-meta">
                <span class="meta-item">
                  <el-icon><Warning /></el-icon>
                  阈值: {{ dimension.confidenceThreshold }}
                </span>
                <span class="meta-item">
                  <el-icon><Setting /></el-icon>
                  {{ getActionText(dimension.actionType) }}
                </span>
              </div>
            </div>

            <!-- 选中时显示配置项 -->
            <div v-if="isSelected(dimension.id!)" class="card-config" @click.stop>
              <el-divider />
              <div class="config-items">
                <div class="config-item">
                  <label>权重:</label>
                  <el-slider
                    :model-value="getConfigValue(dimension.id!, 'weight', 1.0)"
                    :min="0.1"
                    :max="2.0"
                    :step="0.1"
                    style="width: 120px"
                    @input="updateConfig(dimension.id!, 'weight', $event)"
                  />
                  <span class="value-display">
                    {{ getConfigValue(dimension.id!, 'weight', 1.0) }}
                  </span>
                </div>
                
                <div class="config-item">
                  <label>自定义阈值:</label>
                  <el-slider
                    :model-value="getConfigValue(dimension.id!, 'customThreshold', dimension.confidenceThreshold)"
                    :min="0.1"
                    :max="1.0"
                    :step="0.01"
                    style="width: 120px"
                    @input="updateConfig(dimension.id!, 'customThreshold', $event)"
                  />
                  <span class="value-display">
                    {{ getConfigValue(dimension.id!, 'customThreshold', dimension.confidenceThreshold) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="config-summary">
        <el-card>
          <template #header>
            <div class="summary-header">
              <span>配置摘要</span>
              <el-tag type="info">已选择 {{ selectedConfigs.length }} 个维度</el-tag>
            </div>
          </template>
          
          <div v-if="selectedConfigs.length === 0" class="empty-state">
            <el-icon><Box /></el-icon>
            <p>暂未选择任何审核维度</p>
          </div>
          
          <div v-else class="selected-list">
            <div
              v-for="config in selectedConfigs"
              :key="config.dimensionId"
              class="selected-item"
            >
              <div class="item-info">
                <span class="item-name">{{ getDimensionName(config.dimensionId) }}</span>
                <el-tag size="small" :type="getDimensionSeverityTagType(config.dimensionId)">
                  {{ getDimensionSeverityText(config.dimensionId) }}
                </el-tag>
              </div>
              <div class="item-config">
                <span class="config-detail">权重: {{ config.weight }}</span>
                <span class="config-detail">阈值: {{ config.customThreshold }}</span>
                <el-button
                  size="small"
                  type="danger"
                  link
                  @click="removeDimension(config.dimensionId)"
                >
                  移除
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <div class="config-actions">
      <el-button @click="handleCancel">取消</el-button>
      <el-button
        type="primary"
        :loading="saving"
        :disabled="selectedConfigs.length === 0"
        @click="handleSave"
      >
        保存配置
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning, Setting, Box } from '@element-plus/icons-vue'
import { 
  moderationApi, 
  type ModerationPolicy, 
  type ModerationDimension, 
  type PolicyDimensionConfig 
} from '@/api/smartcs/moderation'

interface Props {
  policy: ModerationPolicy
}

interface Emits {
  (e: 'save'): void
  (e: 'close'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 数据状态
const loading = ref(false)
const saving = ref(false)
const availableDimensions = ref<ModerationDimension[]>([])
const selectedConfigs = ref<PolicyDimensionConfig[]>([])

// 页面加载
onMounted(async () => {
  await Promise.all([
    loadAvailableDimensions(),
    loadPolicyDimensions()
  ])
})

// 加载可用维度
const loadAvailableDimensions = async () => {
  try {
    const response = await moderationApi.getAllActiveDimensions()
    availableDimensions.value = response.data
  } catch (error) {
    console.error('加载可用维度失败:', error)
    ElMessage.error('加载可用维度失败')
  }
}

// 加载策略已配置的维度
const loadPolicyDimensions = async () => {
  if (!props.policy.id) return
  
  try {
    const response = await moderationApi.getPolicyDimensions(props.policy.id)
    const policyDimensions = response.data
    
    selectedConfigs.value = policyDimensions.map(dimension => ({
      dimensionId: dimension.id!,
      isActive: true,
      weight: 1.0, // 这里应该从后端获取实际配置的权重
      customThreshold: dimension.confidenceThreshold
    }))
  } catch (error) {
    console.error('加载策略维度失败:', error)
  }
}

// 判断维度是否已选择
const isSelected = (dimensionId: number) => {
  return selectedConfigs.value.some(config => config.dimensionId === dimensionId)
}

// 切换维度选择状态
const toggleDimension = (dimension: ModerationDimension) => {
  const dimensionId = dimension.id!
  const index = selectedConfigs.value.findIndex(config => config.dimensionId === dimensionId)
  
  if (index >= 0) {
    selectedConfigs.value.splice(index, 1)
  } else {
    selectedConfigs.value.push({
      dimensionId,
      isActive: true,
      weight: 1.0,
      customThreshold: dimension.confidenceThreshold || 0.7
    })
  }
}

// 移除维度
const removeDimension = (dimensionId: number) => {
  const index = selectedConfigs.value.findIndex(config => config.dimensionId === dimensionId)
  if (index >= 0) {
    selectedConfigs.value.splice(index, 1)
  }
}

// 全选
const handleSelectAll = () => {
  availableDimensions.value.forEach(dimension => {
    if (!isSelected(dimension.id!)) {
      selectedConfigs.value.push({
        dimensionId: dimension.id!,
        isActive: true,
        weight: 1.0,
        customThreshold: dimension.confidenceThreshold || 0.7
      })
    }
  })
}

// 取消全选
const handleSelectNone = () => {
  selectedConfigs.value = []
}

// 添加推荐维度
const handleAddRecommended = () => {
  const recommendedDimensions = getRecommendedDimensions()
  recommendedDimensions.forEach(dimension => {
    if (!isSelected(dimension.id!)) {
      selectedConfigs.value.push({
        dimensionId: dimension.id!,
        isActive: true,
        weight: getRecommendedWeight(dimension),
        customThreshold: getRecommendedThreshold(dimension)
      })
    }
  })
  ElMessage.success(`已添加 ${recommendedDimensions.length} 个推荐维度`)
}

// 获取推荐维度（根据策略类型和场景）
const getRecommendedDimensions = () => {
  const { policyType, scenario } = props.policy
  
  return availableDimensions.value.filter(dimension => {
    // 根据策略类型推荐维度
    if (policyType === 'STRICT') {
      return ['HIGH', 'CRITICAL'].includes(dimension.severityLevel || '')
    } else if (policyType === 'LENIENT') {
      return dimension.severityLevel === 'CRITICAL'
    } else {
      // STANDARD
      return ['MEDIUM', 'HIGH', 'CRITICAL'].includes(dimension.severityLevel || '')
    }
  })
}

// 获取推荐权重
const getRecommendedWeight = (dimension: ModerationDimension) => {
  const { policyType } = props.policy
  const severity = dimension.severityLevel
  
  if (policyType === 'STRICT') {
    return severity === 'CRITICAL' ? 1.5 : 1.2
  } else if (policyType === 'LENIENT') {
    return 0.8
  } else {
    return 1.0
  }
}

// 获取推荐阈值
const getRecommendedThreshold = (dimension: ModerationDimension) => {
  const { policyType } = props.policy
  const baseThreshold = dimension.confidenceThreshold || 0.7
  
  if (policyType === 'STRICT') {
    return Math.max(0.1, baseThreshold - 0.1)
  } else if (policyType === 'LENIENT') {
    return Math.min(0.9, baseThreshold + 0.1)
  } else {
    return baseThreshold
  }
}

// 获取配置值
const getConfigValue = (dimensionId: number, field: string, defaultValue: any) => {
  const config = selectedConfigs.value.find(c => c.dimensionId === dimensionId)
  return config ? (config as any)[field] : defaultValue
}

// 更新配置值
const updateConfig = (dimensionId: number, field: string, value: any) => {
  const config = selectedConfigs.value.find(c => c.dimensionId === dimensionId)
  if (config) {
    (config as any)[field] = value
  }
}

// 获取维度名称
const getDimensionName = (dimensionId: number) => {
  const dimension = availableDimensions.value.find(d => d.id === dimensionId)
  return dimension?.name || ''
}

// 获取维度严重级别
const getDimensionSeverityText = (dimensionId: number) => {
  const dimension = availableDimensions.value.find(d => d.id === dimensionId)
  return getSeverityText(dimension?.severityLevel)
}

const getDimensionSeverityTagType = (dimensionId: number) => {
  const dimension = availableDimensions.value.find(d => d.id === dimensionId)
  return getSeverityTagType(dimension?.severityLevel)
}

// 保存配置
const handleSave = async () => {
  if (!props.policy.id || selectedConfigs.value.length === 0) {
    ElMessage.warning('请至少选择一个审核维度')
    return
  }
  
  saving.value = true
  try {
    await moderationApi.configurePolicyDimensions(props.policy.id, selectedConfigs.value)
    ElMessage.success('维度配置保存成功')
    emit('save')
  } catch (error) {
    console.error('保存维度配置失败:', error)
    ElMessage.error('保存维度配置失败')
  } finally {
    saving.value = false
  }
}

// 取消
const handleCancel = () => {
  emit('close')
}

// 工具函数
const getScenarioText = (scenario: string) => {
  const map: Record<string, string> = {
    'USER_CHAT': '用户聊天',
    'BOT_REPLY': '机器人回复',
    'CONTENT_PUBLISH': '内容发布'
  }
  return map[scenario] || scenario
}

const getScenarioTagType = (scenario: string) => {
  const map: Record<string, string> = {
    'USER_CHAT': 'primary',
    'BOT_REPLY': 'success',
    'CONTENT_PUBLISH': 'warning'
  }
  return map[scenario] || ''
}

const getPolicyTypeText = (type: string) => {
  const map: Record<string, string> = {
    'STANDARD': '标准',
    'STRICT': '严格',
    'LENIENT': '宽松'
  }
  return map[type] || type
}

const getPolicyTypeTagType = (type: string) => {
  const map: Record<string, string> = {
    'STANDARD': '',
    'STRICT': 'danger',
    'LENIENT': 'info'
  }
  return map[type] || ''
}

const getSeverityText = (severity?: string) => {
  const map: Record<string, string> = {
    'LOW': '低风险',
    'MEDIUM': '中风险',
    'HIGH': '高风险',
    'CRITICAL': '极高风险'
  }
  return map[severity || ''] || '未知'
}

const getSeverityTagType = (severity?: string) => {
  const map: Record<string, string> = {
    'LOW': 'info',
    'MEDIUM': '',
    'HIGH': 'warning',
    'CRITICAL': 'danger'
  }
  return map[severity || ''] || ''
}

const getActionText = (action?: string) => {
  const map: Record<string, string> = {
    'APPROVE': '通过',
    'REJECT': '拒绝',
    'MANUAL_REVIEW': '人工审核',
    'AUTO_FIX': '自动修复'
  }
  return map[action || ''] || '未知'
}
</script>

<style scoped lang="scss">
.dimension-config {
  .config-header {
    margin-bottom: 24px;
    
    .policy-info {
      .policy-name {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 8px 0;
      }
      
      .policy-desc {
        color: #606266;
        font-size: 14px;
        margin: 0 0 12px 0;
      }
      
      .policy-tags {
        display: flex;
        gap: 8px;
      }
    }
  }

  .config-content {
    display: flex;
    gap: 24px;
    
    .dimension-selection {
      flex: 1;
      
      .selection-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-end;
        margin-bottom: 16px;
        
        .title-section {
          h4 {
            font-size: 16px;
            font-weight: 600;
            color: #303133;
            margin: 0 0 4px 0;
          }
          
          .subtitle {
            color: #909399;
            font-size: 13px;
            margin: 0;
          }
        }
        
        .batch-actions {
          display: flex;
          gap: 8px;
        }
      }
      
      .dimensions-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
        gap: 16px;
        
        .dimension-card {
          border: 1px solid #e4e7ed;
          border-radius: 8px;
          padding: 16px;
          cursor: pointer;
          transition: all 0.3s ease;
          
          &:hover {
            border-color: #409eff;
            box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
          }
          
          &.selected {
            border-color: #409eff;
            background: #f0f8ff;
          }
          
          .card-header {
            .card-title {
              display: flex;
              align-items: center;
              gap: 8px;
              margin-bottom: 8px;
              
              .dimension-name {
                font-weight: 500;
                color: #303133;
                flex: 1;
              }
            }
          }
          
          .card-content {
            .dimension-desc {
              color: #606266;
              font-size: 13px;
              margin: 0 0 12px 0;
              line-height: 1.4;
            }
            
            .dimension-meta {
              display: flex;
              gap: 16px;
              
              .meta-item {
                display: flex;
                align-items: center;
                gap: 4px;
                color: #909399;
                font-size: 12px;
              }
            }
          }
          
          .card-config {
            margin-top: 16px;
            
            .config-items {
              display: flex;
              flex-direction: column;
              gap: 12px;
              
              .config-item {
                display: flex;
                align-items: center;
                gap: 12px;
                
                label {
                  width: 80px;
                  font-size: 13px;
                  color: #606266;
                }
                
                .value-display {
                  width: 40px;
                  font-size: 13px;
                  color: #303133;
                  font-weight: 500;
                }
              }
            }
          }
        }
      }
    }
    
    .config-summary {
      width: 320px;
      
      .summary-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
      
      .empty-state {
        text-align: center;
        padding: 40px 20px;
        color: #909399;
        
        .el-icon {
          font-size: 48px;
          margin-bottom: 16px;
        }
        
        p {
          margin: 0;
          font-size: 14px;
        }
      }
      
      .selected-list {
        .selected-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 12px 0;
          border-bottom: 1px solid #f0f0f0;
          
          &:last-child {
            border-bottom: none;
          }
          
          .item-info {
            .item-name {
              display: block;
              font-weight: 500;
              color: #303133;
              margin-bottom: 4px;
            }
          }
          
          .item-config {
            text-align: right;
            
            .config-detail {
              display: block;
              font-size: 12px;
              color: #606266;
              margin-bottom: 2px;
            }
          }
        }
      }
    }
  }

  .config-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding-top: 24px;
    border-top: 1px solid #e4e7ed;
    margin-top: 24px;
  }
}
</style>