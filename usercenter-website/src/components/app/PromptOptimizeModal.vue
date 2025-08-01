<template>
  <el-dialog
    v-model="visible"
    title="Prompt优化"
    width="800px"
    :close-on-click-modal="false"
    @closed="handleClose"
  >
    <div class="optimize-container">
      <!-- 原始Prompt输入 -->
      <div class="form-section">
        <div class="section-title">
          <span>原始Prompt</span>
          <span class="required">*</span>
        </div>
        <el-input
          v-model="formData.originalPrompt"
          type="textarea"
          :rows="6"
          placeholder="请输入需要优化的Prompt内容..."
          maxlength="4000"
          show-word-limit
          :disabled="optimizing"
        />
      </div>

      <!-- 优化目标 -->
      <div class="form-section">
        <div class="section-title">优化目标</div>
        <el-input
          v-model="formData.optimizeGoal"
          type="textarea"
          :rows="3"
          placeholder="请描述您希望的优化方向，例如：让回答更专业和准确、增加更多创意等..."
          maxlength="500"
          show-word-limit
          :disabled="optimizing"
        />
      </div>

      <!-- 模型选择 -->
      <div class="form-section">
        <div class="section-title">优化模型</div>
        <el-select 
          v-model="formData.modelId" 
          placeholder="选择用于优化的LLM模型"
          style="width: 100%"
          :loading="modelsLoading"
          :disabled="optimizing"
          filterable
        >
          <el-option
            v-for="model in availableModels"
            :key="model.id"
            :label="`${model.label} (${model.modelType.join('/')})`"
            :value="model.id"
          >
            <div class="model-option">
              <span class="model-name">{{ model.label }}</span>
              <span class="model-type">{{ model.modelType.join('/') }}</span>
            </div>
          </el-option>
        </el-select>
      </div>

      <!-- 优化结果 -->
      <div class="form-section" v-if="optimizeResult">
        <div class="section-title">优化结果</div>
        <div class="result-container">
          <div class="result-tabs">
            <el-tabs v-model="activeTab" type="border-card">
              <el-tab-pane label="优化后的Prompt" name="optimized">
                <div class="prompt-content">
                  <el-input
                    v-model="optimizeResult.optimizedPrompt"
                    type="textarea"
                    :rows="8"
                    readonly
                  />
                  <div class="copy-actions">
                    <el-button 
                      size="small" 
                      @click="copyToClipboard(optimizeResult.optimizedPrompt)"
                    >
                      复制
                    </el-button>
                  </div>
                </div>
              </el-tab-pane>
              <el-tab-pane label="优化说明" name="explanation">
                <div class="explanation-content">
                  <p>{{ optimizeResult.optimizeExplanation }}</p>
                  <div class="meta-info">
                    <span>使用模型：{{ optimizeResult.modelName }}</span>
                    <span>任务ID：{{ optimizeResult.taskId }}</span>
                  </div>
                </div>
              </el-tab-pane>
              <el-tab-pane label="对比视图" name="compare">
                <div class="compare-view">
                  <div class="compare-column">
                    <h4>原始Prompt</h4>
                    <div class="prompt-box original">
                      {{ formData.originalPrompt }}
                    </div>
                  </div>
                  <div class="compare-column">
                    <h4>优化后Prompt</h4>
                    <div class="prompt-box optimized">
                      {{ optimizeResult.optimizedPrompt }}
                    </div>
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div class="loading-section" v-if="optimizing">
        <el-alert
          title="正在优化Prompt，请稍等..."
          type="info"
          show-icon
          :closable="false"
        >
          <template #default>
            <div class="loading-content">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>AI正在分析您的Prompt并生成优化建议...</span>
            </div>
          </template>
        </el-alert>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose" :disabled="optimizing">
          取消
        </el-button>
        <el-button 
          type="primary" 
          @click="handleOptimize" 
          :loading="optimizing"
          :disabled="!canOptimize"
        >
          {{ optimizing ? '优化中...' : (optimizeResult ? '重新优化' : '开始优化') }}
        </el-button>
        <el-button 
          v-if="optimizeResult" 
          type="success" 
          @click="handleAccept"
          :disabled="optimizing"
        >
          采用优化结果
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { optimizePrompt, type AiAppPromptOptimizeRequest, type AiAppPromptOptimizeResponse } from '@/api/smartcs/app'
import { modelApi, type Model } from '@/api/smartcs/model'

