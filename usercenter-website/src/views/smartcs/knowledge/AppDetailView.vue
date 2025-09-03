<template>
  <div class="app-detail" v-loading="loading">
    <!-- 顶部导航栏 -->
    <div class="app-header">
      <div class="header-left">
        <el-button text @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <div class="app-info">
          <span class="app-icon">{{ appData?.icon || '🤖' }}</span>
          <div class="app-meta">
            <h3 class="app-name">{{ appData?.name || '加载中...' }}</h3>
            <span class="app-type">{{ appData?.typeName }}</span>
          </div>
        </div>
      </div>
      <div class="header-actions">
        <div class="save-status" v-if="saving">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>保存中...</span>
        </div>
        <el-button @click="handlePreview">
          <el-icon><View /></el-icon>
          预览
        </el-button>
        <el-button 
          @click="handleRun" 
          :disabled="appData?.status !== 'PUBLISHED'"
          title="仅已发布的应用可以运行"
        >
          <el-icon><VideoPlay /></el-icon>
          运行
        </el-button>
        <el-button @click="handleTest">
          <el-icon><VideoPlay /></el-icon>
          测试
        </el-button>
        <el-button type="primary" @click="handlePublish" :loading="publishing">
          发布
        </el-button>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="app-content">
      <!-- 左侧配置面板 -->
      <div class="config-panel">
        <div class="panel-header">
          <h4>编排</h4>
        </div>
        
        <!-- 提示词编辑器 -->
        <div class="config-section">
          <AppPromptEditor 
            v-model:content="promptConfig.content"
            v-model:variables="promptConfig.variables"
            @change="handlePromptChange"
            @optimize-prompt="showOptimizeModal = true"
          />
        </div>

        <!-- 变量管理 -->
        <div class="config-section" v-if="promptConfig.variables.length > 0">
          <AppVariableManager 
            v-model:variables="promptConfig.variables"
            @change="handleVariableChange"
          />
        </div>

        <!-- 知识库配置 -->
        <div class="config-section">
          <AppKnowledgeConfig 
            :knowledge="knowledgeConfig"
            @update:knowledge="(val) => Object.assign(knowledgeConfig, val)"
            @change="handleKnowledgeChange"
          />
        </div>

        <!-- 无数据过滤 -->
        <div class="config-section">
          <div class="section-title">
            <span>无数据过滤</span>
            <el-switch 
              v-model="filterConfig.enabled" 
              @change="handleFilterChange"
            />
          </div>
          <div class="section-description" v-if="filterConfig.enabled">
            当检索不到相关内容时的处理方式
          </div>
        </div>
      </div>

      <!-- 右侧测试面板 -->
      <div class="test-panel">
        <AppTestPanel 
          :app-data="appData"
          :prompt-config="promptConfig"
          :knowledge-config="knowledgeConfig"
          :filter-config="filterConfig"
          @test="handleTestConversation"
          @show-function-management="showFunctionModal = true"
        />
      </div>
    </div>

    <!-- Prompt优化弹窗 -->
    <PromptOptimizeModal
      v-model="showOptimizeModal"
      :current-prompt="promptConfig.content"
      :app-id="appData?.id || 0"
      @optimized="handlePromptOptimized"
      @accepted="handlePromptOptimized"
    />

    <!-- 功能管理弹窗 -->
    <FunctionManageModal
      v-model="showFunctionModal"
      :app-id="appData?.id || 0"
      @config-changed="handleFunctionConfigChanged"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, VideoPlay, Loading, View } from '@element-plus/icons-vue'
import { getApp, updateApp, updateAppStatus, type AiAppDTO } from '@/api/smartcs/app'
import AppPromptEditor from './components/AppPromptEditor.vue'
import AppVariableManager from './components/AppVariableManager.vue'
import AppKnowledgeConfig from './components/AppKnowledgeConfig.vue'
import AppTestPanel from './components/AppTestPanel.vue'
import PromptOptimizeModal from '@/components/app/PromptOptimizeModal.vue'
import FunctionManageModal from '@/components/app/FunctionManageModal.vue'

const route = useRoute()
const router = useRouter()

// 状态管理
const loading = ref(true)
const publishing = ref(false)
const saving = ref(false)
const appData = ref<AiAppDTO | null>(null)
const showOptimizeModal = ref(false)
const showFunctionModal = ref(false)
let saveTimeout: NodeJS.Timeout | null = null

// 配置数据
const promptConfig = reactive({
  content: '',
  variables: [] as Array<{
    key: string
    label: string
    type: string
    required: boolean
    defaultValue?: any
  }>
})

const knowledgeConfig = reactive({
  enabled: false,
  knowledgeBases: [] as Array<number>,
  retrievalSettings: {
    topK: 3,
    scoreThreshold: 0.5
  }
})

const filterConfig = reactive({
  enabled: false,
  strategy: 'disabled' // disabled, reply, redirect
})

