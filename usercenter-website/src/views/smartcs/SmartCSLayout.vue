<template>
  <div class="smartcs-layout">
    <!-- 页面头部 - 在意图管理和审核管理页面时隐藏 -->
    <div v-if="!isIntentPage && !isModerationPage" class="page-header">
      <div class="header-left">
        <h1 class="page-title">SmartCS AI Platform</h1>
        <p class="page-description">智能客服平台 - AI驱动的知识管理与对话系统</p>
      </div>
    </div>

    <!-- 模块导航 - 在意图管理和审核管理页面时隐藏 -->
    <el-tabs
      v-if="!isIntentPage && !isModerationPage"
      v-model="activeModule"
      class="module-tabs"
      @tab-click="onTabClick"
    >
      <el-tab-pane label="知识管理" name="knowledge" />
      <el-tab-pane label="意图管理" name="intent" />

      <el-tab-pane label="对话管理" name="conversation" />
      <el-tab-pane label="基础设施" name="infrastructure" />
    </el-tabs>

    <!-- 子页面内容 -->
    <div class="module-content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 判断是否为意图管理页面
const isIntentPage = computed(() => {
  return route.path.includes('/platform/smartcs/intent')
})



// 计算当前激活的模块
const activeModule = computed({
  get() {
    const path = route.path
    if (path.includes('/platform/smartcs/knowledge')) return 'knowledge'
    if (path.includes('/platform/smartcs/intent')) return 'intent'

    if (path.includes('/platform/smartcs/conversation')) return 'conversation'
    if (path.includes('/platform/smartcs/infrastructure')) return 'infrastructure'
    return 'knowledge' // 默认
  },
  set(val) {
    switch (val) {
      case 'knowledge':
        router.push('/platform/smartcs/knowledge')
        break
      case 'intent':
        router.push('/platform/smartcs/intent')
        break

      case 'conversation':
        router.push('/platform/smartcs/conversation')
        break
      case 'infrastructure':
        router.push('/platform/smartcs/infrastructure')
        break
      default:
        router.push('/platform/smartcs/knowledge')
    }
  }
})

const onTabClick = () => {
  // 由 activeModule 的 setter 处理路由跳转
}
</script>

<style scoped>
.smartcs-layout {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding: 24px 0 16px 0;
  border-bottom: 1px solid #e4e7ed;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 2rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-description {
  color: #6c757d;
  margin: 0;
  font-size: 14px;
}

.module-tabs {
  margin-bottom: 24px;
}

.module-content {
  min-height: 600px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .smartcs-layout {
    padding: 0 12px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .page-title {
    font-size: 1.5rem;
  }
}

/* Tab样式增强 */
:deep(.el-tabs__header) {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 0 16px;
  margin-bottom: 0;
}

:deep(.el-tabs__nav-wrap::after) {
  display: none;
}

:deep(.el-tabs__item) {
  font-weight: 500;
  color: #606266;
  transition: all 0.3s ease;
}

:deep(.el-tabs__item.is-active) {
  color: #409eff;
  font-weight: 600;
}

:deep(.el-tabs__active-bar) {
  background-color: #409eff;
  height: 3px;
}
</style>