interface Props {
  modelValue: boolean
  appId: number
  initialPrompt?: string
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'optimized', result: AiAppPromptOptimizeResponse): void
  (e: 'accepted', optimizedPrompt: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 显示状态
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 表单数据
const formData = reactive<AiAppPromptOptimizeRequest>({
  appId: props.appId,
  originalPrompt: '',
  optimizeGoal: '',
  modelId: undefined
})

// 状态管理
const optimizing = ref(false)
const modelsLoading = ref(false)
const availableModels = ref<Model[]>([])
const optimizeResult = ref<AiAppPromptOptimizeResponse | null>(null)
const activeTab = ref('optimized')

// 计算属性
const canOptimize = computed(() => {
  return formData.originalPrompt.trim().length > 0 && formData.modelId
})

// 加载可用模型
const loadAvailableModels = async () => {
  try {
    modelsLoading.value = true
    const response = await modelApi.getAvailableModels()
    if (response.success && response.data) {
      // 过滤出LLM类型的模型
      availableModels.value = response.data.filter(model => 
        model.modelType.includes('LLM') && model.status === 'ACTIVE'
      )
      
      // 自动选择第一个模型
      if (availableModels.value.length > 0 && !formData.modelId) {
        formData.modelId = availableModels.value[0].id
      }
    }
  } catch (error) {
    console.error('加载模型列表失败:', error)
    ElMessage.error('加载模型列表失败')
  } finally {
    modelsLoading.value = false
  }
}

// 优化Prompt
const handleOptimize = async () => {
  if (!canOptimize.value) {
    ElMessage.warning('请填写原始Prompt并选择模型')
    return
  }

  try {
    optimizing.value = true
    optimizeResult.value = null
    
    const response = await optimizePrompt(formData)
    if (response.success && response.data) {
      optimizeResult.value = response.data
      activeTab.value = 'optimized'
      emit('optimized', response.data)
      ElMessage.success('Prompt优化完成！')
    } else {
      throw new Error(response.errMessage || 'Prompt优化失败')
    }
  } catch (error) {
    console.error('Prompt优化失败:', error)
    ElMessage.error(error.message || 'Prompt优化失败')
  } finally {
    optimizing.value = false
  }
}

// 采用优化结果
const handleAccept = () => {
  if (optimizeResult.value) {
    emit('accepted', optimizeResult.value.optimizedPrompt)
    ElMessage.success('已采用优化后的Prompt')
    handleClose()
  }
}

// 复制到剪贴板
const copyToClipboard = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
}

// 关闭弹窗
const handleClose = () => {
  visible.value = false
}

// 重置表单
const resetForm = () => {
  formData.originalPrompt = props.initialPrompt || ''
  formData.optimizeGoal = ''
  formData.modelId = availableModels.value.length > 0 ? availableModels.value[0].id : undefined
  optimizeResult.value = null
  activeTab.value = 'optimized'
}

// 监听弹窗显示状态
watch(visible, (newVisible) => {
  if (newVisible) {
    resetForm()
    if (availableModels.value.length === 0) {
      loadAvailableModels()
    }
  }
})

// 监听初始Prompt变化
watch(() => props.initialPrompt, (newPrompt) => {
  if (newPrompt) {
    formData.originalPrompt = newPrompt
  }
})

// 组件挂载时加载模型列表
onMounted(() => {
  loadAvailableModels()
})
</script>

<style scoped>
.optimize-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.section-title {
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
}

.model-name {
  font-weight: 500;
}

.model-type {
  font-size: 12px;
  color: #909399;
}

.result-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.prompt-content {
  position: relative;
}

.copy-actions {
  position: absolute;
  top: 8px;
  right: 8px;
}

.explanation-content {
  padding: 16px;
}

.explanation-content p {
  margin: 0 0 16px 0;
  line-height: 1.6;
  color: #606266;
}

.meta-info {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}

.compare-view {
  display: flex;
  gap: 16px;
  padding: 16px;
}

.compare-column {
  flex: 1;
}

.compare-column h4 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 14px;
}

.prompt-box {
  padding: 12px;
  border-radius: 4px;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
  max-height: 300px;
  overflow-y: auto;
}

.prompt-box.original {
  background: #fef0f0;
  border: 1px solid #fbc4c4;
  color: #f56c6c;
}

.prompt-box.optimized {
  background: #f0f9ff;
  border: 1px solid #b3d8ff;
  color: #409eff;
}

.loading-section {
  margin: 16px 0;
}

.loading-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .compare-view {
    flex-direction: column;
    gap: 16px;
  }
  
  .meta-info {
    flex-direction: column;
    gap: 8px;
  }
}
</style>