// 获取应用详情
const fetchAppDetail = async () => {
  try {
    const appId = parseInt(route.params.id as string)
    if (isNaN(appId)) {
      throw new Error('无效的应用ID')
    }

    const response = await getApp(appId)
    if (response.success && response.data) {
      appData.value = response.data
      
      // 解析应用配置
      if (response.data.config) {
        const config = response.data.config
        
        // 提示词配置 - 优先使用优化后的提示词
        // 后端返回有两种格式：
        // 1) 旧版：config.prompt_template 为字符串
        // 2) 新版：config.prompt_template 为对象，包含 optimizedPrompt / originalPrompt 等字段
        let resolvedPrompt: any = undefined

        // 兼容极少数情况下 optimizedPrompt 置于顶层的返回
        const topLevelOptimized = (response.data as any).optimizedPrompt

        if (config.prompt_template) {
          const pt = config.prompt_template
          if (typeof pt === 'object') {
            // 新版结构：优先取 optimizedPrompt，其次 originalPrompt，再退化到可能的 content/text/prompt
            const candidate = pt.optimizedPrompt ?? pt.originalPrompt ?? pt.content ?? pt.text ?? pt.prompt
            resolvedPrompt = candidate
          } else {
            // 旧版：直接是字符串
            resolvedPrompt = pt
          }
        }

        // 顶层字段作为兜底（保持兼容性）
        if (!resolvedPrompt && topLevelOptimized) {
          resolvedPrompt = topLevelOptimized
        }

        if (resolvedPrompt) {
          // 避免把对象直接转成 [object Object]
          if (typeof resolvedPrompt === 'object') {
            promptConfig.content = String(resolvedPrompt.content || resolvedPrompt.text || resolvedPrompt.prompt || '')
          } else {
            promptConfig.content = String(resolvedPrompt)
          }
        } else {
          promptConfig.content = ''
        }
        
        // 变量配置
        if (config.variables) {
          promptConfig.variables = config.variables
        }
        
        // 知识库配置
        if (config.knowledge) {
          Object.assign(knowledgeConfig, config.knowledge)
        }
        
        // 过滤配置
        if (config.filter) {
          Object.assign(filterConfig, config.filter)
        }
      }
    } else {
      throw new Error(response.errMessage || '获取应用详情失败')
    }
  } catch (error) {
    console.error('获取应用详情失败:', error)
    ElMessage.error(error.message || '获取应用详情失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 防抖保存配置
const debouncedSaveConfig = () => {
  if (saveTimeout) {
    clearTimeout(saveTimeout)
  }
  
  saveTimeout = setTimeout(() => {
    saveConfig()
  }, 1000) // 1秒防抖
}

// 保存配置
const saveConfig = async () => {
  if (!appData.value || saving.value) return

  try {
    saving.value = true
    
    const config = {
      prompt_template: promptConfig.content,
      variables: promptConfig.variables,
      knowledge: knowledgeConfig,
      filter: filterConfig
    }

    const response = await updateApp({
      id: appData.value.id!,
      name: appData.value.name,
      description: appData.value.description,
      config,
      icon: appData.value.icon,
      tags: appData.value.tags
    })

    if (response.success) {
      // 静默保存，不显示成功消息
      console.log('配置已自动保存')
    } else {
      throw new Error(response.errMessage || '保存失败')
    }
  } catch (error: any) {
    console.error('保存配置失败:', error)
    ElMessage.error(error.message || '自动保存失败')
  } finally {
    saving.value = false
  }
}

// 事件处理
const goBack = () => {
  router.back()
}

// 预览应用（新窗口打开，管理员模式）
const handlePreview = () => {
  if (!appData.value?.id) {
    ElMessage.warning('应用数据加载中，请稍后再试')
    return
  }
  
  const previewUrl = `/app/preview/${appData.value.id}`
  window.open(previewUrl, '_blank', 'width=1200,height=800')
}

// 运行应用（新窗口打开，公开模式）
const handleRun = () => {
  if (!appData.value?.id) {
    ElMessage.warning('应用数据加载中，请稍后再试')
    return
  }
  
  if (appData.value.status !== 'PUBLISHED') {
    ElMessage.warning('仅已发布的应用可以运行')
    return
  }
  
  const runUrl = `/app/run/${appData.value.id}`
  window.open(runUrl, '_blank', 'width=1200,height=800')
}

const handlePromptChange = () => {
  // 防抖自动保存
  debouncedSaveConfig()
}

const handleVariableChange = () => {
  debouncedSaveConfig()
}

const handleKnowledgeChange = () => {
  debouncedSaveConfig()
}

const handleFilterChange = () => {
  debouncedSaveConfig()
}

const handleTest = () => {
  // 测试功能
  ElMessage.info('测试功能开发中...')
}

const handlePublish = async () => {
  if (!appData.value) return

  // 发布前先保存当前配置
  if (saveTimeout) {
    clearTimeout(saveTimeout)
    await saveConfig()
  }

  try {
    publishing.value = true
    
    const response = await updateAppStatus({
      id: appData.value.id!,
      status: 'PUBLISHED'
    })

    if (response.success) {
      ElMessage.success('应用发布成功')
      appData.value.status = 'PUBLISHED'
      appData.value.statusName = '已发布'
    } else {
      throw new Error(response.errMessage || '发布失败')
    }
  } catch (error: any) {
    console.error('发布应用失败:', error)
    ElMessage.error(error.message || '发布应用失败')
  } finally {
    publishing.value = false
  }
}

const handleTestConversation = (data: any) => {
  console.log('测试对话:', data)
}

// 处理Prompt优化
const handlePromptOptimized = (optimizedPrompt: string) => {
  promptConfig.content = optimizedPrompt
  handlePromptChange()
  ElMessage.success('Prompt已更新')
}

// 处理功能配置变更
const handleFunctionConfigChanged = (config: any) => {
  console.log('功能配置已更新:', config)
}

// 监听路由变化
watch(() => route.params.id, () => {
  if (route.params.id) {
    fetchAppDetail()
  }
}, { immediate: true })

onMounted(() => {
  // 组件挂载时的逻辑
})

onUnmounted(() => {
  // 清理定时器
  if (saveTimeout) {
    clearTimeout(saveTimeout)
  }
})
</script>

<style scoped lang="scss">
.app-detail {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8fafc;

  .app-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
    background: white;
    border-bottom: 1px solid #e5e7eb;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);

    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;

      .app-info {
        display: flex;
        align-items: center;
        gap: 12px;

        .app-icon {
          font-size: 32px;
          width: 48px;
          height: 48px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: #f3f4f6;
          border-radius: 12px;
        }

        .app-meta {
          .app-name {
            margin: 0 0 4px 0;
            font-size: 18px;
            font-weight: 600;
            color: #1f2937;
          }

          .app-type {
            font-size: 14px;
            color: #6b7280;
          }
        }
      }
    }

    .header-actions {
      display: flex;
      align-items: center;
      gap: 12px;

      .save-status {
        display: flex;
        align-items: center;
        gap: 6px;
        color: #6b7280;
        font-size: 14px;

        .el-icon {
          font-size: 16px;
        }
      }
    }
  }

  .app-content {
    flex: 1;
    display: flex;
    min-height: 0;

    .config-panel {
      width: 480px;
      background: white;
      border-right: 1px solid #e5e7eb;
      display: flex;
      flex-direction: column;
      overflow-y: auto;

      .panel-header {
        padding: 20px 24px 0;
        border-bottom: 1px solid #f3f4f6;

        h4 {
          margin: 0 0 16px 0;
          font-size: 16px;
          font-weight: 600;
          color: #1f2937;
        }
      }

      .config-section {
        padding: 24px;
        border-bottom: 1px solid #f3f4f6;

        &:last-child {
          border-bottom: none;
        }

        .section-title {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;
          font-weight: 500;
          color: #374151;
        }

        .section-description {
          font-size: 14px;
          color: #6b7280;
          line-height: 1.5;
        }
      }
    }

    .test-panel {
      flex: 1;
      background: white;
      display: flex;
      flex-direction: column;
    }
  }
}

:deep(.el-loading-mask) {
  background-color: rgba(255, 255, 255, 0.9);
}

// 响应式设计
@media (max-width: 1200px) {
  .app-detail {
    .app-content {
      .config-panel {
        width: 400px;
      }
    }
  }
}

@media (max-width: 1024px) {
  .app-detail {
    .app-header {
      padding: 12px 16px;

      .header-left {
        gap: 12px;

        .app-info {
          gap: 8px;

          .app-icon {
            width: 40px;
            height: 40px;
            font-size: 24px;
          }

          .app-meta {
            .app-name {
              font-size: 16px;
            }

            .app-type {
              font-size: 13px;
            }
          }
        }
      }

      .header-actions {
        .save-status {
          display: none; // 隐藏保存状态
        }
      }
    }

    .app-content {
      flex-direction: column;

      .config-panel {
        width: 100%;
        border-right: none;
        border-bottom: 1px solid #e5e7eb;
        max-height: 60vh;
      }

      .test-panel {
        flex: 1;
        min-height: 40vh;
      }
    }
  }
}

@media (max-width: 768px) {
  .app-detail {
    .app-header {
      .header-left {
        .app-info {
          .app-meta {
            .app-name {
              font-size: 14px;
            }

            .app-type {
              font-size: 12px;
            }
          }
        }
      }

      .header-actions {
        gap: 8px;

        .el-button {
          padding: 8px 12px;
        }
      }
    }
  }
}
</style>
