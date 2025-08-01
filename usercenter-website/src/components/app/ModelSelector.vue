<template>
  <div class="model-selector">
    <div class="selector-label" v-if="label">
      <span>{{ label }}</span>
      <span v-if="required" class="required">*</span>
    </div>
    
    <el-select 
      v-model="selectedModelId" 
      :placeholder="placeholder"
      :style="{ width: width }"
      :loading="loading"
      :disabled="disabled"
      :clearable="clearable"
      :filterable="filterable"
      @change="handleModelChange"
      @clear="handleClear"
    >
      <el-option-group
        v-for="group in groupedModels"
        :key="group.label"
        :label="group.label"
      >
        <el-option
          v-for="model in group.models"
          :key="model.id"
          :label="model.label"
          :value="model.id"
          :disabled="model.status !== 'ACTIVE'"
        >
          <div class="model-option">
            <div class="model-info">
              <span class="model-name">{{ model.label }}</span>
              <div class="model-meta">
                <el-tag 
                  v-for="type in model.modelType" 
                  :key="type" 
                  size="small" 
                  effect="plain"
                  class="model-type-tag"
                >
                  {{ type }}
                </el-tag>
                <el-tag 
                  v-if="model.features" 
                  size="small" 
                  type="info" 
                  effect="plain"
                  class="features-tag"
                >
                  {{ getFeaturesSummary(model.features) }}
                </el-tag>
              </div>
            </div>
            <div class="model-status">
              <el-tag 
                :type="getStatusType(model.status)" 
                size="small"
              >
                {{ getStatusText(model.status) }}
              </el-tag>
            </div>
          </div>
        </el-option>
      </el-option-group>
      
      <!-- 无分组时的普通显示 -->
      <el-option
        v-if="!groupByProvider"
        v-for="model in filteredModels"
        :key="model.id"
        :label="model.label"
        :value="model.id"
        :disabled="model.status !== 'ACTIVE'"
      >
        <div class="model-option">
          <div class="model-info">
            <span class="model-name">{{ model.label }}</span>
            <div class="model-meta">
              <el-tag 
                v-for="type in model.modelType" 
                :key="type" 
                size="small" 
                effect="plain"
                class="model-type-tag"
              >
                {{ type }}
              </el-tag>
              <el-tag 
                v-if="model.features" 
                size="small" 
                type="info" 
                effect="plain"
                class="features-tag"
              >
                {{ getFeaturesSummary(model.features) }}
              </el-tag>
            </div>
          </div>
          <div class="model-status">
            <el-tag 
              :type="getStatusType(model.status)" 
              size="small"
            >
              {{ getStatusText(model.status) }}
            </el-tag>
          </div>
        </div>
      </el-option>
    </el-select>
    
    <!-- 模型详情展示 -->
    <div v-if="selectedModel && showDetails" class="model-details">
      <div class="details-header">
        <span class="details-title">选中模型详情</span>
        <el-button 
          text 
          size="small" 
          @click="showDetails = false"
        >
          隐藏
        </el-button>
      </div>
      <div class="details-content">
        <div class="detail-item">
          <span class="detail-label">模型名称：</span>
          <span class="detail-value">{{ selectedModel.label }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">模型类型：</span>
          <span class="detail-value">{{ selectedModel.modelType.join(', ') }}</span>
        </div>
        <div class="detail-item" v-if="selectedModel.features">
          <span class="detail-label">支持功能：</span>
          <span class="detail-value">{{ selectedModel.features }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">模型状态：</span>
          <el-tag :type="getStatusType(selectedModel.status)" size="small">
            {{ getStatusText(selectedModel.status) }}
          </el-tag>
        </div>
        <div class="detail-item" v-if="selectedModel.loadBalancingEnabled">
          <span class="detail-label">负载均衡：</span>
          <el-tag type="success" size="small">已启用</el-tag>
        </div>
      </div>
    </div>
    
    <!-- 刷新按钮 -->
    <div v-if="showRefresh" class="refresh-section">
      <el-button 
        size="small" 
        :icon="RefreshIcon" 
        @click="handleRefresh"
        :loading="loading"
      >
        刷新模型列表
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh as RefreshIcon } from '@element-plus/icons-vue'
import { modelApi, type Model } from '@/api/smartcs/model'

interface Props {
  modelValue?: number
  label?: string
  placeholder?: string
  width?: string
  required?: boolean
  disabled?: boolean
  clearable?: boolean
  filterable?: boolean
  modelTypes?: string[] // 过滤特定类型的模型
  groupByProvider?: boolean // 是否按提供商分组
  showDetails?: boolean // 是否显示选中模型的详情
  showRefresh?: boolean // 是否显示刷新按钮
  autoLoad?: boolean // 是否自动加载
}

interface Emits {
  (e: 'update:modelValue', value?: number): void
  (e: 'change', model?: Model): void
  (e: 'clear'): void
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '请选择模型',
  width: '100%',
  required: false,
  disabled: false,
  clearable: true,
  filterable: true,
  groupByProvider: false,
  showDetails: false,
  showRefresh: false,
  autoLoad: true
})

