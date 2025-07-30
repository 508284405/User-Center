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
            v-model:knowledge="knowledgeConfig"
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
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, VideoPlay } from '@element-plus/icons-vue'
import { getApp, updateApp, updateAppStatus, type AiAppDTO } from '@/api/smartcs/app'
import AppPromptEditor from './components/AppPromptEditor.vue'
import AppVariableManager from './components/AppVariableManager.vue'
import AppKnowledgeConfig from './components/AppKnowledgeConfig.vue'
import AppTestPanel from './components/AppTestPanel.vue'

const route = useRoute()
const router = useRouter()

// 状态管理
const loading = ref(true)
const publishing = ref(false)
const appData = ref<AiAppDTO | null>(null)

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
        
        // 提示词配置
        if (config.prompt_template) {
          promptConfig.content = config.prompt_template
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

// 保存配置
const saveConfig = async () => {
  if (!appData.value) return

  try {
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
      ElMessage.success('配置保存成功')
    } else {
      throw new Error(response.errMessage || '保存失败')
    }
  } catch (error) {
    console.error('保存配置失败:', error)
    ElMessage.error(error.message || '保存配置失败')
  }
}

// 事件处理
const goBack = () => {
  router.back()
}

const handlePromptChange = () => {
  // 自动保存
  saveConfig()
}

const handleVariableChange = () => {
  saveConfig()
}

const handleKnowledgeChange = () => {
  saveConfig()
}

const handleFilterChange = () => {
  saveConfig()
}

const handleTest = () => {
  // 测试功能
  ElMessage.info('测试功能开发中...')
}

const handlePublish = async () => {
  if (!appData.value) return

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
  } catch (error) {
    console.error('发布应用失败:', error)
    ElMessage.error(error.message || '发布应用失败')
  } finally {
    publishing.value = false
  }
}

const handleTestConversation = (data: any) => {
  console.log('测试对话:', data)
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
      gap: 12px;
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
</style>