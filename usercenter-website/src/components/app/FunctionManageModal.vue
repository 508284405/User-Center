<template>
  <el-drawer
    v-model="visible"
    title="功能管理"
    direction="rtl"
    size="420px"
    :close-on-click-modal="false"
    @closed="handleClose"
  >
    <div class="function-manage-container">
      <!-- 功能描述 -->
      <div class="description-section">
        <p class="description-text">
          增强 web app 用户体验。在对话类型应用中，让 AI 主动说第一句话可以提高与用户的距离。
        </p>
      </div>

      <!-- 功能配置列表 -->
      <div class="functions-list">
        
        <!-- 对话开场白 -->
        <div class="function-item">
          <div class="function-header">
            <div class="function-info">
              <div class="function-icon">💬</div>
              <div class="function-content">
                <h3 class="function-title">对话开场白</h3>
                <p class="function-desc">在对话类型应用中，让AI主动说第一句话可以拉近与用户的距离。</p>
              </div>
            </div>
            <el-switch 
              v-model="functionConfig.conversationOpenerEnabled" 
              @change="handleConfigChange"
              :loading="saving"
            />
          </div>
        </div>

        <!-- 下一步问题建议 -->
        <div class="function-item">
          <div class="function-header">
            <div class="function-info">
              <div class="function-icon">💡</div>
              <div class="function-content">
                <h3 class="function-title">下一步问题建议</h3>
                <p class="function-desc">设置下一步问题建议可以让用户更好的对话。</p>
              </div>
            </div>
            <el-switch 
              v-model="functionConfig.nextQuestionSuggestionEnabled" 
              @change="handleConfigChange"
              :loading="saving"
            />
          </div>
        </div>

        <!-- 文字转语音 -->
        <div class="function-item">
          <div class="function-header">
            <div class="function-info">
              <div class="function-icon">🔊</div>
              <div class="function-content">
                <h3 class="function-title">文字转语音</h3>
                <p class="function-desc">文本可以转换成语音。</p>
              </div>
            </div>
            <el-switch 
              v-model="functionConfig.textToSpeechEnabled" 
              @change="handleConfigChange"
              :loading="saving"
            />
          </div>
        </div>

        <!-- 语音转文字 -->
        <div class="function-item">
          <div class="function-header">
            <div class="function-info">
              <div class="function-icon">🎤</div>
              <div class="function-content">
                <h3 class="function-title">语音转文字</h3>
                <p class="function-desc">您可以使用语音输入。</p>
              </div>
            </div>
            <el-switch 
              v-model="functionConfig.speechToTextEnabled" 
              @change="handleConfigChange"
              :loading="saving"
            />
          </div>
        </div>

        <!-- 引用和归属 -->
        <div class="function-item">
          <div class="function-header">
            <div class="function-info">
              <div class="function-icon">📄</div>
              <div class="function-content">
                <h3 class="function-title">引用和归属</h3>
                <p class="function-desc">显示文本中的生成内容的引用部分。</p>
              </div>
            </div>
            <el-switch 
              v-model="functionConfig.citationEnabled" 
              @change="handleConfigChange"
              :loading="saving"
            />
          </div>
        </div>

        <!-- 内容审查 -->
        <div class="function-item">
          <div class="function-header">
            <div class="function-info">
              <div class="function-icon">🛡️</div>
              <div class="function-content">
                <h3 class="function-title">内容审查</h3>
                <p class="function-desc">您可以调用审查API或者保护用户输入以使模型输出更安全地输出。</p>
              </div>
            </div>
            <el-switch 
              v-model="functionConfig.contentModerationEnabled" 
              @change="handleConfigChange"
              :loading="saving"
            />
          </div>
        </div>

        <!-- 标杆回复 -->
        <div class="function-item">
          <div class="function-header">
            <div class="function-info">
              <div class="function-icon">⭐</div>
              <div class="function-content">
                <h3 class="function-title">标杆回复</h3>
                <p class="function-desc">启用后，将标杆用户的回复，以便您用更喜欢的回复这个询问。</p>
              </div>
            </div>
            <el-switch 
              v-model="functionConfig.standardReplyEnabled" 
              @change="handleConfigChange"
              :loading="saving"
            />
          </div>
        </div>

      </div>

      <!-- 保存状态提示 -->
      <div class="save-status" v-if="saving">
        <el-alert
          title="正在保存配置..."
          type="info"
          show-icon
          :closable="false"
        >
          <template #default>
            <div class="saving-content">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>配置保存中，请稍等...</span>
            </div>
          </template>
        </el-alert>
      </div>

      <!-- 重置按钮 -->
      <div class="action-section">
        <el-button 
          @click="handleReset" 
          :loading="loading"
          :disabled="saving"
        >
          重置为默认值
        </el-button>
        <el-button 
          type="primary" 
          @click="handleSave" 
          :loading="saving"
        >
          保存配置
        </el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { 
  updateFunctionConfig, 
  getFunctionConfig, 
  type AiAppFunctionConfigRequest, 
  type AiAppFunctionConfigResponse 
} from '@/api/smartcs/app'

