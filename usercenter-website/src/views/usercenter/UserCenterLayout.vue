<template>
  <div class="usercenter-layout">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">User Center</h1>
        <p class="page-description">用户中心 - 身份管理与系统管理</p>
      </div>
    </div>

    <!-- 模块导航 -->
    <el-tabs
      v-model="activeModule"
      class="module-tabs"
      @tab-click="onTabClick"
    >
      <el-tab-pane label="身份管理" name="identity" />
      <el-tab-pane label="系统管理" name="system" />
      <el-tab-pane label="个人资料" name="profile" />
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

// 计算当前激活的模块
const activeModule = computed({
  get() {
    const path = route.path
    if (path.includes('/platform/usercenter/identity')) return 'identity'
    if (path.includes('/platform/usercenter/system')) return 'system'
    if (path.includes('/platform/usercenter/profile')) return 'profile'
    return 'identity' // 默认
  },
  set(val) {
    switch (val) {
      case 'identity':
        router.push('/platform/usercenter/identity')
        break
      case 'system':
        router.push('/platform/usercenter/system')
        break
      case 'profile':
        router.push('/platform/usercenter/profile')
        break
      default:
        router.push('/platform/usercenter/identity')
    }
  }
})

const onTabClick = () => {
  // 由 activeModule 的 setter 处理路由跳转
}
</script>

<style scoped>
.usercenter-layout {
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
  background: linear-gradient(135deg, #74b9ff 0%, #0984e3 100%);
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
  .usercenter-layout {
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
  background-color: #f0f8ff;
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
  color: #74b9ff;
  font-weight: 600;
}

:deep(.el-tabs__active-bar) {
  background-color: #74b9ff;
  height: 3px;
}
</style>