const emit = defineEmits<Emits>()

// 状态管理
const loading = ref(false)
const models = ref<Model[]>([])
const showDetails = ref(props.showDetails)

// 选中的模型ID
const selectedModelId = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 选中的模型对象
const selectedModel = computed(() => {
  return models.value.find(model => model.id === selectedModelId.value)
})

// 过滤后的模型列表
const filteredModels = computed(() => {
  let filtered = models.value

  // 按模型类型过滤
  if (props.modelTypes && props.modelTypes.length > 0) {
    filtered = filtered.filter(model => 
      model.modelType.some(type => props.modelTypes!.includes(type))
    )
  }

  // 按状态过滤，只显示激活的模型
  filtered = filtered.filter(model => model.status === 'ACTIVE')

  return filtered
})

// 按提供商分组的模型列表
const groupedModels = computed(() => {
  if (!props.groupByProvider) {
    return []
  }

  const groups = new Map<number, { label: string, models: Model[] }>()
  
  filteredModels.value.forEach(model => {
    const providerId = model.providerId
    if (!groups.has(providerId)) {
      groups.set(providerId, {
        label: `提供商 ${providerId}`, // 实际应用中应该获取提供商名称
        models: []
      })
    }
    groups.get(providerId)!.models.push(model)
  })

  return Array.from(groups.values())
})

// 加载模型列表
const loadModels = async () => {
  try {
    loading.value = true
    const response = await modelApi.getAvailableModels()
    
    if (response.success && response.data) {
      models.value = response.data
    } else {
      throw new Error(response.errMessage || '获取模型列表失败')
    }
  } catch (error) {
    console.error('加载模型列表失败:', error)
    ElMessage.error('加载模型列表失败')
  } finally {
    loading.value = false
  }
}

// 处理模型选择变化
const handleModelChange = (modelId: number) => {
  const model = models.value.find(m => m.id === modelId)
  emit('change', model)
  
  if (props.showDetails && model) {
    showDetails.value = true
  }
}

// 处理清空选择
const handleClear = () => {
  emit('clear')
  showDetails.value = false
}

// 处理刷新
const handleRefresh = () => {
  loadModels()
}

// 获取状态类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'ACTIVE': return 'success'
    case 'INACTIVE': return 'warning'
    case 'DISABLED': return 'danger'
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'ACTIVE': return '激活'
    case 'INACTIVE': return '非激活'
    case 'DISABLED': return '禁用'
    default: return '未知'
  }
}

// 获取功能摘要
const getFeaturesSummary = (features: string) => {
  if (!features) return ''
  const featureList = features.split(',').map(f => f.trim())
  return featureList.length > 2 ? `${featureList.slice(0, 2).join(', ')}...` : features
}

// 监听选中模型变化，显示详情
watch(selectedModel, (newModel) => {
  if (newModel && props.showDetails) {
    showDetails.value = true
  }
})

// 组件挂载时自动加载模型列表
onMounted(() => {
  if (props.autoLoad) {
    loadModels()
  }
})

// 暴露方法给父组件
defineExpose({
  loadModels,
  selectedModel
})
</script>

<style scoped>
.model-selector {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.selector-label {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
}

.required {
  color: #f56c6c;
}

.model-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.model-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.model-name {
  font-weight: 500;
  color: #303133;
}

.model-meta {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.model-type-tag,
.features-tag {
  font-size: 11px;
}

.model-status {
  flex-shrink: 0;
}

.model-details {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
  background: #fafafa;
}

.details-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.details-title {
  font-weight: 500;
  color: #303133;
  font-size: 13px;
}

.details-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-label {
  font-size: 12px;
  color: #909399;
  min-width: 80px;
}

.detail-value {
  font-size: 12px;
  color: #606266;
}

.refresh-section {
  display: flex;
  justify-content: flex-end;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .model-option {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .model-meta {
    flex-wrap: wrap;
  }
  
  .detail-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .detail-label {
    min-width: auto;
  }
}
</style>