interface Props {
  modelValue: boolean
  appId: number
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'configChanged', config: AiAppFunctionConfigResponse): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 显示状态
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 状态管理
const loading = ref(false)
const saving = ref(false)
const originalConfig = ref<AiAppFunctionConfigResponse | null>(null)

// 功能配置
const functionConfig = reactive({
  conversationOpenerEnabled: false,
  nextQuestionSuggestionEnabled: false,
  textToSpeechEnabled: false,
  speechToTextEnabled: false,
  citationEnabled: true, // 默认启用引用和归属
  contentModerationEnabled: false,
  standardReplyEnabled: false
})

// 默认配置
const defaultConfig = {
  conversationOpenerEnabled: false,
  nextQuestionSuggestionEnabled: false,
  textToSpeechEnabled: false,
  speechToTextEnabled: false,
  citationEnabled: true,
  contentModerationEnabled: false,
  standardReplyEnabled: false
}

// 检查配置是否有变化
const hasChanges = computed(() => {
  if (!originalConfig.value) return false
  
  return Object.keys(functionConfig).some(key => {
    return functionConfig[key] !== originalConfig.value![key]
  })
})

// 加载功能配置
const loadFunctionConfig = async () => {
  try {
    loading.value = true
    const response = await getFunctionConfig(props.appId)
    
    if (response.success && response.data) {
      const config = response.data
      
      // 更新本地配置
      Object.assign(functionConfig, {
        conversationOpenerEnabled: config.conversationOpenerEnabled,
        nextQuestionSuggestionEnabled: config.nextQuestionSuggestionEnabled,
        textToSpeechEnabled: config.textToSpeechEnabled,
        speechToTextEnabled: config.speechToTextEnabled,
        citationEnabled: config.citationEnabled,
        contentModerationEnabled: config.contentModerationEnabled,
        standardReplyEnabled: config.standardReplyEnabled
      })
      
      // 保存原始配置用于变更检测
      originalConfig.value = { ...config }
    } else {
      // 如果获取失败，使用默认配置
      Object.assign(functionConfig, defaultConfig)
    }
  } catch (error) {
    console.error('加载功能配置失败:', error)
    // 使用默认配置
    Object.assign(functionConfig, defaultConfig)
    ElMessage.error('加载功能配置失败，使用默认配置')
  } finally {
    loading.value = false
  }
}

// 防抖保存配置
let saveTimeout: NodeJS.Timeout | null = null

const debouncedSave = () => {
  if (saveTimeout) {
    clearTimeout(saveTimeout)
  }
  
  saveTimeout = setTimeout(() => {
    handleSave()
  }, 1000) // 1秒防抖
}

// 处理配置变化
const handleConfigChange = () => {
  // 实时保存配置
  debouncedSave()
}

// 保存配置
const handleSave = async () => {
  try {
    saving.value = true
    
    const request: AiAppFunctionConfigRequest = {
      appId: props.appId,
      ...functionConfig
    }
    
    const response = await updateFunctionConfig(props.appId, request)
    
    if (response.success) {
      // 更新原始配置
      originalConfig.value = { ...functionConfig, appId: props.appId, functionConfig: {} }
      
      // 触发配置变更事件
      emit('configChanged', originalConfig.value)
      
      ElMessage.success('功能配置保存成功')
    } else {
      throw new Error(response.errMessage || '保存配置失败')
    }
  } catch (error) {
    console.error('保存功能配置失败:', error)
    ElMessage.error(error.message || '保存功能配置失败')
  } finally {
    saving.value = false
  }
}

// 重置配置
const handleReset = () => {
  Object.assign(functionConfig, defaultConfig)
  ElMessage.success('已重置为默认配置')
  debouncedSave()
}

// 关闭抽屉
const handleClose = () => {
  // 如果有未保存的更改，提示用户
  if (hasChanges.value && !saving.value) {
    ElMessage.info('配置将自动保存')
  }
}

// 监听抽屉显示状态
watch(visible, (newVisible) => {
  if (newVisible) {
    loadFunctionConfig()
  }
})

// 清理定时器
const cleanup = () => {
  if (saveTimeout) {
    clearTimeout(saveTimeout)
    saveTimeout = null
  }
}

// 组件卸载时清理
import { onUnmounted } from 'vue'
onUnmounted(() => {
  cleanup()
})
</script>

<style scoped>
.function-manage-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 20px;
}

.description-section {
  padding: 0 4px;
}

.description-text {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.functions-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow-y: auto;
}

.function-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  background: #fff;
  transition: border-color 0.2s ease;
}

.function-item:hover {
  border-color: #c0c4cc;
}

.function-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.function-info {
  display: flex;
  gap: 12px;
  flex: 1;
}

.function-icon {
  font-size: 20px;
  line-height: 1;
  flex-shrink: 0;
}

.function-content {
  flex: 1;
}

.function-title {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.function-desc {
  margin: 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.save-status {
  margin: 16px 0;
}

.saving-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-section {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
}

.action-section .el-button {
  flex: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .function-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .function-info {
    width: 100%;
  }
  
  .action-section {
    flex-direction: column;
  }
  
  .action-section .el-button {
    width: 100%;
  }
}

/* 滚动条样式 */
.functions-list::-webkit-scrollbar {
  width: 6px;
}

.functions-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.functions-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.functions-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>