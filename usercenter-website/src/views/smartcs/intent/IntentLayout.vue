<template>
  <div class="intent-layout">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">意图管理</h1>
        <p class="page-description">统一的意图分类、目录、快照与测试管理</p>
      </div>
    </div>

    <!-- 功能模块导航 -->
    <el-tabs
      v-model="activeTab"
      class="intent-tabs"
      @tab-click="onTabClick"
    >
      <el-tab-pane label="意图分类管理" name="classification" />
      <el-tab-pane label="意图目录管理" name="catalog" />
      <el-tab-pane label="分类测试" name="testing" />
      <el-tab-pane label="快照管理" name="snapshots" />
    </el-tabs>

    <!-- 子页面内容 -->
    <div class="tab-content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const activeTab = computed({
  get() {
    const path = route.path
    if (path.includes('/platform/smartcs/intent/catalog')) return 'catalog'
    if (path.includes('/platform/smartcs/intent/testing')) return 'testing'
    if (path.includes('/platform/smartcs/intent/snapshots')) return 'snapshots'
    // 默认分类管理
    return 'classification'
  },
  set(val) {
    switch (val) {
      case 'catalog':
        router.push('/platform/smartcs/intent/catalog')
        break
      case 'testing':
        router.push('/platform/smartcs/intent/testing')
        break
      case 'snapshots':
        router.push('/platform/smartcs/intent/snapshots')
        break
      default:
        router.push('/platform/smartcs/intent/classification')
    }
  }
})

function onTabClick() {
  // 由 activeTab 的 setter 处理路由
}

// 确保直接访问父路由时跳转到默认 TAB
watch(
  () => route.fullPath,
  (path) => {
    if (path === '/platform/smartcs/intent' || path === '/platform/smartcs/intent/') {
      router.replace('/platform/smartcs/intent/classification')
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.intent-layout {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 1.75rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.page-description {
  color: #6c757d;
  margin: 0;
  font-size: 14px;
}

.intent-tabs {
  margin-bottom: 24px;
}

.tab-content {
  min-height: 500px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .page-title {
    font-size: 1.5rem;
  }
}

/* Tab样式 */
:deep(.el-tabs__header) {
  background-color: #f8f9fa;
  border-radius: 6px;
  padding: 0 12px;
  margin-bottom: 0;
}

:deep(.el-tabs__nav-wrap::after) {
  display: none;
}

:deep(.el-tabs__item) {
  font-weight: 500;
  color: #606266;
}

:deep(.el-tabs__item.is-active) {
  color: #409eff;
  font-weight: 600;
}
</style>